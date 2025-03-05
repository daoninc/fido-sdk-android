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

package com.daon.fido.sdk.sample.basic.ui.authenticators;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.daon.fido.client.sdk.Fido;
import com.daon.fido.client.sdk.IXUAFDeregisterEventListener;
import com.daon.fido.client.sdk.IXUAFRegisterEventListener;
import com.daon.fido.client.sdk.core.Error;
import com.daon.fido.client.sdk.core.INotifyUafResultCallback;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.sdk.sample.basic.EdgeToEdgeActivity;
import com.daon.fido.sdk.sample.basic.databinding.ActivityAuthenticatorsBinding;
import com.daon.fido.sdk.sample.basic.preferences.SharedPreferencesManager;
import com.daon.fido.sdk.sample.basic.network.tasks.GetAuthenticatorTask;
import com.daon.fido.sdk.sample.basic.network.tasks.ListAuthenticatorsTask;
import com.daon.fido.sdk.sample.basic.ui.chooser.authenticator.ChooseAuthenticatorDialogFragment;
import com.daon.fido.sdk.sample.basic.util.AuthenticatorUtil;
import com.daon.fido.sdk.sample.basic.R;
import com.daon.fido.sdk.sample.basic.model.AuthenticatorInfo;

import java.util.Arrays;

/**
 * @noinspection SpellCheckingInspection
 */
public class AuthenticatorsActivity extends EdgeToEdgeActivity implements ListAuthenticatorsTask.ListAuthenticatorsResultListener, GetAuthenticatorTask.GetAuthenticatorResultListener {

    private static final String TAG = AuthenticatorsActivity.class.getSimpleName();
    private static final String KEY_APP_ID = "fidoAppId";
    private static final String ARCHIVED_STATUS = "ARCHIVED";
    private AuthenticatorInfo selectedAuthenticationInfo;
    private SharedPreferencesManager sharedPreferencesManager;
    private ActivityAuthenticatorsBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        viewBinding = ActivityAuthenticatorsBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        sharedPreferencesManager = new SharedPreferencesManager();

        viewBinding.deregisterButton.setOnClickListener(view -> attemptDeregister());
        viewBinding.deregisterButton.setEnabled(false);

        viewBinding.registerNewAuthenticatorButton.setOnClickListener(view -> attemptRegistration());

        viewBinding.listViewAuthenticators.setSelector(R.drawable.listitem_background);
        viewBinding.listViewAuthenticators.setOnItemClickListener((parent, view, position, id) -> {

            selectedAuthenticationInfo = (AuthenticatorInfo) view.getTag();
            // If the authenticator is archived on the server and is either not available on this
            // device or is not registered with the user
            // then disable deregistration
            try {
                viewBinding.deregisterButton.setEnabled((Fido.getInstance(getApplicationContext()).isRegistered(selectedAuthenticationInfo.getAaid(), sharedPreferencesManager.getStringData(this, SharedPreferencesManager.SHARED_PREF_EMAIL), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(KEY_APP_ID, null))) || !selectedAuthenticationInfo.getStatus().equals(ARCHIVED_STATUS));
            } catch (Exception e) {
                viewBinding.deregisterButton.setEnabled(false);
            }

        });

