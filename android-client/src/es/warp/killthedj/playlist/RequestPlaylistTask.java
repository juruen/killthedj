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

import android.app.ProgressDialog;
import android.os.AsyncTask;
import es.warp.killthedj.R;
import es.warp.killthedj.server.ServerRequest;
import es.warp.killthedj.server.ServerRequestException;
import es.warp.killthedj.track.AvailableTrack;
import es.warp.killthedj.util.DialogHelper;

public class RequestPlaylistTask extends AsyncTask<Void, Void, Playlist> {
    private final PlaylistAdapter adapter;
    private ProgressDialog progressDialog;
    private String errorMessage;

    public RequestPlaylistTask(PlaylistAdapter adapter) {
        super();
        this.adapter = adapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog = new ProgressDialog(adapter.getContext());
        this.progressDialog.setMessage(adapter.getContext().getString(R.string.requesting_playlist));
        this.progressDialog.show();
    }

    @Override
    protected Playlist doInBackground(Void... nothing) {
        try {
            return new ServerRequest(adapter.getContext().getContentResolver()).getPlaylist();
        } catch (ServerRequestException e) {
            errorMessage = e.getMessage();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Playlist result) {
        if (this.isCancelled())
            return;
        super.onPostExecute(result);
        adapter.clear();
        if (result == null) {
            // Error happened
            progressDialog.dismiss();
            String message = adapter.getContext().getString(R.string.error_request_playlist) + " ("+errorMessage+")";
            DialogHelper.errorMessage(adapter.getContext(), message);
        } else {
            // Ok, we got playlist
            for (AvailableTrack at : result)
                adapter.add(at);
            adapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }
    }
}
