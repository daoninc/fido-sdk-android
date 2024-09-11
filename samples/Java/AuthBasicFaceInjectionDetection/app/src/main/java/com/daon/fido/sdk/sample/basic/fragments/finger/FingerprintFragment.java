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

package com.daon.fido.sdk.sample.basic.fragments.finger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daon.fido.sdk.sample.basic.R;
import com.daon.fido.sdk.sample.basic.fragments.BaseCaptureFragment;
import com.daon.sdk.authenticator.controller.FingerprintCaptureControllerProtocol;

public class FingerprintFragment extends BaseCaptureFragment {

    public FingerprintCaptureControllerProtocol getController() {
        return (FingerprintCaptureControllerProtocol) super.getController();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.finger_auth, container, false);
    }

    @Override
    protected void start() {
        if (getController() != null) {
            getController().startCapture(this, new DefaultCaptureCompleteListener());
        }
    }

    @Override
    protected void stop() {
        if (getController() != null) getController().stopCapture();
    }
}
