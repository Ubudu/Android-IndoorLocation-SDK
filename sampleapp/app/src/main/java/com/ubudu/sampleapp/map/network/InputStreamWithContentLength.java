package com.ubudu.sampleapp.map.network;

import java.io.InputStream;

/**
 * Created by mgasztold on 14/12/15.
 */
public class InputStreamWithContentLength {

    public final InputStream inputStream;
    public final int contentLength;

    public InputStreamWithContentLength(InputStream inputStream, int contentLength){
        this.inputStream = inputStream;
        this.contentLength = contentLength;
    }

}
