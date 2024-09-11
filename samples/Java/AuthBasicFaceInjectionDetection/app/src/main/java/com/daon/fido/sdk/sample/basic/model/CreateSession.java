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

/** @noinspection unused, unused , unused , unused , unused , unused */
public class CreateSession {

    private String email;
    private String password;
    private String fidoAuthenticationResponse;
    private String authenticationRequestId;

    public CreateSession() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFidoAuthenticationResponse() {
        return fidoAuthenticationResponse;
    }

    public void setFidoAuthenticationResponse(String fidoAuthenticationResponse) {
        this.fidoAuthenticationResponse = fidoAuthenticationResponse;
    }

    public String getAuthenticationRequestId() {
        return authenticationRequestId;
    }

    public void setAuthenticationRequestId(String authenticationRequestId) {
        this.authenticationRequestId = authenticationRequestId;
    }
}
