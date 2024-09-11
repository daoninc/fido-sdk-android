package com.daon.fido.sdk.sample.basic.network.tasks;

import android.content.Context;
import android.widget.Toast;

import com.daon.fido.client.sdk.Fido;
import com.daon.fido.client.sdk.IXUAFDeregisterEventListener;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.R;
import com.daon.fido.sdk.sample.basic.model.AuthenticatorInfo;
import com.daon.fido.sdk.sample.basic.model.GetAuthenticatorResponse;
import com.daon.fido.sdk.sample.basic.network.RPSAService;
import com.daon.fido.sdk.sample.basic.network.ServerOperationResult;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;

public class GetAuthenticatorTask extends TaskExecutor<ServerOperationResult<GetAuthenticatorResponse>> {

    private final Context context;
    private final AuthenticatorInfo selectedAuthenticationInfo;
    private final GetAuthenticatorResultListener listener;

    public GetAuthenticatorTask(Context context, AuthenticatorInfo selectedAuthenticationInfo, GetAuthenticatorResultListener taskListener) {
        this.context = context;
        this.selectedAuthenticationInfo = selectedAuthenticationInfo;
        this.listener = taskListener;
    }

    @Override
    protected ServerOperationResult<GetAuthenticatorResponse> doInBackground() {
        ServerOperationResult<GetAuthenticatorResponse> result;
        try {
            RPSAService service = RPSAService.getInstance(context.getApplicationContext());
            GetAuthenticatorResponse response = service.getAuthenticator(selectedAuthenticationInfo.getId());
            result = new ServerOperationResult<>(response);
        } catch (ServerError e) {
            result = new ServerOperationResult<>(e.getError());
        } catch (CommunicationsException e) {
            result = new ServerOperationResult<>(e.getError());
        }
        return result;
    }

    @Override
    protected void onPostExecute(ServerOperationResult<GetAuthenticatorResponse> result) {
        if (result.isSuccessful()) {
            AuthenticatorInfo authenticatorInfo = result.getResponse().getAuthenticatorInfo();
            Fido.getInstance(context.getApplicationContext()).deregisterWithMessage(authenticatorInfo.getFidoDeregistrationRequest(), new IXUAFDeregisterEventListener() {
                @Override
                public void onDeregistrationComplete() {
                    Toast.makeText(context, R.string.deregistration_complete, Toast.LENGTH_LONG).show();
                    listener.refreshAuthenticators();
                }

                @Override
                public void onDeregistrationFailed(int errorCode, String errorMessage) {
                    Toast.makeText(context, R.string.error_deregistering_authenticator, Toast.LENGTH_LONG).show();
                    listener.refreshAuthenticators();
                }
            });
        } else {
            listener.showProgress(false);
            Toast.makeText(context, result.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public interface GetAuthenticatorResultListener {
        void refreshAuthenticators();

        void showProgress(boolean show);
    }
}
