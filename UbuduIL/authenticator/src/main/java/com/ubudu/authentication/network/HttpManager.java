package com.ubudu.authentication.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by mgasztold on 24/02/2017.
 */

public class HttpManager {

    private static HttpManager instance;

    private RequestQueue requestQueue;

    private Context context;

    private HttpManager(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized HttpManager getInstance(Context context) {
        if (instance == null) {
            instance = new HttpManager(context);
        }
        return instance;
    }

    public static HttpManager getInstance() {
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}