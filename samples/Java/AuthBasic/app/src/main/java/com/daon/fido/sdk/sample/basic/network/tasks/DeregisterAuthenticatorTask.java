package com.daon.fido.sdk.sample.basic.network.tasks;

import android.util.Log;

import com.daon.fido.client.sdk.ErrorInfo;
import com.daon.fido.client.sdk.IXUAFCommServiceListener;
import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.model.AuthenticatorInfo;
import com.daon.fido.sdk.sample.basic.model.GetAuthenticatorResponse;
import com.daon.fido.sdk.sample.basic.model.ListAuthenticatorsResponse;
import com.daon.fido.sdk.sample.basic.network.HTTP;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;

public class DeregisterAuthenticatorTask extends TaskExecutor<ServerCommResult> {
    private static final String TAG = "DeregisterAuthenticator";
    private static final String SERVER_RESOURCE_LIST_AUTHENTICATORS = "listAuthenticators";
    private static final String SERVER_RESOURCE_AUTHENTICATORS = "authenticators";
    private final HTTP http;
    private final IXUAFCommServiceListener listener;
    private final String authenticatorId;
    private final String deviceId;

    public DeregisterAuthenticatorTask(HTTP http, IXUAFCommServiceListener commServiceListener, String authId, String deviceId) {
        this.http = http;
        this.listener = commServiceListener;
        authenticatorId = authId;
        this.deviceId = deviceId;
    }

    @Override
    protected ServerCommResult doInBackground() {
        Log.d(TAG, "doInBackground: ");
        ServerCommResult response = new ServerCommResult();
        boolean found = false;
        String deregRequest = null;
        try {
            ListAuthenticatorsResponse listAuthenticatorsResponse = http.get(SERVER_RESOURCE_LIST_AUTHENTICATORS, ListAuthenticatorsResponse.class);
            AuthenticatorInfo[] authenticatorInfos = listAuthenticatorsResponse.getAuthenticatorInfoList();
            for (AuthenticatorInfo info : authenticatorInfos) {
                if (info.getId().equals(authenticatorId)) {
                    if (info.getDeviceCorrelationId() == null || (info.getDeviceCorrelationId().equals(deviceId) || info.getDeviceCorrelationId().isEmpty())) {
                        found = true;
                        if (info.getStatus().equals("ACTIVE")) {
                            deregRequest = http.deleteResource(SERVER_RESOURCE_AUTHENTICATORS, authenticatorId, true);
                        } else {
                            deregRequest = http.get(SERVER_RESOURCE_AUTHENTICATORS, info.getId(), GetAuthenticatorResponse.class).getAuthenticatorInfo().getFidoDeregistrationRequest();
                        }
                    }
                }
            }
            if (!found) {
                Log.e(TAG, "doInBackground: Authenticator not found on server");
                ErrorInfo errorInfo = new ErrorInfo(99, "Authenticator not found on server");
                response.setErrorInfo(errorInfo);
            } else {
                response.setResponse(deregRequest);
            }
        } catch (ServerError e) {
            Log.e(TAG, "doInBackground: ", e );
            ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
            response.setErrorInfo(errorInfo);
        } catch (CommunicationsException e) {
            Log.e(TAG, "doInBackground: ", e );
            ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
            response.setErrorInfo(errorInfo);
        }
        return response;
    }

    @Override
    protected void onPostExecute(ServerCommResult serverCommResult) {
        listener.onComplete(serverCommResult);
    }
}
