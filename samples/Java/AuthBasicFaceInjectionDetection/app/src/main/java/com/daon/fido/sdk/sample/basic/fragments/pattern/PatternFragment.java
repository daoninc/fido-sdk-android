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

package com.daon.fido.sdk.sample.basic.fragments.pattern;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.daon.fido.sdk.sample.basic.R;
import com.daon.fido.sdk.sample.basic.fragments.BaseCaptureFragment;
import com.daon.sdk.authenticator.controller.PatternControllerProtocol;

public abstract class PatternFragment extends BaseCaptureFragment implements PatternCollect.PatternCollectResultReceiver, IPatternManager {
    private PatternCollect patternCollect;
    private PatternParameters parameters;

    public PatternControllerProtocol getController() {
        return (PatternControllerProtocol) super.getController();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        patternCollect = new PatternCollect(getContext(), null);
        parameters = new PatternParameters();

        ViewGroup rootView = createView(inflater, container);

        patternCollect.setParameters(parameters);
        patternCollect.setEnabled(true);
        return rootView;
    }

    private ViewGroup createView(LayoutInflater inflater, ViewGroup container) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(getLayoutResource(), container, false);

        ViewGroup layout = rootView.findViewById(R.id.layout);
        if (layout != null) layout.addView(patternCollect);

        return rootView;
    }

    @Override
    protected void start() {
        super.start();

        patternCollect.setController(getController());
        patternCollect.startCapture(getPatternCollectMode(), this, parameters, this);
    }

    protected abstract int getLayoutResource();

    protected abstract PatternCollect.Mode getPatternCollectMode();
}
