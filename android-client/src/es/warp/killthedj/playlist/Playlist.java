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
package es.warp.killthedj.playlist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import es.warp.killthedj.track.AvailableTrack;

public class Playlist implements Iterable<AvailableTrack> {
    private final List<AvailableTrack> tracks = new ArrayList<AvailableTrack>();

    public Playlist(JSONArray json) throws JSONException {
        for (int i = 0; i < json.length(); i++) {
            tracks.add(new AvailableTrack(json.getJSONObject(i)));
        }
    }

    public Iterator<AvailableTrack> iterator() {
        return tracks.iterator();
    }

    public int size() {
        return tracks.size();
    }
}
