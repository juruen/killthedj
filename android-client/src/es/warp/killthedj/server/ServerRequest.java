/**
* Kill The DJ is a social jukebox for Spotify and Android
*   
* Copyright (C) 2012, Warp Networks, S.L
* 
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 3 of the License, or
* (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software Foundation,
* Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
*
*/
package es.warp.killthedj.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.util.Log;
import es.warp.killthedj.KillTheDjActivity;
import es.warp.killthedj.config.RemoteServers;
import es.warp.killthedj.playlist.Playlist;
import es.warp.killthedj.track.AvailableTrack;

public class ServerRequest extends RemoteServers {
    private final DefaultHttpClient httpClient;
    private final ContentResolver resolver;
    private final String partyId;

    public ServerRequest(ContentResolver resolver) {
        httpClient = new DefaultHttpClient();
        this.resolver = resolver;
        this.partyId = KillTheDjActivity.partyId;
    }

    public int requestTrack(AvailableTrack track) throws ServerRequestException, ServerRequestForbiddenException  {
        try {
            JSONObject json = new JSONObject(post(requestTrackURL(partyId), track.toJson()));
            return json.getInt("id");
        } catch (JSONException e) {
            throw new ServerRequestException(e.getMessage(), e);
        } catch (ClientProtocolException e) {
            throw new ServerRequestException(e.getMessage(), e);
        } catch (IOException e) {
            throw new ServerRequestException(e.getMessage(), e);
        }
    }

    public Playlist getPlaylist() throws ServerRequestException  {
        try {
            return new Playlist(new JSONArray(get(requestPlaylistURL(partyId))));
        } catch (Exception e) {
            throw new ServerRequestException(e.getMessage(), e);
        }
    }

    public void vote(boolean type, AvailableTrack track) throws ServerRequestException  {
        try {
            post(voteSongURL(partyId), track.voteJson(type));
        } catch (Exception e) {
            throw new ServerRequestException(e.getMessage(), e);
        }
    }

    public List<AvailableTrack> getTracks(List<String> mytracks) throws ServerRequestException {
        try {
            List<AvailableTrack> tracks = new ArrayList<AvailableTrack>();
            JSONArray json = new JSONArray(get(requestTrackStatusURL(partyId, mytracks)));
            for (int i = 0; i < json.length(); i++) {
                tracks.add(new AvailableTrack(json.getJSONObject(i)));
            }
            return tracks;
        } catch (Exception e){
            throw new ServerRequestException(e.getMessage(), e);
        }
    }

    public AvailableTrack getCurrentTrack() throws ServerRequestException {
        try {
            return new AvailableTrack(new JSONObject(get(requestCurrentTrackURL(partyId))));
        } catch (Exception e){
            throw new ServerRequestException(e.getMessage(), e);
        }
    }

    public Bitmap getCurrentCover() throws ServerRequestException {
        try {
            URL coverUrl = new URL(RemoteServers.currentTrackCoverURL(partyId));
            return BitmapFactory.decodeStream(coverUrl.openConnection() .getInputStream());
        } catch (IOException e){
            throw new ServerRequestException(e.getMessage(), e);
        }
    }

    protected String post(String url, String data) throws ClientProtocolException, IOException, ServerRequestException, ServerRequestForbiddenException {
        Log.d("ServerRequest", "Post " + url + " (data: "+ data+")");
        long startTime = System.currentTimeMillis();
        HttpPost postMethod = new HttpPost(url);
        postMethod.addHeader("android-device-id", Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID));
        postMethod.setEntity(new StringEntity(data, "utf8"));
        HttpResponse res = httpClient.execute(postMethod);
        if (res.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN) {
            throw new ServerRequestForbiddenException();
        } else if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new ServerRequestException(
                    "Invalid server response (" + String.valueOf(res.getStatusLine().getStatusCode()) + ")");
        }
        String body = toString(res.getEntity().getContent());
        Log.d("ServerRequest", "Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");
        return body;
    }

    protected String get(String url) throws ClientProtocolException, IOException, ServerRequestException, ServerRequestForbiddenException {
        Log.d("ServerRequest", "Get " + url);
        long startTime = System.currentTimeMillis();
        HttpGet getMethod = new HttpGet(url);
        getMethod.addHeader("android-device-id", Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID));
        HttpResponse res = httpClient.execute(getMethod);
        if (res.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN) {
            throw new ServerRequestForbiddenException();
        } else if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new ServerRequestException(
                    "Invalid server response (" + String.valueOf(res.getStatusLine().getStatusCode()) + ")");
        }
        String body = toString(res.getEntity().getContent());
        Log.d("ServerRequest", "Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");
        return body;
    }

    protected String toString(InputStream in) throws IOException {
        StringBuilder sb  = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while((line = br.readLine()) != null) sb.append(line);
        return sb.toString();

    }
}
