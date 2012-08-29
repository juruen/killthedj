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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import es.warp.killthedj.R;

public class MyRequestsActivity extends Activity  {
    private final Context context = this;
    private ListView listAddedView;
    private ListView listPlayedView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_requests);

        listAddedView = (ListView) findViewById(R.id.my_requests_added);
        listAddedView.setAdapter(new MyRequestsAddedAdapter(context));

        listPlayedView = (ListView) findViewById(R.id.my_requests_played);
        listPlayedView.setAdapter(new MyRequestsPlayedAdapter(context));
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
        new FetchMyRequestsTask((MyRequestsAddedAdapter)listAddedView.getAdapter(),
                (MyRequestsPlayedAdapter)listPlayedView.getAdapter()).execute();
    }
}
