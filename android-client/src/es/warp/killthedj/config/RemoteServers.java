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
package es.warp.killthedj.config;

import java.util.List;

public class RemoteServers {
    static private  String SERVER_URL = "http://192.168.45.59:3000";
    static private String API = "/api/v1/";

    static public String serverURL() {
        return SERVER_URL;
    }

    static public String currentTrackCoverURL(String partyId) {
        return SERVER_URL + "/covers/" + partyId + ".png";
    }

    static public String requestCurrentTrackURL(String partyId) {
        return SERVER_URL + API + partyId + "/current";
    }

    static public String requestTrackURL(String partyId) {
        return SERVER_URL + API + partyId + "/track";
    }

    static public String requestPlaylistURL(String partyId) {
        return SERVER_URL + API + partyId + "/queue";
    }

    static public String voteSongURL(String partyId) {
        return SERVER_URL + API + partyId + "/vote";
    }

    static public String requestTrackStatusURL(String partyId, List<String> tracksIds) {
        String tracks = "/";
        for (String trackId : tracksIds) tracks += trackId + ",";
        return SERVER_URL + API + partyId + "/status" + tracks.substring(0, tracks.length()-1);
    }

}
