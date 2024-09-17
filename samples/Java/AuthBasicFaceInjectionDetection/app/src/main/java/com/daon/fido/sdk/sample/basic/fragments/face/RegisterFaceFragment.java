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

public class RegisterFaceFragment extends BaseFaceFragment {
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
        doneButton.setOnClickListener(v12 -> enroll());

    }

    private void enroll() {
        showMessage(R.string.face_enroll);

        if (doneButton != null)
            doneButton.setVisibility(View.GONE);

        if (takePhotoButton != null)
            takePhotoButton.setVisibility(View.GONE);

        if (getController() != null) {
            onAuthenticateWait(true);
            getController().register((errorCode, result, collectedImage) -> Log.d("Face", "onRegistrationResult: " + errorCode));
        }
    }


    @Override
    protected void onRecapture() {
        retakePhotoDelayed();
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.face_enroll_failed;
    }

    @Override
    protected int getCaptureSuccessMessageId() {
        return R.string.face_enroll_complete;
    }
}
