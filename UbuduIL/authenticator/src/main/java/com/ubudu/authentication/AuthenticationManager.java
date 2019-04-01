package com.ubudu.authentication;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ubudu.authentication.error.AuthenticationException;
import com.ubudu.authentication.network.HttpManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mgasztold on 24/02/2017.
 */

public class AuthenticationManager {

    private static final String SERVER_AUTH_URL = "https://manager.ubudu.com/management_app/read_and_write_access_token.json";

    protected SuccessfulLoginListener successfulLoginListener;

    protected FailedLoginListener failedLoginListener;

    protected HttpManager httpManager;

    public AuthenticationManager(HttpManager httpManager) {
        this.httpManager = httpManager;
    }

    public SuccessfulLoginListener getSuccessfulLoginListener() {
        return successfulLoginListener;
    }

    public void setSuccessfulLoginListener(SuccessfulLoginListener successfulLoginListener) {
        this.successfulLoginListener = successfulLoginListener;
    }

    public FailedLoginListener getFailedLoginListener() {
        return failedLoginListener;
    }

    public void setFailedLoginListener(FailedLoginListener failedLoginListener) {
        this.failedLoginListener = failedLoginListener;
    }

    public void login(final String email, final String password) {
        JSONObject authData = new JSONObject();
        try {
            JSONObject userData = new JSONObject();
            userData.put("email", email);
            userData.put("password", password);
            authData.put("user", userData);
        } catch (JSONException e) {
            e.printStackTrace();

            // TODO Create our own Error class and return the correct message / status
        }

        RequestQueue queue = httpManager.getRequestQueue();

        // TODO Disable 'Submit' button and display a spinner somewhere

        JsonObjectRequest authRequest = new JsonObjectRequest(
                Request.Method.POST,
                SERVER_AUTH_URL,
                authData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String authToken = null;
                        try {
                            authToken = response.getString("token");
                        } catch (JSONException e) {
                            e.printStackTrace();

                            // TODO Display an alert to the user and return
                        }
                        User userInfo = new User(email, password, authToken);

                        if (successfulLoginListener != null) {
                            successfulLoginListener.onResponse(userInfo);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (failedLoginListener != null) {
                            if (error.networkResponse != null && error.networkResponse.data != null) {
                                try {
                                    JSONObject response = new JSONObject(new String(error.networkResponse.data));
                                    String message = response.getString("msg");
                                    failedLoginListener.onResponse(new AuthenticationException(message));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    failedLoginListener.onResponse(new AuthenticationException("Malformatted response from the server"));
                                }
                            } else {
                                failedLoginListener.onResponse(new AuthenticationException("No response from the server"));
                            }
                        }
                    }
                });

        queue.add(authRequest);
    }

    public interface SuccessfulLoginListener {

        void onResponse(User userInfo);
    }

    public interface FailedLoginListener {

        void onResponse(AuthenticationException exception);
    }

}
