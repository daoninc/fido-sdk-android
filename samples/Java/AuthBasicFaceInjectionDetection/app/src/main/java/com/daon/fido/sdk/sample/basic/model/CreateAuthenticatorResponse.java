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

/** @noinspection unused, unused , unused */
public class CreateAuthenticatorResponse {

    private String fidoRegistrationConfirmation;
    private Long fidoResponseCode;
    private String fidoResponseMsg;

    public CreateAuthenticatorResponse() {
    }

    public String getFidoRegistrationConfirmation() {
        return fidoRegistrationConfirmation;
    }

    public void setFidoRegistrationConfirmation(String fidoRegistrationConfirmation) {
        this.fidoRegistrationConfirmation = fidoRegistrationConfirmation;
    }

    public Long getFidoResponseCode() {
        return fidoResponseCode;
    }

    public void setFidoResponseCode(Long fidoResponseCode) {
        this.fidoResponseCode = fidoResponseCode;
    }

    public String getFidoResponseMsg() {
        return fidoResponseMsg;
    }

    public void setFidoResponseMsg(String fidoResponseMsg) {
        this.fidoResponseMsg = fidoResponseMsg;
    }
}
