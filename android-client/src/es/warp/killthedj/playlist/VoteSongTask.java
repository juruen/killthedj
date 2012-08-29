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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;
import es.warp.killthedj.R;
import es.warp.killthedj.server.ServerRequest;
import es.warp.killthedj.server.ServerRequestException;
import es.warp.killthedj.track.AvailableTrack;
import es.warp.killthedj.util.DialogHelper;

public class VoteSongTask extends AsyncTask<Void, Void, Boolean> {
    private final AlertDialog dialog;
    private final boolean type;
    private final AvailableTrack track;
    private ProgressDialog progressDialog;
    private String errorMessage;
    private final PlaylistActivity activity;

    public VoteSongTask(AlertDialog voteDialog, boolean type, AvailableTrack track, PlaylistActivity activity) {
        super();
        this.dialog = voteDialog;
        this.type = type;
        this.track = track;
        this.activity = activity;
        System.out.println("the vote is " + track.getVote());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog = new ProgressDialog(dialog.getContext());
        int dialogMessage = type ? R.string.sending_vote_positive : R.string.sending_vote_negative;
        this.progressDialog.setMessage(dialog.getContext().getString(dialogMessage));
        this.progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... nothing) {
        try {
            new ServerRequest(dialog.getContext().getContentResolver()).vote(type, track);
            return true;
        } catch (ServerRequestException e) {
            errorMessage = e.getMessage();
        }
        return this.isCancelled();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (this.isCancelled())
            return;
        super.onPostExecute(result);
        progressDialog.dismiss();
        dialog.dismiss();
        if (result) {
            Toast t = Toast.makeText(dialog.getContext(), dialog.getContext().getString(R.string.vote_done), Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            activity.update();
        } else {
            String message = dialog.getContext().getString(R.string.error_voting_song) + " ("+errorMessage+")";
            DialogHelper.errorMessage(dialog.getContext(), message);
        }

    }
}
