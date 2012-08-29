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
package es.warp.killthedj.current;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.TextView;
import es.warp.killthedj.R;
import es.warp.killthedj.server.ServerRequest;
import es.warp.killthedj.server.ServerRequestException;
import es.warp.killthedj.track.AvailableTrack;
import es.warp.killthedj.util.DialogHelper;

public class FetchCurrentTrackTask extends AsyncTask<Void, Void, AvailableTrack> {
    private final Context context;
    private final Activity activity;
    private ProgressDialog progressDialog;
    private String errorMessage;
    private Bitmap bitmap;

    public FetchCurrentTrackTask(Context context, Activity activity) {
        super();
        this.context = context;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog = new ProgressDialog(context);
        this.progressDialog.setMessage(context.getString(R.string.fetching_current));
        this.progressDialog.show();
    }

    @Override
    protected AvailableTrack doInBackground(Void... nothing) {
        try {
            ServerRequest server = new ServerRequest(activity.getContentResolver());
            bitmap = server.getCurrentCover();
            return server.getCurrentTrack();
        } catch (ServerRequestException e) {
            errorMessage = e.getMessage();
        }
        return null;
    }

    @Override
    protected void onPostExecute(AvailableTrack track) {
        if (this.isCancelled()) return;
        super.onPostExecute(track);
        if (track == null) {
            // Error happened
            progressDialog.dismiss();
            String message = context.getString(R.string.error_current_song) + " ("+errorMessage+")";
            DialogHelper.errorMessage(context, message);
        } else {
            progressDialog.dismiss();
            ((ImageView)activity.findViewById(R.id.currentTracksCover)).setImageBitmap(bitmap);
            ((TextView)activity.findViewById(R.id.textTitle)).setText(track.getTitle());
            ((TextView)activity.findViewById(R.id.textExtra)).setText(track.getExtra());
        }
    }
}
