package com.ubudu.authentication;

import java.io.Serializable;

/**
 * Created by mgasztold on 24/02/2017.
 */

public class User implements Serializable {

    protected String email;
    protected String password;
    protected String authToken;

    public User(String email, String password, String authToken) {
        this.email = email;
        this.password = password;
        this.authToken = authToken;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getAuthToken() {
        return authToken;
    }
}
