package com.daon.fido.sdk.sample.basic.network.tasks;

import com.daon.fido.client.sdk.ErrorInfo;
import com.daon.fido.client.sdk.IXUAFCommServiceListener;
import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.model.CreateAuthRequestResponse;
import com.daon.fido.sdk.sample.basic.model.CreateTransactionAuthRequest;
import com.daon.fido.sdk.sample.basic.network.HTTP;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;

public class CreateTransactionAuthRequestTask extends TaskExecutor<ServerCommResult> {

    private static final String SERVER_RESOURCE_TRANSACTION_AUTH_REQUESTS = "transactionAuthRequests";
    private final CreateTransactionAuthRequest createTransactionAuthRequest;
    private final HTTP http;
    private final IXUAFCommServiceListener listener;
    private final CreateAuthRequestTask.AuthRequestListener authRequestListener;

    public CreateTransactionAuthRequestTask(HTTP http, IXUAFCommServiceListener commServiceListener, CreateAuthRequestTask.AuthRequestListener authRequestListener) {
        this.http = http;
        this.listener = commServiceListener;
        this.authRequestListener = authRequestListener;
        createTransactionAuthRequest = new CreateTransactionAuthRequest();
        createTransactionAuthRequest.setStepUpAuth(true);
    }

    @Override
    protected ServerCommResult doInBackground() {

        ServerCommResult response = new ServerCommResult();
        try {
            CreateAuthRequestResponse authRequestResponse = http.post(SERVER_RESOURCE_TRANSACTION_AUTH_REQUESTS, createTransactionAuthRequest, CreateAuthRequestResponse.class);
            authRequestListener.setAuthRequestId(authRequestResponse.getAuthenticationRequestId());
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
}
