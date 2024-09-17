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

package com.daon.fido.sdk.sample.basic.ui.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.daon.fido.client.sdk.AuthenticationEventListener;
import com.daon.fido.client.sdk.Fido;
import com.daon.fido.client.sdk.IXUAFPolicyAuthListListener;
import com.daon.fido.client.sdk.core.Error;
import com.daon.fido.client.sdk.model.AccountInfo;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.client.sdk.model.AuthenticatorReg;
import com.daon.fido.sdk.sample.basic.R;
import com.daon.fido.sdk.sample.basic.databinding.ActivityHomeBinding;
import com.daon.fido.sdk.sample.basic.network.tasks.AccountDeleteTask;
import com.daon.fido.sdk.sample.basic.network.tasks.UserLogoutTask;
import com.daon.fido.sdk.sample.basic.preferences.SharedPreferencesManager;
import com.daon.fido.sdk.sample.basic.ui.authenticators.AuthenticatorsActivity;
import com.daon.fido.sdk.sample.basic.ui.chooser.account.ChooseAccountDialogFragment;
import com.daon.fido.sdk.sample.basic.ui.chooser.authenticator.ChooseAuthenticatorDialogFragment;
import com.daon.fido.sdk.sample.basic.ui.intro.IntroActivity;
import com.daon.fido.sdk.sample.basic.util.AuthenticatorUtil;

import java.util.Date;

public class HomeActivity extends AppCompatActivity implements AccountDeleteTask.AccountDeleteResultListener {

    private SharedPreferencesManager sharedPreferencesManager;
    private ActivityHomeBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        sharedPreferencesManager = new SharedPreferencesManager();

        viewBinding.stepUpAuthButton.setOnClickListener(view -> {
            showProgress(true);
            checkForRegistrations();
        });

        viewBinding.authenticatorsButton.setOnClickListener(view -> {
            Intent newIntent = new Intent(this, AuthenticatorsActivity.class);
            startActivity(newIntent);
        });

        viewBinding.deleteButton.setOnClickListener(view -> deleteAccount());

        // Set the info fields on the screen
        viewBinding.textViewUser.setText(sharedPreferencesManager.getStringData(this, SharedPreferencesManager.SHARED_PREF_EMAIL));
        if (getIntent().getExtras() != null && !getIntent().getExtras().isEmpty()) {
            String lastLoggedIn = getIntent().getExtras().getString("LAST_LOGGED_IN");
            viewBinding.textViewLastLoggedInDate.setText(lastLoggedIn);
            String loggedInWith = getIntent().getExtras().getString("LOGGED_IN_WITH");
            viewBinding.textViewLastLoggedInWith.setText(loggedInWith);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Back is pressed... Finishing the activity
                new UserLogoutTask(getApplicationContext()).execute();
                finish();
            }
        });

    }

    private void checkForRegistrations() {
        Fido.getInstance(getApplicationContext()).checkForRegistrations(sharedPreferencesManager.getStringData(this, SharedPreferencesManager.SHARED_PREF_EMAIL), new IXUAFPolicyAuthListListener() {
            @Override
            public void onPolicyAuthListAvailable(AuthenticatorReg[] authenticatorRegs) {
                showProgress(false);
                if (AuthenticatorUtil.registeredAuthsPresent(authenticatorRegs)) {
                    attemptAuthentication();
                } else {
                    endProgressWithError("No suitable FIDO authenticator was found");
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                showProgress(false);
                endProgressWithError(errorMessage);
            }
        });
    }

    private void attemptAuthentication() {
        showProgress(true);
        String email = sharedPreferencesManager.getStringData(this, SharedPreferencesManager.SHARED_PREF_EMAIL);
        String description = email + " Step-up " + new Date();
        Fido.getInstance(getApplicationContext()).authenticateWithUserNameAndDescription(email, description, null, authenticationEventListener);
    }

    final AuthenticationEventListener authenticationEventListener = new AuthenticationEventListener() {
        @Override
        public void onAuthListAvailable(Authenticator[][] authenticators) {
            if (authenticators.length == 1) {
                Fido.getInstance(getApplicationContext()).submitUserSelectedAuth(authenticators[0]);
            } else {
                showAuthenticatorListFragment(authenticators);
            }
        }

        @Override
        public void onAuthenticationComplete() {
            showProgress(false);
            Toast.makeText(HomeActivity.this, R.string.transaction_validation_success, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onAuthenticationFailed(Error error) {
            endProgressWithError(error.getMessage());
        }

        @Override
        public void onAccountListAvailable(AccountInfo[] accountInfos) {
            showAccountChooserFragment(accountInfos);
        }
    };

    private void showAccountChooserFragment(AccountInfo[] accountInfos) {
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

    private void showAuthenticatorListFragment(Authenticator[][] authenticators) {
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

    protected void endProgressWithError(String errorMsg) {
        new Handler(Looper.getMainLooper()).post(() -> showProgress(false));
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
    }

    private void deleteAccount() {
        showProgress(true);

        String email = sharedPreferencesManager.getStringData(this, SharedPreferencesManager.SHARED_PREF_EMAIL);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Reset");
        alertDialogBuilder.setMessage("Are you sure?");
        alertDialogBuilder.setPositiveButton(R.string.dialog_logout_confirmation_yes, (arg0, arg1) -> new AccountDeleteTask(getApplicationContext(), email, this).execute());
        alertDialogBuilder.setNegativeButton(R.string.dialog_logout_confirmation_no, (dialog, which) -> {});
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void showProgress(boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        viewBinding.homeForm.setVisibility(show ? View.GONE : View.VISIBLE);
        viewBinding.homeForm.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewBinding.homeForm.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        viewBinding.homeProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        viewBinding.homeProgress.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewBinding.homeProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onDeregisterComplete() {
        showProgress(false);
        Intent newIntent = new Intent(this, IntroActivity.class);
        startActivity(newIntent);
        finish();
    }

    @Override
    public void onDeregisterFailed(String errorMessage) {
        runOnUiThread(() -> {
            showProgress(false);
            Toast.makeText(this, "Reset failed: " + errorMessage, Toast.LENGTH_LONG).show();
        });
    }
}
