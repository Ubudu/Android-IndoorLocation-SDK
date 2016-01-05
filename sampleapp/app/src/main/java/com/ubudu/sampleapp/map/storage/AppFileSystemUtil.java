package com.ubudu.sampleapp.map.storage;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mgasztold on 14/12/15.
 */
public class AppFileSystemUtil {

    private Context mContext;

    /**
     * Constructor
     * @param ctx application's context
     */
    public AppFileSystemUtil(Context ctx){
        mContext = ctx;
    }

    /**
     * Tries to delete the file from the app file system.
     * @param fileName name of the file to be deleted
     * @return true if the file was deleted, false otherwise
     */
    public boolean removeFile(String fileName) {
        if(mContext!=null) {
            File inputFile = new File(mContext.getFilesDir(), fileName);
            return inputFile.delete();
        } else
            return false;
    }

    public boolean fileExists(String fileName){
        File inputFile = new File(mContext.getFilesDir(), fileName);
        return inputFile.exists();
    }

    /**
     * Writes given input stream to file
     * @param inputStream input stream
     * @param fileName file name
     * @param fileLength length of the content
     * @param listener progress listener
     */
    public boolean writeInputStreamToFile(InputStream inputStream, String fileName, int fileLength, WriteInputStreamToFileProgressListener listener) {
        if(mContext!=null) {
            FileOutputStream outputStream = null;
            try {
                removeFile(fileName);
                outputStream = mContext.openFileOutput(fileName, Context.MODE_APPEND);

                int read = 0;
                byte[] bytes = new byte[1024];

                long total = 0;

                while ((read = inputStream.read(bytes)) != -1) {
                    total += read;
                    outputStream.write(bytes, 0, read);
                    if (listener != null)
                        listener.publishFileWritingProgress((int) ((total * 100) / fileLength));
                }

                outputStream.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        } else
            return false;
    }

}
