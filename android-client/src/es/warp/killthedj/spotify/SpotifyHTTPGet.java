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
/**
 *
 */
package es.warp.killthedj.spotify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

/**
 * @author Javier Uruen Val
 *
 */
public class SpotifyHTTPGet {
    final static private String SPOTIFY_URL = "http://ws.spotify.com/search/1/track.json?q=";

    public String get(String query) throws SpotifyQueryNetworkException {
        String url = SPOTIFY_URL + query;
        Log.d("SpotifyHTTPGet", "URL: " + url);
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                Log.e("SpotifyHTTPGet", "URL: " + url);
                Log.e("SpotifyHTTPGet", response.getStatusLine().toString());
                throw new SpotifyQueryNetworkException("Didn't get 200 OK");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (ClientProtocolException e) {
            Log.e("SpotifyHTTPGet", e.getClass().getSimpleName() + ": " + e.getMessage());
            throw new SpotifyQueryNetworkException(e);
        } catch (IOException e) {
            Log.e("SpotifyHTTPGet", e.getClass().getSimpleName() + ": " + e.getMessage());
            throw new SpotifyQueryNetworkException(e);
        }
    }
}
