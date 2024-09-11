package com.daon.fido.sdk.sample.basic.network.tasks;

import android.os.Bundle;

import com.daon.fido.client.sdk.ErrorInfo;
import com.daon.fido.client.sdk.IXUAFCommServiceListener;
import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.model.SubmitFailedAttemptRequest;
import com.daon.fido.sdk.sample.basic.model.SubmitFailedAttemptResponse;
import com.daon.fido.sdk.sample.basic.network.HTTP;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;
import com.daon.sdk.authenticator.VerificationAttemptParameters;

public class SubmitFailedAttemptTask extends TaskExecutor<ServerCommResult> {
    private static final String SERVER_RESOURCE_SUBMIT_FAILED_ATTEMPTS = "failedTransactionData";

    private final SubmitFailedAttemptRequest submitFailedAttemptRequest;
    private final HTTP http;
    private final IXUAFCommServiceListener listener;

    public SubmitFailedAttemptTask(HTTP http, IXUAFCommServiceListener commServiceListener, Bundle params, String mAuthRequestId) {
        this.http = http;
        this.listener = commServiceListener;
        submitFailedAttemptRequest = new SubmitFailedAttemptRequest();
        submitFailedAttemptRequest.setEmailAddress(params.getString(VerificationAttemptParameters.PARAM_USER_ACCOUNT, null));
        submitFailedAttemptRequest.setAttempt(Integer.toString(params.getInt(VerificationAttemptParameters.PARAM_ATTEMPT, Integer.MIN_VALUE)));
        submitFailedAttemptRequest.setAttemptsRemaining(Integer.toString(params.getInt(VerificationAttemptParameters.PARAM_ATTEMPTS_REMAINING, Integer.MIN_VALUE)));
        submitFailedAttemptRequest.setGlobalAttempt(Integer.toString(params.getInt(VerificationAttemptParameters.PARAM_GLOBAL_ATTEMPT, Integer.MIN_VALUE)));
        submitFailedAttemptRequest.setLockStatus(params.getString(VerificationAttemptParameters.PARAM_LOCK_STATUS, null));
        submitFailedAttemptRequest.setErrorCode(Integer.toString(params.getInt(VerificationAttemptParameters.PARAM_ERROR_CODE, 0)));
        submitFailedAttemptRequest.setScore(Double.toString(params.getDouble(VerificationAttemptParameters.PARAM_SCORE, 0)));
        submitFailedAttemptRequest.setUserAuthKeyId(params.getString(VerificationAttemptParameters.PARAM_USER_AUTH_KEY_ID, null));
        submitFailedAttemptRequest.setAuthenticationRequestId(mAuthRequestId);
    }

    @Override
    protected ServerCommResult doInBackground() {
        ServerCommResult response = new ServerCommResult();
        try {
            SubmitFailedAttemptResponse submitFailedAttemptResponse = http.post(SERVER_RESOURCE_SUBMIT_FAILED_ATTEMPTS, submitFailedAttemptRequest, SubmitFailedAttemptResponse.class);
            if (submitFailedAttemptResponse.getFidoAuthenticationResponse() != null) {
                response.setResponse(submitFailedAttemptResponse.getFidoAuthenticationResponse());
            } else {
                response.setResponse(submitFailedAttemptResponse.getFidoAuthenticationRequest());
            }
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
