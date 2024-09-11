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

import java.util.Date;

/** @noinspection unused*/
public class AuthenticatorInfo {
    private String id;
    private Date created;
    private Date lastUsed;
    private String name;
    private String description;
    private String vendorName;
    private String icon;
    private String status;
    private String fidoDeregistrationRequest;
    private String aaid;
    private String deviceCorrelationId;
    private boolean presentOnDevice;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }

    public String getFidoDeregistrationRequest() {
        return fidoDeregistrationRequest;
    }

    public void setFidoDeregistrationRequest(String fidoDeregistrationRequest) {
        this.fidoDeregistrationRequest = fidoDeregistrationRequest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAaid() {
        return aaid;
    }

    public void setAaid(String aaid) {
        this.aaid = aaid;
    }

    public String getDeviceCorrelationId() {
        return deviceCorrelationId;
    }

    public void setDeviceCorrelationId(String deviceCorrelationId) {
        this.deviceCorrelationId = deviceCorrelationId;
    }

    public boolean isPresentOnDevice() {
        return presentOnDevice;
    }

    public void setPresentOnDevice(boolean presentOnDevice) {
        this.presentOnDevice = presentOnDevice;
    }
}
