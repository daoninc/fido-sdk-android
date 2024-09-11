package com.daon.fido.sdk.sample.basic.network.tasks;

import com.daon.fido.client.sdk.ErrorInfo;
import com.daon.fido.client.sdk.IXUAFCommServiceListener;
import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.model.ValidateTransactionAuth;
import com.daon.fido.sdk.sample.basic.model.ValidateTransactionAuthResponse;
import com.daon.fido.sdk.sample.basic.network.HTTP;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;

public class ValidateTransactionAuthTask extends TaskExecutor<ServerCommResult> {
    private final ValidateTransactionAuth validateTransactionAuth;
    private static final String SERVER_RESOURCE_TRANSACTION_AUTH_VALIDATION = "transactionAuthValidation";
    private final HTTP http;
    private final IXUAFCommServiceListener listener;

    public ValidateTransactionAuthTask(HTTP http, IXUAFCommServiceListener commServiceListener, String authRequest, String authResponse, String username, String authRequestId) {
        this.http = http;
        this.listener = commServiceListener;
        validateTransactionAuth = new ValidateTransactionAuth();
        validateTransactionAuth.setFidoAuthenticationResponse(authResponse);
        if (authRequest != null) {
            validateTransactionAuth.setFidoAuthenticationRequest(authRequest);
            validateTransactionAuth.setEmail(username);
        } else {
            validateTransactionAuth.setAuthenticationRequestId(authRequestId);
        }
    }

    @Override
    protected ServerCommResult doInBackground() {
        ServerCommResult response = new ServerCommResult();
        try {
            ValidateTransactionAuthResponse validateTransactionAuthResponse = http.post(SERVER_RESOURCE_TRANSACTION_AUTH_VALIDATION, validateTransactionAuth, ValidateTransactionAuthResponse.class);
            response.setResponse(validateTransactionAuthResponse.getFidoAuthenticationResponse());
            response.setResponseCode(validateTransactionAuthResponse.getFidoResponseCode().shortValue());
            response.setResponseMessage(validateTransactionAuthResponse.getFidoResponseMsg());
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
}
