// Copyright (C) 2022 Daon.
//
// Permission to use, copy, modify, and/or distribute this software for any purpose with or without
// fee is hereby granted.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS
// SOFTWARE INCLUDING ALL IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
// SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL
// DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER
// TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

package com.daon.fido.sdk.sample.basic.ui.intro;

import static com.daon.fido.sdk.sample.basic.preferences.SharedPreferencesManager.SHARED_PREF_EMAIL;
import static com.daon.fido.sdk.sample.basic.preferences.SharedPreferencesManager.SHARED_PREF_IS_FACE_ONLY;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.daon.fido.client.sdk.AuthenticationEventListener;
import com.daon.fido.client.sdk.Fido;
import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.client.sdk.IXUAFCommService;
import com.daon.fido.client.sdk.IXUAFDeregisterEventListener;
import com.daon.fido.client.sdk.IXUAFInitialiseListener;
import com.daon.fido.client.sdk.core.Error;
import com.daon.fido.client.sdk.model.AccountInfo;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.sdk.sample.basic.CustomApplication;
import com.daon.fido.sdk.sample.basic.CustomCaptureFragmentFactory;
import com.daon.fido.sdk.sample.basic.EdgeToEdgeActivity;
import com.daon.fido.sdk.sample.basic.R;
import com.daon.fido.sdk.sample.basic.databinding.ActivityIntroBinding;
import com.daon.fido.sdk.sample.basic.model.AuthenticationMethod;
import com.daon.fido.sdk.sample.basic.model.CreateSessionResponse;
import com.daon.fido.sdk.sample.basic.network.RPSAService;
import com.daon.fido.sdk.sample.basic.network.tasks.UserSignupTask;
import com.daon.fido.sdk.sample.basic.permission.PermissionHelper;
import com.daon.fido.sdk.sample.basic.preferences.SharedPreferencesManager;
import com.daon.fido.sdk.sample.basic.ui.chooser.account.ChooseAccountDialogFragment;
import com.daon.fido.sdk.sample.basic.ui.home.HomeActivity;
import com.daon.fido.sdk.sample.basic.util.AuthenticatorUtil;

import java.text.DateFormat;
import java.util.List;

public class IntroActivity extends EdgeToEdgeActivity implements UserSignupTask.UserSignupResultListener {
    private static final String TAG = IntroActivity.class.getSimpleName();
    private PermissionHelper permissionHelper;
    private SharedPreferencesManager sharedPreferencesManager;
    private ActivityIntroBinding viewBinding;
    private IXUAF fido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        sharedPreferencesManager = new SharedPreferencesManager();
        permissionHelper = new PermissionHelper(this, this::processGrantedPermissions);
        checkPermissions();

        viewBinding.loginFidoButton.setOnClickListener(view -> attemptFIDOLogin());
        viewBinding.newAccountButton.setOnClickListener(view -> signup());
        viewBinding.resetButton.setOnClickListener(view->reset());

        viewBinding.faceOnly.setOnCheckedChangeListener((compoundButton, isChecked) -> sharedPreferencesManager.storeBooleanData(this, SHARED_PREF_IS_FACE_ONLY, isChecked));
        viewBinding.faceOnly.setChecked(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initialize();
    }

