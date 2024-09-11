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

package com.daon.fido.sdk.sample.basic.model;

/** @noinspection unused, unused , unused , unused , unused , unused , unused */
public class CreateTransactionAuthRequest {

    private String transactionContentType;
    private String transactionContent;
    private boolean stepUpAuth;
    private boolean otpEnabled;

    public boolean isStepUpAuth() {
        return stepUpAuth;
    }

    public void setStepUpAuth(boolean stepUpAuth) {
        this.stepUpAuth = stepUpAuth;
    }

    public String getTransactionContentType() {
        return transactionContentType;
    }

    public void setTransactionContentType(String transactionContentType) {
        this.transactionContentType = transactionContentType;
    }

    public String getTransactionContent() {
        return transactionContent;
    }

    public void setTransactionContent(String transactionContent) {
        this.transactionContent = transactionContent;
    }

    public boolean isOtpEnabled() {
        return otpEnabled;
    }

    public void setOtpEnabled(boolean otpEnabled) {
        this.otpEnabled = otpEnabled;
    }
}
