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
package es.warp.killthedj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button b = (Button)findViewById(R.id.sendPartyId);
        final EditText partyText = (EditText)findViewById(R.id.partyIdText);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTabActivity(partyText.getText().toString());
            }
        });
        partyText.setOnEditorActionListener(new OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean actionConsumed = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    actionConsumed = true;
                    startTabActivity(partyText.getText().toString());
                }
                return actionConsumed;
            }
        });
    }

    protected void startTabActivity(String partyId) {
        Intent intent = new Intent(this, KillTheDjActivity.class);
        intent.putExtra("partyId", partyId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}