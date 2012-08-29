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
package es.warp.killthedj.myrequests;

import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import es.warp.killthedj.R;
import es.warp.killthedj.server.ServerRequest;
import es.warp.killthedj.server.ServerRequestException;
import es.warp.killthedj.track.AvailableTrack;
import es.warp.killthedj.track.SearchTrackActivity;
import es.warp.killthedj.util.DialogHelper;

public class FetchMyRequestsTask extends AsyncTask<Void, Void, Boolean> {
    private ProgressDialog progressDialog;
    private String errorMessage;
    private final MyRequestsAddedAdapter addedAdapter;
    private final MyRequestsPlayedAdapter playedAdapter;
    private List<AvailableTrack> tracks;

    public FetchMyRequestsTask(MyRequestsAddedAdapter addedAdapter, MyRequestsPlayedAdapter playedAdapter) {
        super();
        this.addedAdapter = addedAdapter;
        this.playedAdapter = playedAdapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog = new ProgressDialog(addedAdapter.getContext());
        this.progressDialog.setMessage(addedAdapter.getContext().getString(R.string.requesting_playlist));
        this.progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... nothing) {
        try {
            tracks = new ServerRequest(addedAdapter.getContext().getContentResolver()).getTracks(SearchTrackActivity.myTracks);
            return true;
        } catch (ServerRequestException e) {
            errorMessage = e.getMessage();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (this.isCancelled())
            return;
        super.onPostExecute(result);
        addedAdapter.clear();
        playedAdapter.clear();
        if (!result) {
            // Error happened
            progressDialog.dismiss();
            String message = playedAdapter.getContext().getString(R.string.error_request_playlist) + " ("+errorMessage+")";
            DialogHelper.errorMessage(addedAdapter.getContext(), message);
        } else {
            // Ok, we got tracks
            for (AvailableTrack at : tracks) {
                if (at.added()) {
                    addedAdapter.add(at);
                } else {
                    playedAdapter.add(at);
                }
            }
            addedAdapter.notifyDataSetChanged();
            playedAdapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }
    }
}
