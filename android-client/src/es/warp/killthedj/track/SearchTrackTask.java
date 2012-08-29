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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import es.warp.killthedj.R;
import es.warp.killthedj.spotify.SpotifyJSONParseException;
import es.warp.killthedj.spotify.SpotifyQuery;
import es.warp.killthedj.spotify.SpotifyQueryNetworkException;
import es.warp.killthedj.util.DialogHelper;

public class SearchTrackTask extends AsyncTask<String, Void, ArrayList<AvailableTrack>> {
    private final int CONNECTION_ATTEMPTS = 10;
    private final AvailableTracksAdapter adapter;
    private ProgressDialog progressDialog;

    public SearchTrackTask(AvailableTracksAdapter adapter) {
        super();
        this.adapter = adapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.adapter.clear();
        this.adapter.notifyDataSetChanged();
        this.progressDialog = new ProgressDialog(this.adapter.getContext());
        this.progressDialog.setMessage(this.adapter.getContext().getString(R.string.searching_songs));
        this.progressDialog.show();
    }

    @Override
    protected ArrayList<AvailableTrack> doInBackground(String... strs) {
        for (int i = 0; i < CONNECTION_ATTEMPTS; i++) {
            try {
                Log.i("SearchTrackTask", "Searching " + strs[0]);
                return new SpotifyQuery().get(strs[0]);
            } catch (SpotifyQueryNetworkException e) {
                Log.e("SearchTrackTask", e.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
            } catch (SpotifyJSONParseException e) {
                Log.e("SearchTrackTask", e.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                Log.e("SearchTrackTask", e.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
            }
            if (this.isCancelled()) {
                Log.i("SearchTrackTask", "Task cancelled. Bail out");
                return null;
            }
        }
        DialogHelper.errorMessage(adapter.getContext(), adapter.getContext().getString(R.string.error_search_tracks));
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<AvailableTrack> result) {
        if (this.isCancelled())
            return;
        super.onPostExecute(result);
        adapter.clear();
        for (AvailableTrack at : result)
            adapter.add(at);
        adapter.notifyDataSetChanged();
        progressDialog.dismiss();
    }
}
