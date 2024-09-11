package com.daon.fido.sdk.sample.basic.network.tasks;

import com.daon.fido.client.sdk.ErrorInfo;
import com.daon.fido.client.sdk.IXUAFCommServiceListener;
import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.model.GetPolicyResponse;
import com.daon.fido.sdk.sample.basic.network.HTTP;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;

public class GetPolicyTask extends TaskExecutor<ServerCommResult> {
    private static final String SERVER_RESOURCE_POLICIES = "policies";
    private final HTTP http;
    private final IXUAFCommServiceListener listener;

    public GetPolicyTask(HTTP http, IXUAFCommServiceListener commServiceListener) {
        this.http = http;
        this.listener = commServiceListener;
    }

    @Override
    protected ServerCommResult doInBackground() {
        ServerCommResult response = new ServerCommResult();
        try {
            GetPolicyResponse policyResponse = http.get(SERVER_RESOURCE_POLICIES, "reg", GetPolicyResponse.class);
            response.setResponse(policyResponse.getPolicyInfo().getPolicy());
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
