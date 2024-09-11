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

/** @noinspection unused*/
public class SubmitOfflineOTPRequest {
    private String emailAddress;
    private String submittedAuthenticationCode;
    private String secureTransactionContent;

    /** @noinspection unused*/
    public String getEmailAddress() {
        return emailAddress;
    }

    /** @noinspection unused*/
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /** @noinspection unused*/
    public String getSubmittedAuthenticationCode() {
        return submittedAuthenticationCode;
    }

    /** @noinspection unused*/
    public void setSubmittedAuthenticationCode(String submittedAuthenticationCode) {
        this.submittedAuthenticationCode = submittedAuthenticationCode;
    }

    /** @noinspection unused*/
    public String getSecureTransactionContent() {
        return secureTransactionContent;
    }

    /** @noinspection unused*/
    public void setSecureTransactionContent(String secureTransactionContent) {
        this.secureTransactionContent = secureTransactionContent;
    }
}
