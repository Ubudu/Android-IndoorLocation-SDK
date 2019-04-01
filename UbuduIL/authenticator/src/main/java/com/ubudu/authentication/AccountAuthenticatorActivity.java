package com.ubudu.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ubudu.authentication.error.AuthenticationException;
import com.ubudu.authentication.network.HttpManager;

/**
 * Created by mgasztold on 24/02/2017.
 * <p>
 * The Authenticator activity.
 * <p>
 * Called by the Authenticator and in charge of identifing the user.
 * <p>
 * It sends back to the Authenticator the result.
 */
public class AccountAuthenticatorActivity extends android.accounts.AccountAuthenticatorActivity {

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String PARAM_USER_PASSWORD = "USER_PASSWORD";

    private final String TAG = this.getClass().getSimpleName();

    private AccountManager accountManager;
    private String authTokenType;

    protected AuthenticationManager authenticationManager;

    // UI Elements
    private TextView emailTextView;
    private TextView passwordTextView;
    private Button signInButton;
    private static int logoDrawableId = R.mipmap.ic_launcher;

    public static void setUILogoDrawableId(int id) {
        logoDrawableId = id;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_authenticator);
        accountManager = AccountManager.get(getBaseContext());
        authenticationManager = new AuthenticationManager(HttpManager.getInstance(this));

        authTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (authTokenType == null)
            authTokenType = AccountGeneral.AUTHTOKEN_TYPE_READ_WRITE;

        initAuthenticationManager();
        initUI();
    }

    protected void initUI() {
        // Retrieve the UI elements
        emailTextView = (TextView) findViewById(R.id.email);
        passwordTextView = (TextView) findViewById(R.id.password);
        signInButton = (Button) findViewById(R.id.email_sign_in_button);
        ImageView logo = (ImageView) findViewById(R.id.imageView);

        logo.setImageDrawable(getResources().getDrawable(logoDrawableId));

        // If an account name is provided, we pre-fill the 'email' field
        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
        if (accountName != null) {
            emailTextView.setText(accountName);
        }

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailTextView.getText().toString();
                final String password = passwordTextView.getText().toString();
                submit(email, password);
            }
        });

        emailTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString();
                String password = passwordTextView.getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    signInButton.setEnabled(false);
                } else {
                    signInButton.setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        passwordTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString();
                String password = passwordTextView.getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    signInButton.setEnabled(false);
                } else {
                    signInButton.setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    protected void submit(String email, String password) {
        authenticationManager.login(email, password);
    }

    protected void initAuthenticationManager() {
        // If no auth token type is provided, we default to 'Read & Write' type
        authTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (authTokenType == null) {
            authTokenType = AccountGeneral.AUTHTOKEN_TYPE_READ_WRITE;
        }

        authenticationManager.setSuccessfulLoginListener(new AuthenticationManager.SuccessfulLoginListener() {
            @Override
            public void onResponse(User userInfo) {
                // TODO Stop the spinner, hide it, re-enable the 'Sign in' button

                String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

                Bundle data = new Bundle();
                data.putString(AccountManager.KEY_ACCOUNT_NAME, userInfo.getEmail());
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                data.putString(AccountManager.KEY_AUTHTOKEN, userInfo.getAuthToken());
                data.putString(PARAM_USER_PASSWORD, userInfo.getPassword());

                final Intent responseIntent = new Intent();
                responseIntent.putExtras(data);

                finishLogin(responseIntent);
            }
        });
        authenticationManager.setFailedLoginListener(new AuthenticationManager.FailedLoginListener() {
            @Override
            public void onResponse(AuthenticationException exception) {
                // TODO Stop the spinner, hide it, re-enable the 'Sign in' button

                displayAlert(exception.getLocalizedMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully created an account
        int REQ_SIGNUP = 1;
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void finishLogin(Intent intent) {
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASSWORD);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = authTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            accountManager.addAccountExplicitly(account, accountPassword, null);
            accountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            accountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    protected void displayAlert(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
