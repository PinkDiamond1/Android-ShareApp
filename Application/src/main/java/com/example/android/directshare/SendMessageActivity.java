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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Provides the UI for sharing a text with a {@link ShareContext}.
 */
public class SendMessageActivity extends Activity {

    /**
     * The request code for {@link SelectShareContextActivity}. This is used when the user doesn't select
     * any of Direct Share icons.
     */
    private static final int REQUEST_SELECT_CONTACT = 1;

    /**
     * The text to share.
     */
    private String mBody;

    /**
     * The ID of the context to share the text with.
     */
    private int mContextId;

    // View references.
    private TextView mTextContactName;
    private TextView mTextMessageBody;

    class SendClaim extends AsyncTask<String, Void, Void> {

        public final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient();

        @Override
        protected Void doInBackground(String... params) {

            ShareContext shareContext = ShareContext.byId(Integer.valueOf(params[1]));

            Log.e("OPENSHARE", "INSIDE ASYNC");

            /*
               {
                    "context" : "ethereum",
                    "issued" : "2016-06-21T03:40:19Z",
                    "type" : [
                        "Claim",
                        "Backing"
                    ],
                    "claim" : {
                        "target" : "alamakota",
                        "amount" : 13
                    },
                    "signature" : {
                        "type" : "EthereumSignature.1",
                        "created" : "2016-06-21T03:40:19Z",
                        "creator" : "0xfE02a56127aFfBba940bB116Fa30A3Af10d12f80",
                        "domain" : "ethereum",
                        "nonce" : "783b4dfa",
                        "signatureValue" : "Rxj7Kb/tDbGHFAs6ddHjVLsHDiNyYzxs2MPmNG8G47oS06N8i0Dis5mUePIzII4+p/ewcOTjvH7aJxnKEePCO9IrlqaHnO1TfmTut2rvXxE5JNzur0qoNq2yXl+TqUWmDXoHZF+jQ7gCsmYqTWhhsG5ufo9oyqDMzPoCb9ibsNk="
                    }
                }
            */

            JSONObject body = new JSONObject();

            Log.e("A", params[0]);

            String target;

            Matcher m = Patterns.WEB_URL.matcher(params[0]);
            if (m.find()) {
                target = m.group();
            } else {
                target = "text:base64:" + Base64.encodeToString(params[0].getBytes(), Base64.DEFAULT);
            }

            try {
                JSONArray labels = new JSONArray();
                labels.put(params[3]);

                JSONObject claim = new JSONObject();
                claim.put("target", target);
                claim.put("labels", labels);

                JSONObject signature = new JSONObject();
                signature.put("created", System.currentTimeMillis());
                signature.put("creator", params[2]);
                signature.put("type", shareContext.getSignatureType());
                signature.put("domain", shareContext.getDomain());
                signature.put("nonce", "???");
                signature.put("signatureValue", "");

                JSONArray types = new JSONArray();
                types.put("Claim");
                types.put("Labels");

                body.put("context", shareContext.getIdentifier());
                body.put("issued", System.currentTimeMillis());
                body.put("type", types);
                body.put("claim", claim);
                body.put("signature", signature);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                post("http://10.0.2.2:80/", body.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.e("OPENSHARE", "INSIDE ASYNC END");

            return null;
        }

        String post(String url, String json) throws IOException {
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_message);
        //setTitle(R.string.sending_message);
        // View references.
        mTextContactName = (TextView) findViewById(R.id.contact_name);
        mTextMessageBody = (TextView) findViewById(R.id.message_body);
        // Resolve the share Intent.
        boolean resolved = resolveIntent(getIntent());
        if (!resolved) {
            finish();
            return;
        }
        // Bind event handlers.
        //findViewById(R.id.send).setOnClickListener(mOnClickListener);
        // Set up the UI.
        prepareUi();
        // The contact ID will not be passed on when the user clicks on the app icon rather than any
        // of the Direct Share icons. In this case, we show another dialog for selecting a contact.
        if (mContextId == ShareContext.INVALID_ID) {
            selectContact();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_CONTACT:
                if (resultCode == RESULT_OK) {
                    mContextId = data.getIntExtra(ShareContext.ID, ShareContext.INVALID_ID);
                }
                // Give up sharing the send_message if the user didn't choose a contact.
                if (mContextId == ShareContext.INVALID_ID) {
                    finish();
                    return;
                }
                prepareUi();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Resolves the passed {@link Intent}. This method can only resolve intents for sharing a plain
     * text. {@link #mBody} and {@link #mContextId} are modified accordingly.
     *
     * @param intent The {@link Intent}.
     * @return True if the {@code intent} is resolved properly.
     */
    private boolean resolveIntent(Intent intent) {
        if (Intent.ACTION_SEND.equals(intent.getAction()) &&
                "text/plain".equals(intent.getType())) {
            mBody = intent.getStringExtra(Intent.EXTRA_TEXT);
            mContextId = intent.getIntExtra(ShareContext.ID, ShareContext.INVALID_ID);
            return true;
        }
        return false;
    }

    /**
     * Sets up the UI.
     */
    private void prepareUi() {
        if (mContextId != ShareContext.INVALID_ID) {
            ShareContext shareContext = ShareContext.byId(mContextId);
            ShareContextViewBinder.bind(shareContext, mTextContactName, getApplicationContext());
        }
        mTextMessageBody.setText(mBody);
    }

    /**
     * Delegates selection of a {@Contact} to {@link SelectShareContextActivity}.
     */
    private void selectContact() {
        Intent intent = new Intent(this, SelectShareContextActivity.class);
        intent.setAction(SelectShareContextActivity.ACTION_SELECT_CONTACT);
        startActivityForResult(intent, REQUEST_SELECT_CONTACT);
    }

    public void onSendClick(View view) {
        switch (view.getId()) {
            case R.id.thumbsup:
                send("thumbsup");
                break;
            case R.id.thumbsdown:
                send("thumbsdown");
                break;
        }
    };

    /**
     * Pretends to send the text to the contact. This only shows a dummy message.
     */
    private void send(String type) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String profile = prefs.getString("selectedProfile", "error");
        final String claimType = type;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SendClaim sendClaim = new SendClaim();
                sendClaim.execute(mBody, String.valueOf(mContextId), profile, claimType);
            }
        });

        Toast.makeText(this,
                getString(R.string.message_sent, mBody, ShareContext.byId(mContextId).getName()),
                Toast.LENGTH_SHORT).show();
        finish();
    }

}
