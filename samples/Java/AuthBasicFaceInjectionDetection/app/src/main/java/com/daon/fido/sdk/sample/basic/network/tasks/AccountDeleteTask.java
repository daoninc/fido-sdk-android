package com.daon.fido.sdk.sample.basic.network.tasks;

import android.content.Context;

import com.daon.fido.client.sdk.Fido;
import com.daon.fido.client.sdk.IXUAFDeregisterEventListener;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.model.DeleteAccountResponse;
import com.daon.fido.sdk.sample.basic.network.RPSAService;
import com.daon.fido.sdk.sample.basic.network.ServerOperationResult;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;

/**
 * Represents an asynchronous task used to delete a user account
 */
public class AccountDeleteTask extends TaskExecutor<ServerOperationResult<DeleteAccountResponse>> {

    private final Context applicationContext;
    private final String username;
    private final AccountDeleteResultListener listener;

    public AccountDeleteTask(Context context, String username, AccountDeleteResultListener listener) {

        this.applicationContext = context;
        this.username = username;
        this.listener = listener;
    }

    @Override
    protected ServerOperationResult<DeleteAccountResponse> doInBackground() {
        ServerOperationResult<DeleteAccountResponse> result;
        try {
            RPSAService service = RPSAService.getInstance(applicationContext);
            DeleteAccountResponse response = service.deleteAccount();
            result = new ServerOperationResult<>(response);
        } catch (ServerError e) {
            result = new ServerOperationResult<>(e.getError());
        }
        return result;
    }

    @Override
    protected void onPostExecute(ServerOperationResult<DeleteAccountResponse> response) {
        if (response.isSuccessful()) {
            String appId = Fido.getInstance(applicationContext).getAppID();
            Fido.getInstance(applicationContext).reset(appId, username, new IXUAFDeregisterEventListener() {
                @Override
                public void onDeregistrationComplete() {
                    listener.onDeregisterComplete();
                }

                @Override
                public void onDeregistrationFailed(int errorCode, String errorMessage) {
                   listener.onDeregisterFailed(errorMessage);
                }
            });

        } else {
            listener.onDeregisterFailed(response.getError().getMessage());
        }
    }

    public interface AccountDeleteResultListener {
        void onDeregisterComplete();
        void onDeregisterFailed(String errorMessage);
    }
}
