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
package es.warp.killthedj.track;


import java.text.DateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class AvailableTrack {
    private final String title;
    private final String album;
    private final String artist;
    private final String link;
    private String id;
    private String votes;
    private String status;
    private Date time;
    private String position;
    private String vote;

    public static final String[] MANDATORY_FIELDS  = {"title", "album", "artist", "link"};

    public AvailableTrack(String title, String album, String artist, String link) {
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.link = link;
    }

    public AvailableTrack(JSONObject json) throws JSONTrackParseException {
        for (String f : MANDATORY_FIELDS) {
            if (!json.has(f))
                throw new JSONTrackParseException("JSON doesn't contain field " + f);
        }

        title  = json.optString("title");
        album  = json.optString("album");
        artist = json.optString("artist");
        link   = json.optString("link");
        id     = json.optString("id");
        votes  = json.optString("votes");
        vote   = json.optString("vote");
        status = json.optString("status");
        time   = new Date(Long.valueOf(json.optString("time")) * 1000);
        position = json.optString("position");
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getLink() {
        return link;
    }

    public String getVotes() {
        return votes;
    }

    public String getVote() {
        return vote;
    }

    public String getId() {
        return id;
    }

    public Date getTime() {
        return time;
    }

    public String getPosition() {
        return position;
    }

    /**
     * Track as Json
     * @return String with the json representation of the track
     * @throws JSONException
     */
    public String toJson() throws JSONException {
        JSONObject jsonTrackContent = new JSONObject();
        jsonTrackContent.put("album", getAlbum());
        jsonTrackContent.put("artist", getArtist());
        jsonTrackContent.put("title", getTitle());
        jsonTrackContent.put("link", getLink());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("track", jsonTrackContent);
        Log.d("JSON", jsonObject.toString());
        return jsonObject.toString();
    }

    /**
     * A vote looks like: {vote => { id => 1, type => 'up' }}
     * @param type Vote up or down
     * @return Json object as string
     * @throws JSONException
     */
    public String voteJson(boolean type) throws JSONException {
        JSONObject vote = new JSONObject();
        vote.put("id", getId());
        vote.put("type", type ? "up" : "down");
        String json = new JSONObject().put("vote", vote).toString();
        return json;
    }

    public String getExtra() {
        return getArtist() + " • " + getAlbum();
    }

    public String getDuration() {
        // TODO library like duration from ruby
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(getTime());
    }

    public boolean played() {
        return status.equals("played");
    }

    public boolean added() {
        return status.equals("added");
    }
}
