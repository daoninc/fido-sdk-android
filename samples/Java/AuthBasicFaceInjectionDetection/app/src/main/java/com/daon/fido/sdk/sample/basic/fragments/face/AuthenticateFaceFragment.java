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

package com.daon.fido.sdk.sample.basic.fragments.face;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daon.fido.sdk.sample.basic.R;

public class AuthenticateFaceFragment extends BaseFaceFragment {
    private static final String TAG = AuthenticateFaceFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.daon_face, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        infoText = view.findViewById(R.id.infoText);
        previewView = view.findViewById(R.id.previewView);

        takePhotoButton = view.findViewById(R.id.retakePhotoButton);
        takePhotoButton.setOnClickListener(v1 -> retakePhoto());

        doneButton = view.findViewById(R.id.doneButton);
        doneButton.setText(R.string.authenticate_text);
        doneButton.setOnClickListener(v12 -> authenticate());
    }

    @Override
    protected void onRecapture() {
        Log.d(TAG, "onRecapture: ");
        hideInfo();
        retakePhoto();
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.face_verify_failed;
    }

    @Override
    protected int getCaptureSuccessMessageId() {
        return R.string.face_verify_complete;
    }

    @Override
    protected int getCaptureWarningMessageId() {
        return R.string.face_verify_warning;
    }

    private void authenticate() {
        Log.d(TAG, "authenticate: ");
        vibrate();
        reportAuthenticationInProgress();
        onAuthenticateWait(true);
        getController().authenticate((errorCode, result, collectedImage) -> Log.d(TAG, "onAuthenticationResult: " + errorCode));
    }

    private void reportAuthenticationInProgress() {
        showMessage(R.string.face_verify);
    }

}
