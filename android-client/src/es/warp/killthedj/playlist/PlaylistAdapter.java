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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import es.warp.killthedj.R;
import es.warp.killthedj.track.AvailableTrack;

public class PlaylistAdapter extends ArrayAdapter<AvailableTrack> {
    private final static int textViewResourceId = R.layout.playlist_list_item;

    public PlaylistAdapter(Context context) {
        super(context, textViewResourceId, new ArrayList<AvailableTrack>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = ((Activity)this.getContext()).getLayoutInflater();
            row = inflater.inflate(textViewResourceId, parent, false);
        }
        AvailableTrack track = getItem(position);
        ((TextView) row.findViewById(R.id.position)).setText(String.valueOf(position+1));
        ((TextView) row.findViewById(R.id.textTitle)).setText(track.getTitle());
        ((TextView) row.findViewById(R.id.textExtra)).setText(track.getExtra());
        row.setTag(getItem(position));
        return row;
    }
}
