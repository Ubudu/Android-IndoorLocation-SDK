package com.ubudu.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by mgasztold on 24/02/2017.
 */

public class AuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        AccountAuthenticator authenticator = new AccountAuthenticator(this);
        return authenticator.getIBinder();
    }
}