package com.ubudu.sampleapp.map.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mgasztold on 14/12/15.
 */
public class HttpDownloader {

    private Context mContext;

    private HttpURLConnection connection;

    private URL url;

    /**
     * Constructor
     * @param ctx application's context
     */
    public HttpDownloader(Context ctx){
        mContext = ctx;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Opens http url connection. Connection remains open so after operations are finished closeHttpUrlConnection() must be called.
     * @param mUrl connection URL
     */
    public void openHttpUrlConnection(String mUrl){
        try {
            url = new URL(mUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return true if http url connection is opened, false otherwise
     */
    public boolean connectionOpened(){
        if(connection!=null && url!=null)
            return true;
        else
            return false;
    }

    /**
     * Closes the http url connection if it is opened
     */
    public void closeHttpUrlConnection(){
        if(connection!=null)
            connection.disconnect();
        connection = null;
        url = null;
    }

    /**
     *
     * @return input stream for current http url connection
     */
    public InputStreamWithContentLength getInputStream() {
        try {
            if(connection!=null && url!=null)
                return new InputStreamWithContentLength(url.openStream(),connection.getContentLength());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
