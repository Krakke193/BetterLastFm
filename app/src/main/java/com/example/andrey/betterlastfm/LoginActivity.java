package com.example.andrey.betterlastfm;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andrey.betterlastfm.loaders.SessionKeyLoader;

/**
 * Created by Andrey on 09.04.2015.
 */
public class LoginActivity extends Activity implements LoaderManager.LoaderCallbacks<String>{
    private final String LOG_TAG = LoginActivity.class.getSimpleName();
    private static final String UiStateKey = "UiStateKey";

    private String username;
    private String password;
    private String apiKey;
    private String apiSignature;

    private SharedPreferences sharedPreferences;

    private EditText mEditTextUserName;
    private EditText mEditTextPassword;
    private TextView mLogin;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("com.example.andrey.betterlastfm",MODE_PRIVATE);
        if (sharedPreferences.contains("username") && sharedPreferences.contains("session_key")){
            Log.d(LOG_TAG, "true!");
            startActivity(
                    new Intent(this, ProfileActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            );
        }

        Log.d(LOG_TAG,sharedPreferences.getString("username", "ERROR NO USERNAME"));
        Log.d(LOG_TAG,sharedPreferences.getString("session_key", "ERROR NO SESSION KEY"));

        mProgress = (ProgressBar) findViewById(R.id.view_progress);
        mEditTextUserName = (EditText) findViewById(R.id.username_login_edit_text);
        mEditTextPassword = (EditText) findViewById(R.id.userpassword_login_edit_text);
        mLogin = (TextView) findViewById(R.id.login_button);

        if (sharedPreferences.contains(Util.USERNAME_KEY) &&
                sharedPreferences.contains(Util.USER_PASSWORD_KEY)) {

            mEditTextUserName.setText(sharedPreferences.getString(Util.USERNAME_KEY, Util.ERROR));
            mEditTextPassword.setText(sharedPreferences.getString(Util.USER_PASSWORD_KEY, Util.ERROR));
        }

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeUiState(true);

                username = mEditTextUserName.getText().toString();
                password = mEditTextPassword.getText().toString();

                if (username.equals("") && password.equals("")){
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.please_username_and_password),
                            Toast.LENGTH_SHORT)
                            .show();
                    changeUiState(false);
                } else if (username.equals("") && !password.equals("")) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.please_username),
                            Toast.LENGTH_SHORT)
                            .show();
                    changeUiState(false);
                } else if (password.equals("") && !username.equals("")) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.please_password),
                            Toast.LENGTH_SHORT)
                            .show();
                    changeUiState(false);
                }

                apiKey = "f445e682840e750fc7c992898e868efb";
                String secret = "5b332291ad05138bd2e441a22262e5b2";
                String method = "auth.getMobileSession";
                String tmp = "api_key" + apiKey + "method" + method + "password" + password
                        + "username" + username + secret;

                apiSignature = Util.md5(tmp);

                Log.d(LOG_TAG, apiSignature);

                initLoader();
                getLoaderManager().getLoader(0).forceLoad();
            }
        });
    }

    private void initLoader(){
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new SessionKeyLoader(this, username, password, apiKey, apiSignature);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        SharedPreferences mPref = getSharedPreferences(
                "com.example.andrey.betterlastfm", Context.MODE_PRIVATE);

        if (data.equals(Util.ERROR)) {
            Toast.makeText(this, "Invalid username / password.", Toast.LENGTH_SHORT).show();
            changeUiState(false);
            return;
        } else if (data.equals(Util.ERROR_NO_INTERNET)){
            Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
            changeUiState(false);
            return;
        } else if (mPref.contains("username")){
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ProfileActivity.class));
        }

        getLoaderManager().getLoader(0).reset();
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(Util.USERNAME_KEY, mEditTextUserName.getText().toString());
        outState.putString(Util.USER_PASSWORD_KEY, mEditTextPassword.getText().toString());

        outState.putBoolean(UiStateKey, mLogin.isEnabled());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mEditTextUserName.setText(savedInstanceState.getString(Util.USERNAME_KEY));
        mEditTextPassword.setText(savedInstanceState.getString(Util.USER_PASSWORD_KEY));

        mLogin.setEnabled(savedInstanceState.getBoolean(UiStateKey));
        if (savedInstanceState.getBoolean(UiStateKey))
            mProgress.setVisibility(View.GONE);
        else
            mProgress.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Util.USERNAME_KEY, mEditTextUserName.getText().toString());
        editor.putString(Util.USER_PASSWORD_KEY, mEditTextPassword.getText().toString());
        editor.apply();
    }

    private void changeUiState(boolean state){
        if (state) {
            mLogin.setEnabled(false);
            mProgress.setVisibility(View.VISIBLE);
        } else {
            mLogin.setEnabled(true);
            mProgress.setVisibility(View.GONE);
        }
    }
}
