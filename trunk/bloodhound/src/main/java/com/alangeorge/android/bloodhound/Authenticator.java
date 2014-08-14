package com.alangeorge.android.bloodhound;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

public class Authenticator extends AbstractAccountAuthenticator {
    private static final String TAG = "Authenticator";

    public Authenticator(Context context) {
        super(context);
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        Log.d(TAG, "editProperties(" + response + ", " + accountType + ")");
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "addAccount(" + response + ", " + accountType + ", " + authTokenType + ", " + Arrays.toString(requiredFeatures) + ", " + options + ")");
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "confirmCredentials(" + response + ", " + account + ", " +  options + ")");
        return null;
    }


    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "getAuthToken(" + response + ", " + account + ", " + authTokenType + ", " + options + ")");
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Log.d(TAG, "getAuthTokenLabel(" + authTokenType + ")");
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "updateCredentials(" + response + ", " + account + ", " + authTokenType + ", "  + options + ")");
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        Log.d(TAG, "hasFeatures(" + response + ", " + account + ", " + Arrays.toString(features) + ")");
        return null;
    }
}
