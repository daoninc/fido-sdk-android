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

/**
 * Created by mpatefield on 13/07/2016.
 * @noinspection unused
 */
public class PolicyInfo {
    private String id;
    private String type;
    private String policy;

    /** @noinspection unused*/
    public String getId() {
        return id;
    }

    /** @noinspection unused*/
    public void setId(String id) {
        this.id = id;
    }

    /** @noinspection unused*/
    public String getType() {
        return type;
    }

    /** @noinspection unused*/
    public void setType(String type) {
        this.type = type;
    }

    /** @noinspection unused*/
    public String getPolicy() {
        return policy;
    }

    /** @noinspection unused*/
    public void setPolicy(String policy) {
        this.policy = policy;
    }
}
