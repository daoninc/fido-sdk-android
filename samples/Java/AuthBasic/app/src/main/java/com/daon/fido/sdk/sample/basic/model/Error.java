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

import androidx.annotation.NonNull;

/** @noinspection unused, unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused , unused */
public class Error {

    public static Error UNEXPECTED_ERROR =
            new Error(1, "An unexpected error occurred.  Please see the log files.");
    public static Error METHOD_NOT_IMPLEMENTED = new Error(2, "The method has not been implemented");

    public static Error USER_NOT_FOUND = new Error(10, "User not found");
    public static Error INVALID_CREDENTIALS =
            new Error(11, "Invalid credentials provided - the user could not be authenticated");
    public static Error INSUFFICIENT_CREDENTIALS =
            new Error(
                    12,
                    "The user cannot be authenticated - please supply a username and password or a FIDO authentication response");
    public static Error AUTHENTICATION_REQUEST_ID_NOT_PROVIDED =
            new Error(100, "The authentication request ID must be provided");

    public static Error PASSWORD_NOT_PROVIDED = new Error(101, "The password must be provided");
    public static Error EMAIL_NOT_PROVIDED = new Error(102, "The email must be provided");
    public static Error FIRST_NAME_NOT_PROVIDED = new Error(103, "The first name must be provided");
    public static Error LAST_NAME_NOT_PROVIDED = new Error(104, "The last name must be provided");

    public static Error FIDO_AUTH_COMPLETE_USER_NOT_FOUND =
            new Error(200, "The user was authenticated by FIDO but this user is not in the system");
    public static Error UNKNOWN_SESSION_IDENTIFIER = new Error(201, "Unknown session identifier");
    public static Error EXPIRED_SESSION = new Error(202, "The specified session has expired");
    public static Error NON_EXISTENT_SESSION = new Error(203, "The specified session does not exist");

    public static Error TRANSACTION_CONTENT_NOT_PROVIDED =
            new Error(303, "Transaction data must be provided");

    public static Error INVALID_SERVER_RESPONSE =
            new Error(-1, "Unexpected server response. Check server settings.");

    private int code;
    private String message;
    private String fidoMessage;
    private Long fidoResponseCode;
    private String fidoResponseMsg;

    public Error() {
    }

    public Error(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @NonNull
    public String toString() {
        return "Code: " + this.getCode() + " Message: " + this.getMessage();
    }

    public String getFidoMessage() {
        return fidoMessage;
    }

    public void setFidoMessage(String fidoMessage) {
        this.fidoMessage = fidoMessage;
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
