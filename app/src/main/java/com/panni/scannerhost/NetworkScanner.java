package com.panni.scannerhost;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

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

public class NetworkScanner extends AppCompatActivity {

    private ProgressDialog currentDialog;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_scanner);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        ((EditText) findViewById(R.id.editHost)).setText(PreferenceManager.getDefaultSharedPreferences(this).getString("pref_key_remote_server", "192.168.0.239"));
        ((EditText) findViewById(R.id.editPort)).setText(PreferenceManager.getDefaultSharedPreferences(this).getString("pref_key_remote_server_port", "5000"));

        queue = Volley.newRequestQueue(this);

        findViewById(R.id.buttonConnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String host = ((EditText) findViewById(R.id.editHost)).getText().toString();
                String portS = ((EditText) findViewById(R.id.editPort)).getText().toString();
                int port;

                if (host.equals("") || portS.equals("")) {
                    new AlertDialog.Builder(NetworkScanner.this)
                            .setTitle("Format error")
                            .setMessage("You must provide an hostname and a port!")
                            .show();
                }

                try {
                    port = Integer.parseInt(portS);
                } catch (NumberFormatException ex) {
                    new AlertDialog.Builder(NetworkScanner.this)
                            .setTitle("Format error")
                            .setMessage("Port must be a number")
                            .show();
                    return;
                }


                currentDialog = ProgressDialog.show(NetworkScanner.this, "Contacting server...", "Getting user id...");

                // Volley HTTP Request
                String url = "http://" + host + ":" + port + "/new_session";
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (currentDialog != null && currentDialog.isShowing()) {
                                currentDialog.dismiss();
                            }

                            Intent i = new Intent(NetworkScanner.this, ConnectedActivity.class);
                            i.putExtra(ConnectedActivity.CONNECTED_ACTIVITY_KEY_URL, "http://" + ((EditText) findViewById(R.id.editHost)).getText().toString() + ":" + ((EditText) findViewById(R.id.editPort)).getText().toString());
                            i.putExtra(ConnectedActivity.CONNECTED_ACTIVITY_KEY_UID, response);
                            startActivity(i);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (currentDialog != null && currentDialog.isShowing()) {
                                currentDialog.dismiss();
                            }

                            new AlertDialog.Builder(NetworkScanner.this)
                                    .setTitle("Network error")
                                    .setMessage("Unable to contact remote server")
                                    .show();
                        }
                    }
                );
                queue.add(stringRequest);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.networkscanneractivitymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*private class UUIDGetter extends AsyncTask<Void, Void, String> {

        private String host;
        private int port;

        public UUIDGetter(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String uuid = null;
            try {
                URL u = new URL("http://" + host + ":" + port + "/new_session");
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                uuid = br.readLine(); // uuid is the only data sent

                br.close();
                conn.disconnect();
            } catch (MalformedURLException e) {
                //e.printStackTrace();
            } catch (IOException e) {
                // log ?
            }

            return uuid;
        }

        @Override
        protected void onPostExecute(String uuid) {
            super.onPostExecute(uuid);

            if (currentDialog != null && currentDialog.isShowing()) {
                currentDialog.dismiss();
            }

            if (uuid != null) {
                Intent i = new Intent(NetworkScanner.this, ConnectedActivity.class);
                i.putExtra(ConnectedActivity.CONNECTED_ACTIVITY_KEY_URL, "http://" + host + ":" + port);
                i.putExtra(ConnectedActivity.CONNECTED_ACTIVITY_KEY_UID, uuid);
                startActivity(i);
            } else {
                new AlertDialog.Builder(NetworkScanner.this)
                        .setTitle("Network error")
                        .setMessage("Unable to contact remote server")
                        .show();
            }
        }
    }*/
}
