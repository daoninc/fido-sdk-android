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

public class RegisterPatternFragment extends PatternFragment {

    @Override
    protected int getLayoutResource() {
        return R.layout.daon_register_pattern;
    }

    @Override
    protected PatternCollect.Mode getPatternCollectMode() {
        return PatternCollect.Mode.ENROLL;
    }

    @Override
    public void store(int[] pattern) {
        if (getController() != null) {
            getController().registerPattern(pattern, new DefaultCaptureCompleteListener());
        }
    }

    @Override
    public void validate(int[] pattern, PatternValidListener listener) {
        throw new RuntimeException("The pattern cannot be validated in the case of registration");
    }

    @Override
    public void onPatternCollectResult(PatternCollect.PatternCollectResult result) {
        if (result.getMode() == PatternCollect.Mode.ENROLL) {
            switch (result.getStatus()) {
                case FIRST_ENROLLMENT_COMPLETE:
                    showMessage(R.string.pattern_reentry);
                    break;
                case INVALID_CONFIRMATION_ENROLLMENT:
                    showMessage(R.string.pattern_reenetry_invalid);
                    break;
                case INVALID_ENROLMENT_MIN_SIZE:
                    showMessage(String.format(getResources().getString(R.string.pattern_enroll_less_than_min), getController().getMinNumberOfTouchPoints()));
                    break;
                case INVALID_ENROLMENT_MAX_SIZE:
                    showMessage(String.format(getResources().getString(R.string.pattern_enroll_more_than_max), getController().getMaxNumberOfTouchPoints()));
                    break;
                case INVALID_ENROLMENT_WEAK_PATTERN:
                    showMessage(R.string.pattern_enroll_weak_pattern);
                    break;
            }
        }
    }

    @Override
    protected int getCaptureSuccessMessageId() {
        return R.string.pattern_enroll_complete;
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.pattern_enroll_failed;
    }
}