    private void initialize() {
        fido = Fido.getInstance(getApplicationContext());
        IXUAFCommService communicationService = RPSAService.getInstance(getApplicationContext());
        Bundle parameters = new Bundle();
        parameters.putString("com.daon.sdk.log", "true");
        parameters.putString("com.daon.sdk.ignoreNativeClients", "true");
        parameters.putString("com.daon.sdk.ados.enabled", "true");

        showProgress(true);

        fido.initWithService(parameters, new CustomCaptureFragmentFactory(), communicationService, new IXUAFInitialiseListener() {
            @Override
            public void onInitialiseComplete() {
                showProgress(false);
                viewBinding.loginFidoButton.setVisibility(View.VISIBLE);
                viewBinding.newAccountButton.setVisibility(View.VISIBLE);
                viewBinding.resetButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onInitialiseFailed(int code, String message) {
                showProgress(false);
                Toast.makeText(getApplicationContext(), "Initialize failed", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onInitialiseWarnings(List<Error> list) {
                Log.d(TAG, "onInitialiseWarnings");
                for (Error warning : list) {
                    Log.d(TAG, "warning :" + warning.getMessage());
                }
            }
        });
    }

    private void checkPermissions() {
        permissionHelper.checkLocationPermission();
        permissionHelper.checkReadPhoneStatePermission();
        permissionHelper.checkAccessWifiPermission();
    }

    private void processGrantedPermissions(String permission, Boolean granted) {
        if (permission.equals(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (!granted) {
                Toast.makeText(this, R.string.permission_msg_location, Toast.LENGTH_LONG).show();
            }
            permissionHelper.checkReadPhoneStatePermission();
        }
        if (permission.equals(android.Manifest.permission.READ_PHONE_STATE)) {
            if (!granted) {
                Toast.makeText(this, R.string.permission_msg_read_phone_state, Toast.LENGTH_LONG).show();
            }
            permissionHelper.checkAccessWifiPermission();
        }
        if (permission.equals(android.Manifest.permission.ACCESS_WIFI_STATE)) {
            if (!granted) {
                Toast.makeText(this, R.string.permission_msg_wifi, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void createAuthChoicesAlertDialog(Authenticator[][] authenticators) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(IntroActivity.this);
        builderSingle.setTitle("Select authenticator:-");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(IntroActivity.this, android.R.layout.select_dialog_singlechoice);

        for (int i = 0; i < authenticators.length; i++) {
            StringBuilder sb = AuthenticatorUtil.getStringBuilder(authenticators, i);
            arrayAdapter.add(sb.toString());
        }

        builderSingle.setNegativeButton("cancel", (dialog, which) -> {
            Fido.getInstance(getApplicationContext()).submitUserSelectedAuth(null);
            dialog.dismiss();
        });

        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> Fido.getInstance(getApplicationContext()).submitUserSelectedAuth(authenticators[which]));
        builderSingle.show();
    }

    private void signup() {
        showProgress(true);
        new UserSignupTask(IntroActivity.this, IntroActivity.this).execute();
    }

    private void reset() {
        showProgress(true);
        fido.reset(new IXUAFDeregisterEventListener() {
            @Override
            public void onDeregistrationComplete() {
                showProgress(false);
                Toast.makeText(getApplicationContext(), "Reset complete", Toast.LENGTH_SHORT).show();
                initialize();
            }

            @Override
            public void onDeregistrationFailed(int code, String message) {
                showProgress(false);
                Toast.makeText(getApplicationContext(), "Reset failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptFIDOLogin() {
        showProgress(true);
        fido.authenticate(null, new AuthenticationEventListener() {
            @Override
            public void onAuthListAvailable(Authenticator[][] authenticators) {
                if (authenticators.length == 1) {
                    Fido.getInstance(getApplicationContext()).submitUserSelectedAuth(authenticators[0]);
                } else {
                    showProgress(false);
                    createAuthChoicesAlertDialog(authenticators);
                }
            }

            @Override
            public void onAccountListAvailable(AccountInfo[] accountInfos) {
                showAccountChooserActivity(accountInfos);
            }

            @Override
            public void onAuthenticationComplete() {
                showProgress(false);
                RPSAService service = RPSAService.getInstance(getApplicationContext());
                showLoggedIn(service.getCreateSessionResponse());
            }

            @Override
            public void onAuthenticationFailed(Error error) {
                showProgress(false);
                endProgressWithError(error.getCode() + ": " + error.getMessage());
            }
        });
    }

    private void showAccountChooserActivity(AccountInfo[] accountInfos) {
        String[] accounts = new String[accountInfos.length];
        for (int i = 0; i < accountInfos.length; i++) {
            accounts[i] = accountInfos[i].getUserName();
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("accounts", accounts);

        ChooseAccountDialogFragment chooseAccountDialogFragment = getChooseAccountDialogFragment(accountInfos, bundle);

        // NOTE!
        // Get current activity. If this is an ADOS authentication, the current activity is the ADOS Capture Activity,
        // since the authenticator is still active. For non-ADOS authentications, the current activity is the IntroActivity.
        //
        // WORKAROUND:
        // Except for the Silent authenticator, the currentActivity will be CaptureActivity, for non ados,
        // the CaptureActivity will be finished before the dialog is shown. So check if the current activity is finishing.

        CustomApplication app = (CustomApplication) getApplication();
        FragmentActivity currentActivity = (FragmentActivity) app.getCurrentActivity();

        FragmentTransaction ft = currentActivity.isFinishing() ?
                getSupportFragmentManager().beginTransaction() :
                currentActivity.getSupportFragmentManager().beginTransaction();

        ft.add(chooseAccountDialogFragment, "ChooseAccount_tag");
        ft.commitAllowingStateLoss();
    }

    private @NonNull ChooseAccountDialogFragment getChooseAccountDialogFragment(AccountInfo[] accountInfos, Bundle bundle) {
        ChooseAccountDialogFragment chooseAccountDialogFragment = new ChooseAccountDialogFragment(selectedAccount -> {
            if (selectedAccount == -1) {
                //This will cancel the authentication.
                Fido.getInstance(getApplicationContext()).submitUserSelectedAccount(null);
            } else {
                Fido.getInstance(getApplicationContext()).submitUserSelectedAccount(accountInfos[selectedAccount]);
            }
        });
        chooseAccountDialogFragment.setArguments(bundle);
        return chooseAccountDialogFragment;
    }

    public void showProgress(boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        viewBinding.introForm.setVisibility(show ? View.GONE : View.VISIBLE);
        viewBinding.introForm.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewBinding.introForm.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        viewBinding.introProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        viewBinding.introProgress.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewBinding.introProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showLoggedIn(CreateSessionResponse response) {
        sharedPreferencesManager.storeStringData(this, SHARED_PREF_EMAIL, response.getEmail());
        Intent newIntent = new Intent(this, HomeActivity.class);
        newIntent.putExtra("LOGGED_IN_WITH", response.getLoggedInWith().toString());
        if (response.getLastLoggedIn() == null) {
            newIntent.putExtra("LAST_LOGGED_IN", getString(R.string.message_first_login));
        } else {
            String dateString = DateFormat.getDateTimeInstance().format(response.getLastLoggedIn());
            newIntent.putExtra("LAST_LOGGED_IN", dateString);
        }
        startActivity(newIntent);
    }

    protected void endProgressWithError(String errorMsg) {
        showProgress(false);
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        viewBinding.introForm.requestFocus();
    }

    @Override
    public void signupSuccess(String email) {
        showProgress(false);
        sharedPreferencesManager.storeStringData(this, SHARED_PREF_EMAIL, email);
        Toast.makeText(this, "Successfully created an account", Toast.LENGTH_LONG).show();

        Intent newIntent = new Intent(this, HomeActivity.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        newIntent.putExtra("LOGGED_IN_WITH", AuthenticationMethod.FIDO_AUTHENTICATION.toString());
        newIntent.putExtra("LAST_LOGGED_IN", this.getString(R.string.message_first_login));
        startActivity(newIntent);
    }

    @Override
    public void signupError(String message) {
        endProgressWithError(message);
    }
}