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
package es.warp.killthedj.spotify;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import es.warp.killthedj.track.AvailableTrack;

public class SpotifyQuery {
    private final SpotifyHTTPGet spotifyHTTPGet;

    public SpotifyQuery() {
        spotifyHTTPGet = new SpotifyHTTPGet();
    }

    public SpotifyQuery(SpotifyHTTPGet newSpotifyHTTPGet) {
        spotifyHTTPGet = newSpotifyHTTPGet;
    }

    public ArrayList<AvailableTrack> get(String query) throws SpotifyQueryNetworkException, SpotifyJSONParseException,
            UnsupportedEncodingException {
        return ParseJSONResponse(spotifyHTTPGet.get(URLEncoder.encode(query, "UTF-8")));
    }

    private ArrayList<AvailableTrack> ParseJSONResponse(String response)
            throws SpotifyJSONParseException {
        ArrayList<AvailableTrack> tracks = new ArrayList<AvailableTrack>();
        try {
            JSONObject jsonObject;
            jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("tracks");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonTrack = jsonArray.getJSONObject(i);
                String title = jsonTrack.getString("name");
                String link = jsonTrack.getString("href");
                String album = jsonTrack.getJSONObject("album").getString(
                        "name");
                String artist = jsonTrack.getJSONArray("artists")
                        .getJSONObject(0).getString("name");
                tracks.add(new AvailableTrack(title, album, artist, link));
            }
        } catch (JSONException e) {
            Log.d("SpotifyQuery", "Failed to parse Spotify JSON response");
            throw new SpotifyJSONParseException(e);
        }
        Log.i("SpotifyQuery", "Returning " + tracks.size() + " results");
        return tracks;
    }
}
