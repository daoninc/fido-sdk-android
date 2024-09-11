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

/** @noinspection unused, unused , unused , unused , unused , unused , unused , unused , unused */
public class SubmitFailedAttemptRequest {
    private String emailAddress;
    private String authenticationRequestId;
    private String errorCode;
    private String globalAttempt;
    private String attempt;
    private String attemptsRemaining;
    private String userAuthKeyId;
    private String lockStatus;
    private String score;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getAuthenticationRequestId() {
        return authenticationRequestId;
    }

    public void setAuthenticationRequestId(String authenticationRequestId) {
        this.authenticationRequestId = authenticationRequestId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getGlobalAttempt() {
        return globalAttempt;
    }

    public void setGlobalAttempt(String globalAttempt) {
        this.globalAttempt = globalAttempt;
    }

    public String getAttempt() {
        return attempt;
    }

    public void setAttempt(String attempt) {
        this.attempt = attempt;
    }

    public String getAttemptsRemaining() {
        return attemptsRemaining;
    }

    public void setAttemptsRemaining(String attemptsRemaining) {
        this.attemptsRemaining = attemptsRemaining;
    }

    public String getUserAuthKeyId() {
        return userAuthKeyId;
    }

    public void setUserAuthKeyId(String userAuthKeyId) {
        this.userAuthKeyId = userAuthKeyId;
    }

    public String getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(String lockStatus) {
        this.lockStatus = lockStatus;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
