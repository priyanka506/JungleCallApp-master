package com.plivo.callingapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class CallingActivity extends ActionBarActivity {

    String mobileNumber;
    String uuid;
    String authValue = "MANGFLMGEYYWFMZJIXZW:NmI5Mjk2MDdmZjNjNTAyMTgxMzQwYjQ5NGM5OGU5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        final EditText editText = (EditText) findViewById(R.id.mobileNumber);
        Button button = (Button) findViewById(R.id.call);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobileNumber = editText.getText().toString();
                requestCallPlivo();
            }
        });
    }

    private String callHtppClient(String url, JSONObject jsonObject) {
        HttpPost postMethod = new HttpPost(url);
        postMethod.setHeader("Content-Type", "application/json");

        org.apache.commons.codec.binary.Base64 b = new org.apache.commons.codec.binary.Base64();
        postMethod.setHeader("Authorization", "Basic " + new String(b.encode(authValue.getBytes())));
        DefaultHttpClient mHttpClient = new DefaultHttpClient();
        ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
        HttpResponse resp = null;
        try {
            postMethod.setEntity(new StringEntity(jsonObject.toString()));
            resp = mHttpClient.execute(postMethod);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuffer jsonString = new StringBuffer();
        String responseValue = null;
        if (resp.getEntity() != null) {
            try {
                InputStream content = resp.getEntity().getContent();
                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        content));
                String result = null;
                while ((result = rd.readLine()) != null) {
                    jsonString.append(result);
                }
                responseValue = jsonString.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseValue;
    }

    private String callHtppClientGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-Type", "application/json");

        org.apache.commons.codec.binary.Base64 b = new org.apache.commons.codec.binary.Base64();
        httpGet.setHeader("Authorization", "Basic " + new String(b.encode(authValue.getBytes())));
        HttpResponse resp = null;
        StringBuffer jsonString = new StringBuffer();
        DefaultHttpClient mHttpClient = new DefaultHttpClient();
        String responseValue = null;
        try {
            resp = mHttpClient.execute(httpGet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (resp.getEntity() != null) {
            try {
                InputStream content = resp.getEntity().getContent();
                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        content));
                String result = null;
                while ((result = rd.readLine()) != null) {
                    jsonString.append(result);
                }
                responseValue = jsonString.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseValue;
    }

    private void requestCallPlivo() {
        Toast.makeText(CallingActivity.this,"Calling",Toast.LENGTH_SHORT).show();
        new AsyncTask<Void,Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String url = "https://api.plivo.com/v1/Account/MANGFLMGEYYWFMZJIXZW/Call/";
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("from","919052375077");
                    jsonObject.put("to", mobileNumber);
                    jsonObject.put("answer_url", "http://s3.amazonaws.com/plivosamplexml/fallback_url.xml");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return callHtppClient(url,jsonObject);
            }

            @Override
            protected void onPostExecute(String jsonString) {
                super.onPostExecute(jsonString);
                if(jsonString != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        Toast.makeText(CallingActivity.this,jsonString,Toast.LENGTH_SHORT).show();
                        if(jsonObject.has("error")) {
                            Toast.makeText(CallingActivity.this,jsonObject.get("error").toString(),Toast.LENGTH_SHORT).show();
                        } else {
                            String request_uuid = jsonObject.getString("request_uuid");
                            if(request_uuid != null) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // get call live and then fetch details. so added delay
                                        requestDetails();
                                    }
                                },11000);
                            } else {
                                Toast.makeText(CallingActivity.this,"Error in request",Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    private void requestSpeakPlivo() {
        new AsyncTask<Void,Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String url = "https://api.plivo.com/v1/Account/MANGFLMGEYYWFMZJIXZW/Call/"+uuid+"/Speak/";
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("text", "Welcome to the jungle");
                    jsonObject.put("language", "en-US");
                    jsonObject.put("voice", "WOMAN");
                    jsonObject.put("legs", "both");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return callHtppClient(url, jsonObject);
            }

            @Override
            protected void onPostExecute(String jsonString) {
                super.onPostExecute(jsonString);
                if(jsonString != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        if(jsonObject.has("error")) {
                            Toast.makeText(CallingActivity.this,jsonObject.get("error").toString(),Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CallingActivity.this,jsonString,Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }


    private void requestDetails() {
        new AsyncTask<Void,Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String detailsUrl = "https://api.plivo.com/v1/Account/MANGFLMGEYYWFMZJIXZW/Call/?status=live";
                String details = callHtppClientGet(detailsUrl);
                return details;
            }

            @Override
            protected void onPostExecute(String jsonString) {
                if(jsonString != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        if(jsonObject.has("error")) {
                            Toast.makeText(CallingActivity.this,jsonObject.get("error").toString(),Toast.LENGTH_SHORT).show();
                        } else if(jsonObject.has("calls")) {
                            JSONArray calls = jsonObject.getJSONArray("calls");
                            uuid = (String) calls.get(0);
                            Toast.makeText(CallingActivity.this,jsonString,Toast.LENGTH_SHORT).show();
                            requestSpeakPlivo();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();

    }
}
