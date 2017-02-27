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
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;

import io.ipfs.api.IPFS;
import io.ipfs.api.JSONParser;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
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
    private Uri mImage;

    /**
     * The ID of the context to share the text with.
     */
    private int mContextId;

    // View references.
    private TextView mTextContactName;
    private TextView mTextMessageBody;

    public class Wrapper
    {
        public Object result;
        public Object[] params;
    }

    class SendImage extends AsyncTask<Object, Void, Wrapper> {


        @Override
        protected Wrapper doInBackground(Object... params) {
            Log.e("OPENSHARE", "INSIDE IMAGE ASYNC");

            IPFS ipfs = new IPFS("beta.userfeeds.io", 5001);
            NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("image", convertImageToByte((Uri) params[0]));
            Wrapper w = new Wrapper();

            try {
                MerkleNode addResult = ipfs.add(file);
                Log.e("OPENSHARE", addResult.toJSONString());
                w.result = addResult.toJSON();
                w.params = params;
                return w;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("SHARE", "SEND_IMAGE", e);
            }

            Log.e("OPENSHARE", "INSIDE IMAGE ASYNC");
            return null;
        }

        public byte[] convertImageToByte(Uri uri){
            byte[] data = null;
            try {
                ContentResolver cr = getBaseContext().getContentResolver();
                InputStream inputStream = cr.openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                data = baos.toByteArray();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(Wrapper w) {

            super.onPostExecute(w);

            Map result = (Map) w.result;

            Log.e("AAAA", JSONParser.toString(w.result));

            SendClaim sendClaim = new SendClaim();
            sendClaim.execute("ipfs:" + result.get("Hash").toString(), (String) w.params[1], (String) w.params[2], (String) w.params[3], (String) w.params[4]);
        }
    }

    class SendClaim extends AsyncTask<String, Void, Void> {

        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

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
                post("https://beta.userfeeds.io:443/", body.toString());
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
            selectContext();
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

        String action = intent.getAction();
        String type = intent.getType();

        mContextId = intent.getIntExtra(ShareContext.ID, ShareContext.INVALID_ID);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
            return true;
        }
        return false;
    }

    void handleSendText(Intent intent) {
        mBody = intent.getStringExtra(Intent.EXTRA_TEXT);
    }

    void handleSendImage(Intent intent) {
        mImage = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
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
     * Delegates selection of a {@Context} to {@link SelectShareContextActivity}.
     */
    private void selectContext() {
        Intent intent = new Intent(this, SelectShareContextActivity.class);
        intent.setAction(SelectShareContextActivity.ACTION_SELECT_CONTACT);
        startActivityForResult(intent, REQUEST_SELECT_CONTACT);
    }

    public void onSendClick(View view) {
        EditText titleEdit = (EditText) findViewById(R.id.claimTitle);
        String title = titleEdit.getText().toString();
        switch (view.getId()) {
            case R.id.thumbsup:
                send(title, "thumbsup");
                break;
            case R.id.thumbsdown:
                send(title, "thumbsdown");
                break;
        }
    }

    /**
     * Pretends to send the text to the contact. This only shows a dummy message.
     */
    private void send(String title, String label) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String profile = prefs.getString("selectedProfile", "error");
        final String claimTitle = title;
        final String claimLabel = label;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mImage != null) {
                    Log.e("NEW TASK", "IMAGE TASK");
                    SendImage sendImage = new SendImage();
                    sendImage.execute(mImage, String.valueOf(mContextId), profile, claimTitle, claimLabel);
                } else {
                    Log.e("NEW TASK", "TEXT TASK");
                    SendClaim sendClaim = new SendClaim();
                    sendClaim.execute(mBody, String.valueOf(mContextId), profile, claimTitle, claimLabel);
                }
            }
        });

        Toast.makeText(this,
                getString(R.string.message_sent, mBody, ShareContext.byId(mContextId).getName()),
                Toast.LENGTH_SHORT).show();
        finish();
    }

}
