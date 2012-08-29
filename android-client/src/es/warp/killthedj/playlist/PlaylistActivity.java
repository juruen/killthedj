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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import es.warp.killthedj.R;
import es.warp.killthedj.track.AvailableTrack;

public class PlaylistActivity extends Activity  {
    private final Context context = this;
    private ListView playlistView;
    private AlertDialog voteDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);

        // List View with the playlist
        playlistView = (ListView) findViewById(R.id.playlist);
        playlistView.setAdapter(new PlaylistAdapter(context));
        playlistView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedTrack((AvailableTrack) view.getTag());
            }
        });
    }

    protected void selectedTrack(final AvailableTrack track) {
        View view = LayoutInflater.from(context).inflate(R.layout.vote_dialog, null, false);
        ((TextView) view.findViewById(R.id.textTitle)).setText(track.getTitle());
        ((TextView) view.findViewById(R.id.textExtra)).setText(track.getExtra());
        ((TextView) view.findViewById(R.id.votesValue)).setText(track.getVotes());
        view.findViewById(R.id.voteUp).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                vote(true, track);
            }
        });
        view.findViewById(R.id.voteDown).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                vote(false, track);
            }
        });
        voteDialog = new AlertDialog.Builder(context).
            setTitle(getString(R.string.vote_song)).
            setCancelable(true).
            setView(view).create();
        voteDialog.show();
    }

    protected void vote(boolean type, AvailableTrack track) {
        new VoteSongTask(voteDialog, type, track, this).execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playlist_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                update();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void update() {
        new RequestPlaylistTask((PlaylistAdapter)playlistView.getAdapter()).execute();
    }
}
