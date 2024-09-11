package com.daon.fido.sdk.sample.basic.network.tasks;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import com.daon.fido.client.sdk.ErrorInfo;
import com.daon.fido.client.sdk.Fido;
import com.daon.fido.client.sdk.IXUAFCommServiceListener;
import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.model.UafRequestWithPolicy;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.model.CreateAuthRequestResponse;
import com.daon.fido.sdk.sample.basic.network.HTTP;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;
import com.google.gson.Gson;

public class CreateAuthRequestTask extends TaskExecutor<ServerCommResult> {

    private static final String SERVER_RESOURCE_AUTH_REQUESTS = "authRequests";
    public static final String KEY_APP_ID = "fidoAppId";

    private final Context context;
    private final HTTP http;
    private final IXUAFCommServiceListener listener;
    private final Bundle authParams;
    private final AuthRequestListener authRequestListener;

    public CreateAuthRequestTask(Context context, HTTP http, IXUAFCommServiceListener listener, Bundle params, AuthRequestListener authRequestListener) {
        this.context = context;
        this.http = http;
        this.listener = listener;
        this.authParams = params;
        this.authRequestListener = authRequestListener;
    }

    @Override
    protected ServerCommResult doInBackground() {
        ServerCommResult response = new ServerCommResult();
        CreateAuthRequestResponse authRequestResponse;
        String userName = authParams != null ? authParams.getString(Fido.IXUAF_SERVICE_PARAM_USERNAME) : null;

        try {
            if (userName != null) {
                String request = SERVER_RESOURCE_AUTH_REQUESTS + "?userId=" + userName;
                authRequestResponse = http.get(request, CreateAuthRequestResponse.class);
            } else {
                authRequestResponse = http.get(SERVER_RESOURCE_AUTH_REQUESTS, CreateAuthRequestResponse.class);
            }
            authRequestListener.setAuthRequestId(authRequestResponse.getAuthenticationRequestId());
            saveAppID(authRequestResponse.getFidoAuthenticationRequest());
            response.setResponse(authRequestResponse.getFidoAuthenticationRequest());
        } catch (ServerError e) {
            ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
            response.setErrorInfo(errorInfo);
        } catch (CommunicationsException e) {
            ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
            response.setErrorInfo(errorInfo);
        }
        return response;
    }

    @Override
    protected void onPostExecute(ServerCommResult serverCommResult) {
        listener.onComplete(serverCommResult);
    }

    private void saveAppID(String regRequest) {
        UafRequestWithPolicy[] msg = new Gson().fromJson(regRequest, UafRequestWithPolicy[].class);
        msg[0].policy.disallowed = null;
        String appId = msg[0].header.appID;

        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_APP_ID, appId).apply();
    }

    public interface AuthRequestListener {
        void setAuthRequestId(String authRequestId);
    }
}
