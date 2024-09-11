package com.daon.fido.sdk.sample.basic.network.tasks;

import android.content.Context;

import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.network.RPSAService;
import com.daon.fido.sdk.sample.basic.network.ServerOperationResult;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;

public class UserLogoutTask extends TaskExecutor<ServerOperationResult<Boolean>> {


    private final Context context;

    public UserLogoutTask(Context context) {
        this.context = context;
    }

    @Override
    protected ServerOperationResult<Boolean> doInBackground() {
        ServerOperationResult<Boolean> result;
        try {
            RPSAService service = RPSAService.getInstance(context);
            service.deleteSession();
            result = new ServerOperationResult<>(true);
        } catch (ServerError e) {
            result = new ServerOperationResult<>(e.getError());
        } catch (CommunicationsException e) {
            result = new ServerOperationResult<>(e.getError());
        }

        return result;
    }

    @Override
    protected void onPostExecute(ServerOperationResult<Boolean> response) {
        response.isSuccessful();
    }
}
