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

package com.daon.fido.sdk.sample.basic;

import com.daon.fido.sdk.sample.basic.fragments.SilentFragment;
import com.daon.fido.sdk.sample.basic.fragments.face.AuthenticateFaceFragment;
import com.daon.fido.sdk.sample.basic.fragments.face.RegisterFaceFragment;
import com.daon.fido.sdk.sample.basic.fragments.finger.FingerprintFragment;
import com.daon.fido.sdk.sample.basic.fragments.passcode.AuthenticatePasscodeFragment;
import com.daon.fido.sdk.sample.basic.fragments.passcode.RegisterPasscodeFragment;
import com.daon.fido.sdk.sample.basic.fragments.pattern.AuthenticatePatternFragment;
import com.daon.fido.sdk.sample.basic.fragments.pattern.RegisterPatternFragment;
import com.daon.sdk.authenticator.Authenticator;
import com.daon.sdk.authenticator.DefaultCaptureFragmentFactory;

public class CustomCaptureFragmentFactory extends DefaultCaptureFragmentFactory {

    public Class<?> getRegistrationFragment(Authenticator.Factor factor, Authenticator.Type type) {
        if (factor == Authenticator.Factor.PASSCODE) return RegisterPasscodeFragment.class;

        if (factor == Authenticator.Factor.FACE) return RegisterFaceFragment.class;

        if (factor == Authenticator.Factor.SILENT) return SilentFragment.class;

        if (factor == Authenticator.Factor.FINGERPRINT) return FingerprintFragment.class;

        if (factor == Authenticator.Factor.PATTERN) return RegisterPatternFragment.class;

        return super.getRegistrationFragment(factor, type);
    }

    public Class<?> getAuthenticationFragment(Authenticator.Factor factor, Authenticator.Type type) {
        if (factor == Authenticator.Factor.PASSCODE) return AuthenticatePasscodeFragment.class;

        if (factor == Authenticator.Factor.FACE)
            return AuthenticateFaceFragment.class; // Passive and Blink Liveness

        if (factor == Authenticator.Factor.SILENT) return SilentFragment.class;

        if (factor == Authenticator.Factor.FINGERPRINT) return FingerprintFragment.class;

        if (factor == Authenticator.Factor.PATTERN) return AuthenticatePatternFragment.class;

        return super.getAuthenticationFragment(factor, type);
    }
}
