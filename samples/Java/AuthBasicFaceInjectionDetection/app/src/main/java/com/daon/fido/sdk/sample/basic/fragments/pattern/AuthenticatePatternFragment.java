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

import com.daon.fido.sdk.sample.basic.R;
import com.daon.sdk.authenticator.controller.CaptureCompleteResult;

public class AuthenticatePatternFragment extends PatternFragment {
    PatternValidListener patternValidListener;

    @Override
    protected int getLayoutResource() {
        return R.layout.daon_authenticate_pattern;
    }

    @Override
    protected PatternCollect.Mode getPatternCollectMode() {
        return PatternCollect.Mode.AUTHENTICATE;
    }

    @Override
    public void store(int[] pattern) {
        throw new RuntimeException("This pattern cannot be stored during authentication");
    }

    @Override
    public void validate(int[] pattern, PatternValidListener listener) {
        this.patternValidListener = listener;
        if (getController() != null) {
            getController().authenticatePattern(pattern, new CaptureCompleteListener());
        }
    }

    @Override
    public void onPatternCollectResult(PatternCollect.PatternCollectResult result) {
    }

    private class CaptureCompleteListener extends DefaultCaptureCompleteListener {
        @Override
        protected void onTerminateSuccess(CaptureCompleteResult result) {
            if (patternValidListener != null) {
                patternValidListener.onPatternValid(true);
            }
            super.onTerminateSuccess(result);
        }

        @Override
        protected void onClientAuthenticationFailed(CaptureCompleteResult result) {
            if (patternValidListener != null) {
                patternValidListener.onPatternValid(false);
            }
            super.onClientAuthenticationFailed(result);
        }

        @Override
        protected void onClientError(CaptureCompleteResult result) {
            if (patternValidListener != null) {
                patternValidListener.onPatternValid(false);
            }
            super.onClientError(result);
        }
    }

    @Override
    protected int getCaptureSuccessMessageId() {
        return R.string.pattern_verify_complete;
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.pattern_verify_failed;
    }

    @Override
    protected int getCaptureWarningMessageId() {
        return R.string.pattern_verify_warning;
    }
}
