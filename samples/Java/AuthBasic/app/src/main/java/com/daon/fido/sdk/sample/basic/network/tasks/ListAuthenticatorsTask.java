package com.daon.fido.sdk.sample.basic.network.tasks;

import android.content.Context;
import android.provider.Settings;
import android.widget.Toast;

import com.daon.fido.client.sdk.Fido;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.model.AuthenticatorInfo;
import com.daon.fido.sdk.sample.basic.model.ListAuthenticatorsResponse;
import com.daon.fido.sdk.sample.basic.network.RPSAService;
import com.daon.fido.sdk.sample.basic.network.ServerOperationResult;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;
import com.daon.fido.sdk.sample.basic.util.AuthenticatorUtil;

/**
 * Represents an asynchronous task used to get a list of authenticators to display
 */
public class ListAuthenticatorsTask extends TaskExecutor<ServerOperationResult<ListAuthenticatorsResponse>> {

    private final Context context;
    private final ListAuthenticatorsResultListener listener;

    public ListAuthenticatorsTask(Context context, ListAuthenticatorsResultListener taskListener) {
        this.context = context;
        this.listener = taskListener;
    }

    @Override
    protected ServerOperationResult<ListAuthenticatorsResponse> doInBackground() {
        ServerOperationResult<ListAuthenticatorsResponse> result;
        try {
            RPSAService rpsaService = RPSAService.getInstance(context.getApplicationContext());
            ListAuthenticatorsResponse response = rpsaService.serviceRequestAuthInfoList();
            result = new ServerOperationResult<>(response);
        } catch (ServerError e) {
            result = new ServerOperationResult<>(e.getError());
        } catch (CommunicationsException e) {
            result = new ServerOperationResult<>(e.getError());
        }

        return result;
    }

    @Override
    protected void onPostExecute(ServerOperationResult<ListAuthenticatorsResponse> result) {
        if (result.isSuccessful()) {
            if (result.getResponse() != null) {
                AuthenticatorInfo[] authenticatorInfoList = result.getResponse().getAuthenticatorInfoList();
                String deviceId = Fido.getInstance(context).getDeviceInfo().getDeviceId();
                listener.showAuthSelection(AuthenticatorUtil.removeAuthenticatorsNotOnThisDevice(deviceId, authenticatorInfoList));
            } else {
                listener.showProgress(false);
            }
        } else {
            listener.showProgress(false);
        }
    }

    public interface ListAuthenticatorsResultListener {
        void showAuthSelection(AuthenticatorInfo[] authenticatorInfoList);
        void showProgress(boolean show);
    }

}
