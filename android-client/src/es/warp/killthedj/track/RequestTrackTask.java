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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;
import es.warp.killthedj.R;
import es.warp.killthedj.server.ServerRequest;
import es.warp.killthedj.server.ServerRequestException;
import es.warp.killthedj.server.ServerRequestForbiddenException;
import es.warp.killthedj.util.DialogHelper;

public class RequestTrackTask extends AsyncTask<AvailableTrack, Void, Boolean> {
    private final Context context;
    private ProgressDialog progressDialog;
    private String errorMessage;
    private boolean forbidden;
    private int trackId;

    public RequestTrackTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog = new ProgressDialog(context);
        this.progressDialog.setMessage(context.getString(R.string.requesting_song));
        this.progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(AvailableTrack... tracks) {
        try {
            errorMessage = null;
            forbidden = false;
            trackId = new ServerRequest(context.getContentResolver()).requestTrack(tracks[0]);
            return true;
        } catch (ServerRequestException e) {
            errorMessage = e.getMessage();
        } catch (ServerRequestForbiddenException e) {
            forbidden = true;
        }
        return this.isCancelled();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (this.isCancelled())
            return;
        super.onPostExecute(result);
        progressDialog.dismiss();
        if (result) {
            Toast t = Toast.makeText(context, context.getString(R.string.request_done), Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            SearchTrackActivity.myTracks.add(String.valueOf(trackId));
        } else {
            String message;
            if (forbidden) {
                message = context.getString(R.string.forbidden_add_track);
            } else {
                message = context.getString(R.string.error_request_song) + " ("+errorMessage+")";
            }
            DialogHelper.errorMessage(context, message);
        }
    }
}