        refreshAuthenticators();
    }

    @Override
    public void refreshAuthenticators() {
        Log.d(TAG, "refreshAuthenticators: ");
        showProgress(true);
        viewBinding.deregisterButton.setEnabled(false);
        new ListAuthenticatorsTask(this, this).execute();
    }

    @Override
    public void showProgress(boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        viewBinding.fidoAuthenticatorsForm.setVisibility(show ? View.GONE : View.VISIBLE);
        viewBinding.fidoAuthenticatorsForm.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewBinding.fidoAuthenticatorsForm.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        viewBinding.authenticatorsProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        viewBinding.authenticatorsProgress.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewBinding.authenticatorsProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void showAuthSelection(AuthenticatorInfo[] authenticatorInfoList) {
        Log.d(TAG, "showAuthSelection: " + Arrays.toString(authenticatorInfoList));
        try {
            AuthenticatorsAdapter adapter = new AuthenticatorsAdapter(this, authenticatorInfoList);
            viewBinding.listViewAuthenticators.setAdapter(adapter);
            showProgress(false);
        } catch (Exception e) {
            Log.e(TAG, "showAuthSelection: ", e);
        }
    }

    private void attemptRegistration() {
        Log.d(TAG, "attemptRegistration: ");
        showProgress(true);
        Fido.getInstance(getApplicationContext()).registerWithUsername(sharedPreferencesManager.getStringData(this, SharedPreferencesManager.SHARED_PREF_EMAIL), null, new IXUAFRegisterEventListener() {
            @Override
            public void onAuthListAvailable(Authenticator[][] authenticators) {
                Log.d(TAG, "onAuthListAvailable: ");
                Boolean isFaceOnly = sharedPreferencesManager.getBooleanData(AuthenticatorsActivity.this, SharedPreferencesManager.SHARED_PREF_IS_FACE_ONLY);
                if (isFaceOnly) {
                    //filter only face
                    Fido.getInstance(getApplicationContext()).submitUserSelectedAuth(AuthenticatorUtil.getFilteredAuthenticator(authenticators));
                } else {
                    if (authenticators.length == 1) {
                        Fido.getInstance(getApplicationContext()).submitUserSelectedAuth(authenticators[0]);
                    } else {
                        showAuthenticatorListFragment(authenticators);
                    }
                }
            }

            @Override
            public void onRegistrationComplete() {
                Log.d(TAG, "onRegistrationComplete: ");
                showProgress(false);
                Toast.makeText(AuthenticatorsActivity.this, R.string.registration_complete, Toast.LENGTH_LONG).show();
                refreshAuthenticators();
            }

            @Override
            public void onRegistrationFailed(Error error) {
                Log.e(TAG, "onRegistrationFailed: " + error);
                showProgress(false);
                Toast.makeText(AuthenticatorsActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onExpiryWarning(INotifyUafResultCallback.ExpiryWarning[] expiryWarnings) {
            }

            @Override
            public void onUserLockWarning() {
            }
        });
    }

    private void showAuthenticatorListFragment(Authenticator[][] authenticators) {
        Log.d(TAG, "showAuthenticatorListFragment: ");
        ChooseAuthenticatorDialogFragment chooseAuthenticatorDialogFragment = new ChooseAuthenticatorDialogFragment(position -> {
            if (position == -1) {
                Fido.getInstance(getApplicationContext()).submitUserSelectedAuth(null);
            } else {
                Fido.getInstance(getApplicationContext()).submitUserSelectedAuth(authenticators[position]);
            }
        }, authenticators);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(chooseAuthenticatorDialogFragment, "ChooseAuth_tag");
        ft.commitAllowingStateLoss();
    }

    private void attemptDeregister() {
        Log.d(TAG, "attemptDeregister: ");
        if (this.selectedAuthenticationInfo == null) {
            viewBinding.deregisterButton.setEnabled(false);
            return;
        }

        if (this.selectedAuthenticationInfo.getStatus().equals(ARCHIVED_STATUS)) {
            requestDeregOfInactiveAuth();
        } else {
            if (AuthenticatorUtil.hasAuthenticator(getApplicationContext(), this.selectedAuthenticationInfo.getAaid())) {
                if (this.selectedAuthenticationInfo.isPresentOnDevice()) {
                    deregActiveAuthPresent();
                } else {
                    requestDeregOfActiveAuthPresent();
                }
            } else {
                requestDeregOfActiveAuthNotPresent();
            }
        }
    }

    private void requestDeregOfInactiveAuth() {
        Log.d(TAG, "requestDeregOfInactiveAuth: ");
        String message = getString(R.string.confirm_de_registration_inactive_auth_present);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton(R.string.dialog_confirm_yes, (arg0, arg1) -> deregInactiveAuthPresent());

        alertDialogBuilder.setNegativeButton(R.string.dialog_confirm_cancel, (dialog, which) -> {
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deregInactiveAuthPresent() {
        Log.d(TAG, "deregInactiveAuthPresent: ");
        showProgress(true);
        new GetAuthenticatorTask(this, selectedAuthenticationInfo, this).execute();
    }

    private void deregActiveAuthPresent() {
        Log.d(TAG, "deregActiveAuthPresent: ");
        showProgress(true);

        Fido.getInstance(getApplicationContext()).deregister(selectedAuthenticationInfo.getId(), new IXUAFDeregisterEventListener() {
            @Override
            public void onDeregistrationComplete() {
                Log.d(TAG, "onDeregistrationComplete: ");
                Toast.makeText(AuthenticatorsActivity.this, R.string.deregistration_complete, Toast.LENGTH_LONG).show();
                refreshAuthenticators();
            }

            @Override
            public void onDeregistrationFailed(int i, String s) {
                Log.e(TAG, "onDeregistrationFailed: " + s);
                Toast.makeText(AuthenticatorsActivity.this, R.string.error_deregistering_authenticator, Toast.LENGTH_LONG).show();
                refreshAuthenticators();
            }
        });
    }

    private void requestDeregOfActiveAuthPresent() {
        Log.d(TAG, "requestDeregOfActiveAuthPresent: ");
        String message = getString(R.string.confirm_dereg_active_auth_present);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(R.string.dialog_confirm_yes, (arg0, arg1) -> deregActiveAuthPresent());

        alertDialogBuilder.setNegativeButton(R.string.dialog_confirm_cancel, (dialog, which) -> {
        });
    }

    private void requestDeregOfActiveAuthNotPresent() {
        Log.d(TAG, "requestDeregOfActiveAuthNotPresent: ");
        String message = getString(R.string.confirm_dereg_active_auth_not_present);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(R.string.dialog_confirm_yes, (arg0, arg1) -> deregActiveAuthNotPresent());
        alertDialogBuilder.setNegativeButton(R.string.dialog_confirm_cancel, (dialog, which) -> {
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deregActiveAuthNotPresent() {
        showProgress(true);
        Fido.getInstance(getApplicationContext()).deregister(selectedAuthenticationInfo.getId(), new IXUAFDeregisterEventListener() {
            @Override
            public void onDeregistrationComplete() {
                Log.d(TAG, "onDeregistrationComplete: ");
                Toast.makeText(AuthenticatorsActivity.this, R.string.deregistration_complete, Toast.LENGTH_LONG).show();
                refreshAuthenticators();
            }

            @Override
            public void onDeregistrationFailed(int i, String s) {
                Log.e(TAG, "onDeregistrationFailed: " + s);
                Toast.makeText(AuthenticatorsActivity.this, R.string.error_deregistering_authenticator, Toast.LENGTH_LONG).show();
                refreshAuthenticators();
            }
        });
    }
}
