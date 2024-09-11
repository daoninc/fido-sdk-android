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
import androidx.fragment.app.FragmentTransaction;

import com.daon.fido.client.sdk.AuthenticationEventListener;
import com.daon.fido.client.sdk.Fido;
import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.client.sdk.IXUAFCommService;
import com.daon.fido.client.sdk.IXUAFInitialiseListener;
import com.daon.fido.client.sdk.core.Error;
import com.daon.fido.client.sdk.model.AccountInfo;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.sdk.sample.basic.CustomCaptureFragmentFactory;
import com.daon.fido.sdk.sample.basic.R;
import com.daon.fido.sdk.sample.basic.databinding.ActivityIntroBinding;
import com.daon.fido.sdk.sample.basic.model.AuthenticationMethod;
import com.daon.fido.sdk.sample.basic.model.CreateSessionResponse;
import com.daon.fido.sdk.sample.basic.network.RPSAService;
import com.daon.fido.sdk.sample.basic.network.tasks.AccountDeleteTask;
import com.daon.fido.sdk.sample.basic.network.tasks.UserSignupTask;
import com.daon.fido.sdk.sample.basic.permission.PermissionHelper;
import com.daon.fido.sdk.sample.basic.preferences.SharedPreferencesManager;
import com.daon.fido.sdk.sample.basic.ui.chooser.account.ChooseAccountDialogFragment;
import com.daon.fido.sdk.sample.basic.ui.home.HomeActivity;
import com.daon.fido.sdk.sample.basic.util.AuthenticatorUtil;

import java.text.DateFormat;
import java.util.List;

public class IntroActivity extends AppCompatActivity implements AccountDeleteTask.AccountDeleteResultListener, UserSignupTask.UserSignupResultListener {
    private static final String TAG = IntroActivity.class.getSimpleName();
    private PermissionHelper permissionHelper;
    private SharedPreferencesManager sharedPreferencesManager;
    private ActivityIntroBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        sharedPreferencesManager = new SharedPreferencesManager();
        permissionHelper = new PermissionHelper(this, this::processGrantedPermissions);
        checkPermissions();

        viewBinding.newAccountButton.setOnClickListener(view -> {
            // Here deleting any previous accounts if created any for the simplicity of the app.
            showProgress(true);
            new AccountDeleteTask(this, this).execute();
        });
        viewBinding.loginFidoButton.setOnClickListener(view -> attemptFIDOLogin());
        viewBinding.faceOnly.setOnCheckedChangeListener((compoundButton, isChecked) -> sharedPreferencesManager.storeBooleanData(this, SHARED_PREF_IS_FACE_ONLY, isChecked));
        viewBinding.faceOnly.setChecked(true);
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

    @Override
    public void onDeregisterComplete() {
        reinitializeSdk();
    }

    @Override
    public void onDeregisterFailed(String errorMessage) {
        runOnUiThread(() -> Toast.makeText(this, "Reset failed: " + errorMessage, Toast.LENGTH_LONG).show());
        reinitializeSdk();
    }

    public void reinitializeSdk() {
        IXUAF fido = Fido.getInstance(getApplicationContext());
        IXUAFCommService communicationService = RPSAService.getInstance(getApplicationContext());
        Bundle parameters = new Bundle();
        parameters.putString("com.daon.sdk.log", "true");
        parameters.putString("com.daon.sdk.ignoreNativeClients", "true");

        parameters.putString("com.daon.sdk.ados.enabled", "true");
        parameters.putString("com.daon.face.liveness.active.type", "none");
        parameters.putString("com.daon.face.liveness.passive.type", "server");

        fido.initWithService(parameters, new CustomCaptureFragmentFactory(), communicationService, new IXUAFInitialiseListener() {
            @Override
            public void onInitialiseComplete() {
                new UserSignupTask(IntroActivity.this, IntroActivity.this).execute();
            }

            @Override
            public void onInitialiseFailed(int i, String s) {
                showProgress(false);
                Log.d(TAG, "onInitialiseFailed");
            }

            @Override
            public void onInitialiseWarnings(List<Error> list) {

            }
        });
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

    private void attemptFIDOLogin() {
        showProgress(true);
        Fido.getInstance(getApplicationContext()).authenticate(null, new AuthenticationEventListener() {
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
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
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