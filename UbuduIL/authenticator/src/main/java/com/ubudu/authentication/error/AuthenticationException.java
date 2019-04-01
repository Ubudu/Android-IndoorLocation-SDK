package com.ubudu.authentication.error;

/**
 * Created by matthieu on 20/10/15.
 */
public class AuthenticationException extends Exception {

    private static final String TAG = AuthenticationException.class.getSimpleName();

    public AuthenticationException(String message) {
        super(message);
    }
}
