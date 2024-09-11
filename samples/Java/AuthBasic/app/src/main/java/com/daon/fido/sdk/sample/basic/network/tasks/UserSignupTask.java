package com.daon.fido.sdk.sample.basic.network.tasks;

import android.content.Context;

import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.util.TaskExecutor;
import com.daon.fido.sdk.sample.basic.model.CreateAccount;
import com.daon.fido.sdk.sample.basic.network.RPSAService;
import com.daon.fido.sdk.sample.basic.util.Util;

import java.util.Locale;

public class UserSignupTask extends TaskExecutor<ServerCommResult> {
    private final CreateAccount createAccount;
    private final Context context;
    private final UserSignupResultListener listener;

    public UserSignupTask(Context context, UserSignupResultListener listener) {
        this.context = context;
        this.listener = listener;

        createAccount = new CreateAccount();
        createAccount.setFirstName("firstName");
        createAccount.setLastName("lastName");
        createAccount.setEmail(Util.generateEmail());
        createAccount.setPassword("password");
        createAccount.setLanguage(Locale.getDefault().toString());
    }

    @Override
    protected ServerCommResult doInBackground() {
        RPSAService service = RPSAService.getInstance(context.getApplicationContext());
        return service.serviceCreateAccount(createAccount);
    }

    @Override
    protected void onPostExecute(ServerCommResult result) {
        if (result.isSuccessful()) {
            listener.signupSuccess(createAccount.getEmail());
        } else {
            listener.signupError(result.getErrorInfo().getMessage());
        }
    }

    public interface UserSignupResultListener {
        void signupSuccess(String email);

        void signupError(String message);
    }
}
