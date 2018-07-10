package com.panni.scannerhost;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class ConnectedActivity extends AppCompatActivity {

    final public static String CONNECTED_ACTIVITY_KEY_URL = "connected_activity_key_url";
    final public static String CONNECTED_ACTIVITY_KEY_UID = "connected_activity_key_uid";

    final public static int SCAN_NETWORK_TIMEOUT_MS = 30000;

    private String remote_url;
    private String remote_uid;

    private RequestQueue queue;
    private AlertDialog dialog;

    private int acquiredPages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        remote_url = getIntent().getStringExtra(CONNECTED_ACTIVITY_KEY_URL);
        remote_uid = getIntent().getStringExtra(CONNECTED_ACTIVITY_KEY_UID);

        acquiredPages = 0;

        if (remote_url == null || remote_uid == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Application error")
                    .setMessage("There was an expected error in app! Please restart")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish(); // close activity
                        }
                    })
                    .show();
        }

        findViewById(R.id.buttonScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new DoScanTask().execute();
                dialog = new AlertDialog.Builder(ConnectedActivity.this)
                        .setTitle("Contacting server...")
                        .setMessage("Scanning...")
                        .create();
                dialog.show();


                String url = remote_url + "/scan?uid=" + remote_uid;
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            dialog.dismiss();

                            if (!response.equals("-1") && !response.equals("-2")) {
                                Toast.makeText(ConnectedActivity.this, "Scan complete!", Toast.LENGTH_SHORT).show();
                                newPageAcquired();
                            } else {
                                Toast.makeText(ConnectedActivity.this, "There was an error!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();

                            new AlertDialog.Builder(ConnectedActivity.this)
                                    .setTitle("Network error")
                                    .setMessage("Unable to contact remote server")
                                    .show();
                        }
                    }
                );
                stringRequest.setRetryPolicy(
                        new DefaultRetryPolicy(
                                SCAN_NETWORK_TIMEOUT_MS,  // to perform a complete scan
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(stringRequest);
            }
        });
        findViewById(R.id.buttonPdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(remote_url + "/pdf?uid=" + remote_uid));
                startActivity(i);
            }
        });

        queue = Volley.newRequestQueue(this);
    }

    private void newPageAcquired() {
        acquiredPages++;
        ((TextView) findViewById(R.id.numScanView)).setText(String.format(Locale.ITALIAN, "Acquired %d pages", acquiredPages));
    }

    /*private class DoScanTask extends AsyncTask<Void, Void, Boolean> {

        private AlertDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new AlertDialog.Builder(ConnectedActivity.this)
                    .setTitle("Contacting server...")
                    .setMessage("Scanning...")
                    .create();
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String res = "";
            try {
                URL u = new URL(remote_url + "/scan?uid=" + remote_uid);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                res = br.readLine(); // res can be error or path of the image

                br.close();
                conn.disconnect();
            } catch (MalformedURLException e) {
                //e.printStackTrace();
            } catch (IOException e) {
                // log ?
            }

            return !res.equals("-1") && !res.equals("-2");
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            dialog.dismiss();

            if (aBoolean) {
                Toast.makeText(ConnectedActivity.this, "Scan complete!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ConnectedActivity.this, "There was an error!", Toast.LENGTH_LONG).show();
            }
        }
    }*/
}
