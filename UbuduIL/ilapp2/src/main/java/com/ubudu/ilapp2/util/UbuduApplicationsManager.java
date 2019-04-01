package com.ubudu.ilapp2.util;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bluelinelabs.logansquare.LoganSquare;
import com.ubudu.ilapp2.model.UbuduApplication;
import com.ubudu.ilapp2.model.UbuduApplications;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by mgasztold on 27/02/2017.
 */

public class UbuduApplicationsManager {

    private static final String APPLICATIONS_BASE_URL = "https://manager.ubudu.com/u_applications.json";

    private Context mContext;

    public UbuduApplicationsManager(Context context) {
        mContext = context;
    }

    public void getUbuduApplicationsWithAuthToken(String authToken, final ResponseListener listener) {
        new AsyncTask<String, Void, Void>() {

            @Override
            protected Void doInBackground(String... token) {
                String url = APPLICATIONS_BASE_URL + "?access_token=" + token[0];

                RequestQueue requestQueue = Volley.newRequestQueue(mContext.getApplicationContext());

                JsonObjectRequest authRequest = new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (response != null) {
                                    try {
                                        String responseString = String.valueOf(response);
                                        UbuduApplications ubuduApps = LoganSquare.parse(responseString, UbuduApplications.class);
                                        listener.success(ubuduApps.getApplications());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                listener.error(error.getMessage());
                            }
                        });

                requestQueue.add(authRequest);

                return null;
            }
        }.execute(authToken);
    }

    public interface ResponseListener {
        void success(List<UbuduApplication> applications);
        void error(String message);
    }

}
