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

import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;

import com.daon.fido.sdk.sample.basic.fragments.BaseCaptureFragment;
import com.daon.sdk.authenticator.Extensions;
import com.daon.sdk.authenticator.controller.PasscodeControllerProtocol;
import com.daon.sdk.authenticator.util.Keypad;

public abstract class BasePasscodeFragment extends BaseCaptureFragment {
    public PasscodeControllerProtocol getController() {
        return (PasscodeControllerProtocol) super.getController();
    }

    protected void setPasscodeEditTextRestrictions(EditText editText) {
        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        if (getController() != null) {
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(getController().getMaxLength())});

            if (Extensions.TYPE_ALPHANUMERIC.equals(getController().getKeyboardType())) {
                editText.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
        }
    }

    @Override
    protected void start() {
        super.start();

        // Show keypad
        final EditText passcodeEditText = getPrimaryPasscodeEditText();
        if (passcodeEditText != null) {
            passcodeEditText.requestFocus();
            new Handler().postDelayed(() -> Keypad.show(getActivity(), passcodeEditText), 100);
        }
    }

    @Override
    protected void stop() {
        super.stop();

        // Hide keypad
        EditText passcodeEditText = getPrimaryPasscodeEditText();
        if (passcodeEditText != null) {
            Keypad.hide(getActivity(), passcodeEditText);
        }
    }

    protected abstract EditText getPrimaryPasscodeEditText();

    protected abstract void reset();

    @Override
    protected void onRecapture() {
        reset();
    }
}
