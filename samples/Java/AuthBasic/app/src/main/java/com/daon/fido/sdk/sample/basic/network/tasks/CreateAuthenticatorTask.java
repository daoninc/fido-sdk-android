package com.daon.fido.sdk.sample.basic.network.tasks;

import com.daon.fido.client.sdk.ErrorInfo;
import com.daon.fido.client.sdk.IXUAFCommServiceListener;
import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.model.CreateAuthenticator;
import com.daon.fido.sdk.sample.basic.model.CreateAuthenticatorResponse;
import com.daon.fido.sdk.sample.basic.network.HTTP;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;

public class CreateAuthenticatorTask extends TaskExecutor<ServerCommResult> {
    private static final String SERVER_RESOURCE_AUTHENTICATORS = "authenticators";

    private final CreateAuthenticator createAuthenticator;
    private final HTTP http;
    private final IXUAFCommServiceListener listener;

    public CreateAuthenticatorTask(HTTP http, IXUAFCommServiceListener commServiceListener, String response, String mRegRequestId) {
        this.http = http;
        this.listener = commServiceListener;

        createAuthenticator = new CreateAuthenticator();
        createAuthenticator.setRegistrationChallengeId(mRegRequestId);
        createAuthenticator.setFidoReqistrationResponse(response);
    }

    @Override
    protected ServerCommResult doInBackground() {
        ServerCommResult response = new ServerCommResult();
        try {
            CreateAuthenticatorResponse authenticatorResponse = http.post(SERVER_RESOURCE_AUTHENTICATORS, createAuthenticator, CreateAuthenticatorResponse.class);
            response.setResponse(authenticatorResponse.getFidoRegistrationConfirmation());
            response.setResponseCode(authenticatorResponse.getFidoResponseCode().shortValue());
            response.setResponseMessage(authenticatorResponse.getFidoResponseMsg());
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
}
