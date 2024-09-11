package com.daon.fido.sdk.sample.basic.network.tasks;

import com.daon.fido.client.sdk.ErrorInfo;
import com.daon.fido.client.sdk.IXUAFCommServiceListener;
import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.model.CreateSession;
import com.daon.fido.sdk.sample.basic.model.CreateSessionResponse;
import com.daon.fido.sdk.sample.basic.network.HTTP;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;

public class UserLoginWithFIDOTask extends TaskExecutor<ServerCommResult> {
    private static final String SERVER_RESOURCE_SESSIONS = "sessions";
    private final CreateSession createSession;
    private final HTTP http;
    private final IXUAFCommServiceListener listener;
    private final CreateSessionListener createSessionListener;

    public UserLoginWithFIDOTask(HTTP http, IXUAFCommServiceListener commServiceListener, String fidoAuthResponse, String authRequestId, CreateSessionListener createSessionListener) {
        this.http = http;
        this.listener = commServiceListener;
        this.createSessionListener = createSessionListener;

        createSession = new CreateSession();
        createSession.setFidoAuthenticationResponse(fidoAuthResponse);
        createSession.setAuthenticationRequestId(authRequestId);
    }

    @Override
    protected ServerCommResult doInBackground() {
        ServerCommResult response = new ServerCommResult();
        try {
            CreateSessionResponse createSessionResponse = http.post(SERVER_RESOURCE_SESSIONS, createSession, CreateSessionResponse.class);
            createSessionListener.setCreateSessionResponse(createSessionResponse);
            createSessionListener.setSessionId(createSessionResponse.getSessionId());
            response.setResponse(createSessionResponse.getFidoAuthenticationResponse());
            response.setResponseCode(createSessionResponse.getFidoResponseCode().shortValue());
            response.setResponseMessage(createSessionResponse.getFidoResponseMsg());
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

    public interface CreateSessionListener {
        void setCreateSessionResponse(CreateSessionResponse response);
        void setSessionId(String sessionId);
    }
}
