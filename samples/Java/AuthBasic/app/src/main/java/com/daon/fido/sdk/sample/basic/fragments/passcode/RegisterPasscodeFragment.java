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

package com.daon.fido.sdk.sample.basic.fragments.passcode;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.daon.fido.sdk.sample.basic.R;
import com.daon.sdk.authenticator.Authenticator;
import com.daon.sdk.authenticator.controller.AuthenticatorError;
import com.daon.sdk.authenticator.util.Strings;

public class RegisterPasscodeFragment extends BasePasscodeFragment {
    private EditText passcodeEditText;
    private EditText confirmEditText;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.daon_register_passcode, container, false);

        if (rootView != null) {
            passcodeEditText = rootView.findViewById(R.id.pin);

            confirmEditText = rootView.findViewById(R.id.confirm);
            confirmEditText.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    enrol();
                }
                return false;
            });
        }

        return rootView;
    }

    @Override
    protected void start() {
        super.start();

        // Set edit passcode restrictions - requires state information so must be done here
        setPasscodeEditTextRestrictions(passcodeEditText);
        setPasscodeEditTextRestrictions(confirmEditText);
    }

    @Override
    protected EditText getPrimaryPasscodeEditText() {
        return passcodeEditText;
    }

    protected void reset() {
        if (passcodeEditText != null && confirmEditText != null) {
            passcodeEditText.setEnabled(true);
            passcodeEditText.setText("");

            confirmEditText.setEnabled(true);
            confirmEditText.setText("");

            passcodeEditText.requestFocus();
        }
    }

    protected void enrol() {
        if (passcodeEditText != null && confirmEditText != null && getController() != null) {
            passcodeEditText.setEnabled(false);
            confirmEditText.setEnabled(false);

            if (!Strings.equals(passcodeEditText.getText(), confirmEditText.getText())) {
                showMessage(getString(R.string.passcode_mismatch));
            } else if (passcodeEditText.getText().length() == 0) {
                showMessage(getString(R.string.passcode_empty));
            } else {
                AuthenticatorError error = getController().registerPasscode(Strings.toCharArray(passcodeEditText.getText()), new DefaultCaptureCompleteListener());
                if (error != null) {
                    showMessage(error.getMessage());
                } else {
                    if (getController().getType() == Authenticator.Type.ADOS) {
                        onAuthenticateWait(true);
                    }
                    return;
                }
            }
            reset();
        }
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.passcode_enroll_failed;
    }

    @Override
    protected int getCaptureSuccessMessageId() {
        return R.string.passcode_enroll_complete;
    }
}
