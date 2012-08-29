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
package es.warp.killthedj;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import es.warp.killthedj.current.CurrentTrackActivity;
import es.warp.killthedj.myrequests.MyRequestsActivity;
import es.warp.killthedj.playlist.PlaylistActivity;
import es.warp.killthedj.track.SearchTrackActivity;

public class KillTheDjActivity extends TabActivity {

    public static String partyId;
    private TabHost tabHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        partyId = getIntent().getStringExtra("partyId");
        System.out.println(partyId);
        setContentView(R.layout.tabs);
        tabHost = getTabHost();
        setTabs();
    }

    public void setTabs() {
        tabHost.addTab(tabHost.newTabSpec("search")
                .setIndicator(getString(R.string.search_tracks), getResources().getDrawable(R.drawable.icon_search_track_tab))
                .setContent(new Intent(this, SearchTrackActivity.class)));

        tabHost.addTab(tabHost.newTabSpec("playlist")
                .setIndicator(getString(R.string.playlist), getResources().getDrawable(R.drawable.icon_playlist_tab))
                .setContent(new Intent(this, PlaylistActivity.class)));

        tabHost.addTab(tabHost.newTabSpec("current")
                .setIndicator(getString(R.string.current_track), getResources().getDrawable(R.drawable.icon_current_track_tab))
                .setContent(new Intent(this, CurrentTrackActivity.class)));

        tabHost.addTab(tabHost.newTabSpec("mysongs")
                .setIndicator(getString(R.string.my_requests), getResources().getDrawable(R.drawable.icon_mysongs_tab))
                .setContent(new Intent(this, MyRequestsActivity.class)));

        tabHost.setCurrentTab(0);
    }
}