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

package com.daon.fido.sdk.sample.basic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.daon.fido.sdk.sample.basic.R;
import com.daon.sdk.authenticator.Extensions;

public class SilentFragment extends BaseCaptureFragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getBooleanExtension(Extensions.SILENT_UI, false)) {
            return inflater.inflate(R.layout.daon_silent, container, false);
        }

        return null;
    }

    @Override
    public void start() {
        super.start();
        completeAuthentication();
    }

    protected void completeAuthentication() {
        // Create keys if necessary
        if (getController() != null) {
            if (getBooleanExtension(Extensions.SILENT_UI, false)) {
                getController().completeCapture(new DefaultCaptureCompleteListener());
            } else {
                getController().completeCapture();
            }
        }
    }
}
