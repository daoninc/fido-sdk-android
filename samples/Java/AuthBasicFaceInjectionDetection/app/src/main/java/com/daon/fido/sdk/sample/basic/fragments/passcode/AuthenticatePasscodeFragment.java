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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daon.fido.sdk.sample.basic.R;
import com.daon.sdk.authenticator.Authenticator;
import com.daon.sdk.authenticator.util.Strings;

public class AuthenticatePasscodeFragment extends BasePasscodeFragment {
    private EditText passcodeEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.daon_authenticate_passcode, container, false);

        if (rootView != null) {
            passcodeEditText = rootView.findViewById(R.id.pin);
            passcodeEditText.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    authenticate();
                }

                return false;
            });
        }

        return rootView;
    }

    @Override
    protected EditText getPrimaryPasscodeEditText() {
        return passcodeEditText;
    }

    @Override
    protected void start() {
        super.start();

        // Set edit passcode restrictions - requires state information so must be done here
        setPasscodeEditTextRestrictions(passcodeEditText);
    }

    protected void reset() {
        if (passcodeEditText != null) {
            passcodeEditText.setEnabled(true);
            passcodeEditText.setText("");
            passcodeEditText.requestFocus();
        }
    }

    protected void authenticate() {
        if (passcodeEditText != null && getController() != null) {
            passcodeEditText.setEnabled(false);
            if (passcodeEditText.getText().length() == 0) {
                showMessage(getString(R.string.passcode_empty));
                reset();
            } else {
                if (getController().getType() == Authenticator.Type.ADOS) {
                    onAuthenticateWait(true);
                }
                getController().authenticatePasscode(Strings.toCharArray(passcodeEditText.getText()), new DefaultCaptureCompleteListener());
            }
        }
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.passcode_verify_failed;
    }

    @Override
    protected int getCaptureSuccessMessageId() {
        return R.string.passcode_verify_complete;
    }

    @Override
    protected int getCaptureWarningMessageId() {
        return R.string.passcode_verify_warning;
    }
}
