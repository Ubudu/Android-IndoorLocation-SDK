package com.ubudu.ilapp2.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by mgasztold on 21/03/16.
 */
public class ToastUtil {

    public static final String TAG = ToastUtil.class.getSimpleName();

    public static void showToast(Context ctx, String msg){
        if(ctx!=null && msg!=null)
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

}
