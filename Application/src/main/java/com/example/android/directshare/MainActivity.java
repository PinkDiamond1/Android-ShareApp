/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.directshare;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import org.ethereum.geth.Geth;

/**
 * Provides the landing screen of this sample. There is nothing particularly interesting here. All
 * the codes related to the Direct Share feature are in {@link SampleChooserTargetService}.
 */
public class MainActivity extends Activity {

    private EditText mEditBody;
    private int REQUEST_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getBoolean("firstTime", false)) {

            org.ethereum.geth.AccountManager accountManager = Geth.newAccountManager(getFilesDir().toString() + "/data", Geth.LightScryptN, Geth.LightScryptP);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);

            try {
                org.ethereum.geth.Account newAcc = accountManager.newAccount("pass");
                //Toast.makeText(this, "New: " + newAcc.getAddress().getHex(), Toast.LENGTH_SHORT).show();
                editor.putString("selectedProfile", newAcc.getAddress().getHex());
            } catch (java.lang.Exception e) {
                Log.e("OpenShare", "Error", e);
                Toast.makeText(this, "ERROR AM" + e.toString(), Toast.LENGTH_SHORT).show();
            }

            /*byte[] jsonAcc = am.exportKey(newAcc, "Creation password", "Export password");
            Toast.makeText(this, "Json: " + new String(jsonAcc), Toast.LENGTH_SHORT).show();

            am.deleteAccount(newAcc, "Creation password");
            Toast.makeText(this, "Accs: " + am.getAccounts().size(), Toast.LENGTH_SHORT).show();

            Account impAcc = am.importKey(jsonAcc, "Export password", "Import password");
            Toast.makeText(this,"Imp: " + impAcc.getAddress().getHex(), Toast.LENGTH_SHORT).show();
            */

            editor.commit();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(prefs.getString("selectedProfile", "error"));
        setActionBar(toolbar);
        mEditBody = (EditText) findViewById(R.id.body);
        findViewById(R.id.share).setOnClickListener(mOnClickListener);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            } else {
                // User refused to grant permission.
            }
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.share:
                    share();
                    break;
            }
        }
    };

    /**
     * Emits a sample share {@link Intent}.
     */
    private void share() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mEditBody.getText().toString());
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.send_intent_title)));
    }

}
