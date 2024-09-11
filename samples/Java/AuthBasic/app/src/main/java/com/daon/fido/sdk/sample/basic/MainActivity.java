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

package com.daon.fido.sdk.sample.basic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.daon.fido.client.sdk.Fido;
import com.daon.fido.client.sdk.IXUAF;
import com.daon.fido.client.sdk.IXUAFCommService;
import com.daon.fido.client.sdk.IXUAFInitialiseListener;
import com.daon.fido.client.sdk.core.Error;
import com.daon.fido.sdk.sample.basic.databinding.ActivityMainBinding;
import com.daon.fido.sdk.sample.basic.network.RPSAService;
import com.daon.fido.sdk.sample.basic.ui.intro.IntroActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding viewBinding;

    /** @noinspection SpellCheckingInspection*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        IXUAF fido = Fido.getInstance(getApplicationContext());
        IXUAFCommService commService = RPSAService.getInstance(getApplicationContext());
        Bundle parameters = new Bundle();
        parameters.putString("com.daon.sdk.log", "true");
        parameters.putString("com.daon.sdk.ignoreNativeClients", "true");

        parameters.putString("com.daon.sdk.ados.enabled", "true");
        parameters.putString("com.daon.face.liveness.active.type", "none");
        parameters.putString("com.daon.face.liveness.passive.type", "server");

        if (fido.isInitialised()) {
            startIntroActivity();
        } else {
            showProgress(true);
            fido.initWithService(parameters, new CustomCaptureFragmentFactory(), commService, new IXUAFInitialiseListener() {
                @Override
                public void onInitialiseComplete() {
                    Log.d(TAG, "onInitialiseComplete");
                    showProgress(false);
                    startIntroActivity();
                }

                @Override
                public void onInitialiseFailed(int i, String s) {
                    Log.d(TAG, "onInitialiseFailed:" + s);
                    showProgress(false);
                }

                @Override
                public void onInitialiseWarnings(List<Error> list) {
                    showProgress(false);
                    Log.d(TAG, "onInitialiseWarnings");
                    for (Error warning : list) {
                        Log.d(TAG, "warning :" + warning.getMessage());
                    }
                    startIntroActivity();
                }
            });
        }
    }

    private void startIntroActivity() {
        Intent newIntent = new Intent(MainActivity.this, IntroActivity.class);
        startActivity(newIntent);
        finish();
    }

    private void showProgress(boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        viewBinding.signupProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        viewBinding.signupProgress.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewBinding.signupProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
