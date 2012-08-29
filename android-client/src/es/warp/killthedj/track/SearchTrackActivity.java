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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import es.warp.killthedj.R;
import es.warp.killthedj.util.Helper;

public class SearchTrackActivity extends Activity  {
    public static final List<String> myTracks = new ArrayList<String>();

    private final Context context = this;

    private EditText text;
    private ListView listTracks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_track);

        // Search text widget
        text = (EditText) findViewById(R.id.search_track_text);
        text.setOnEditorActionListener(new OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean actionConsumed = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    actionConsumed = true;
                    performSearch();
                }
                return actionConsumed;
            }
        });

        // List View with the results of the search done
        listTracks = (ListView) findViewById(R.id.listTracks);
        listTracks.setAdapter(new AvailableTracksAdapter(context));
        listTracks.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedTrack((AvailableTrack) view.getTag());
            }
        });
    }

    protected void selectedTrack(final AvailableTrack track) {
        View view = LayoutInflater.from(context).inflate(R.layout.request_dialog, null, false);
        ((TextView) view.findViewById(R.id.textTitle)).setText(track.getTitle());
        ((TextView) view.findViewById(R.id.textExtra)).setText(track.getExtra());
        new AlertDialog.Builder(context).
            setTitle(getString(R.string.request_song)).
            setCancelable(true).
            setPositiveButton(getString(R.string.request), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    performRequest(track);
                }
            }).setView(view).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        int pos = text.getText().length();
        text.setSelection(pos);
    }

    @Override
    public void onPause() {
        super.onPause();
        Helper.hideKeyboard(this, text);
    }

    protected void performRequest(AvailableTrack track) {
        new RequestTrackTask(context).execute(track);
    }

    protected void performSearch() {
        String query = text.getText().toString();
        Helper.hideKeyboard(this, text);
        new SearchTrackTask((AvailableTracksAdapter) listTracks.getAdapter()).execute(query, null, null);
    }
}
