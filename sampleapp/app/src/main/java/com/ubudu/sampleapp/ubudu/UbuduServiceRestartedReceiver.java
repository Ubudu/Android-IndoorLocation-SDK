package com.ubudu.sampleapp.ubudu;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UbuduServiceRestartedReceiver extends BroadcastReceiver{
	
	private static final String TAG = "UbuduServiceRestartedReceiver";
	
	@SuppressLint("LongLogTag")
	@Override
	public void onReceive(Context context, Intent intent) {

		UbuduManager.getInstance().start();

		android.util.Log.e(TAG, "UbuduServiceRestartedReceiver task completed");

	}

}
