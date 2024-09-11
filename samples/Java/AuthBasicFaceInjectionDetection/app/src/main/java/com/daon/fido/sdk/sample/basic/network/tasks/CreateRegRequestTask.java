package com.daon.fido.sdk.sample.basic.network.tasks;

import android.content.Context;

import androidx.preference.PreferenceManager;

import com.daon.fido.client.sdk.ErrorInfo;
import com.daon.fido.client.sdk.IXUAFCommServiceListener;
import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.model.UafRequestWithPolicy;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.model.CreateRegRequestResponse;
import com.daon.fido.sdk.sample.basic.network.HTTP;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;
import com.google.gson.Gson;

public class CreateRegRequestTask extends TaskExecutor<ServerCommResult> {

    private static final String SERVER_RESOURCE_REG_REQUESTS = "regRequests";
    public static final String KEY_APP_ID = "fidoAppId";

    private final Context context;
    private final HTTP http;
    private final IXUAFCommServiceListener listener;
    private final RegistrationRequestListener registrationRequestListener;

    public CreateRegRequestTask(Context context, HTTP http, IXUAFCommServiceListener commServiceListener, RegistrationRequestListener registrationRequestListener) {
        this.context = context;
        this.http = http;
        this.listener = commServiceListener;
        this.registrationRequestListener = registrationRequestListener;
    }

    @Override
    protected ServerCommResult doInBackground() {
        ServerCommResult response = new ServerCommResult();
        try {
            CreateRegRequestResponse regRequestResponse = http.get(SERVER_RESOURCE_REG_REQUESTS, CreateRegRequestResponse.class);
            registrationRequestListener.setRegRequestId(regRequestResponse.getRegistrationRequestId());
            saveAppID(context, regRequestResponse.getFidoRegistrationRequest());
            response.setResponse(regRequestResponse.getFidoRegistrationRequest());
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
    protected void onPostExecute(ServerCommResult response) {
        listener.onComplete(response);
    }

    private void saveAppID(Context context, String regRequst) {
        UafRequestWithPolicy[] msg = new Gson().fromJson(regRequst, UafRequestWithPolicy[].class);
        msg[0].policy.disallowed = null;
        String appId = msg[0].header.appID;

        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_APP_ID, appId).apply();
    }

    public interface RegistrationRequestListener {
        void setRegRequestId(String regRequestId);
    }
}
