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

package com.daon.fido.sdk.sample.basic.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.daon.fido.client.sdk.Fido;
import com.daon.fido.client.sdk.core.FidoConstants;
import com.daon.fido.client.sdk.model.Authenticator;
import com.daon.fido.client.sdk.model.AuthenticatorReg;
import com.daon.fido.client.sdk.model.DiscoveryData;
import com.daon.fido.sdk.sample.basic.model.AuthenticatorInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @noinspection SpellCheckingInspection
 */
public class AuthenticatorUtil {

    public static boolean registeredAuthsPresent(AuthenticatorReg[] authenticatorRegs) {
        for (AuthenticatorReg authenticatorReg : authenticatorRegs) {
            if (authenticatorReg.isRegistered()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAuthenticator(Context context, String aaid) {
        try {
            DiscoveryData discoveryData = Fido.getInstance(context).discover();
            List<String> availableAaids = new ArrayList<>(discoveryData.getAvailableAuthenticators().length);
            for (Authenticator authenticator : discoveryData.getAvailableAuthenticators()) {
                availableAaids.add(authenticator.getAaid());
            }
            return availableAaids.contains(aaid);
        } catch (Exception e) {
            return false;
        }
    }

    public static Authenticator[] getFilteredAuthenticator(Authenticator[][] authenticators) {
        for (Authenticator[] authenticator : authenticators) {
            if (authenticator[0].getAaid().equals("D409#8201")) {
                return authenticator;
            }
        }
        return null;
    }

    public static AuthenticatorInfo[] removeAuthenticatorsNotOnThisDevice(String deviceId, AuthenticatorInfo[] authenticatorInfoList) {
        List<AuthenticatorInfo> trimmedAuthenticatorInfoList = new ArrayList<>();
        for (AuthenticatorInfo authenticatorInfo : authenticatorInfoList) {
            if (authenticatorInfo.getDeviceCorrelationId() == null || authenticatorInfo.getDeviceCorrelationId().isEmpty()) {
                // If the device correlation ID is unknown then add to the list as we can't
                // be sure that the authenticator is not on this device.
                trimmedAuthenticatorInfoList.add(authenticatorInfo);
            } else {
                // Device correlation ID for authenticator is known
                if (deviceId == null || deviceId.isEmpty()) {
                    // Device ID of this device is unknown, so add to the list as we can't
                    // be sure that the authenticator is not on this device.
                    trimmedAuthenticatorInfoList.add(authenticatorInfo);
                } else {
                    // Only add the device if its device ID matches this device
                    if (deviceId.equals(authenticatorInfo.getDeviceCorrelationId())) {
                        authenticatorInfo.setPresentOnDevice(true);
                        trimmedAuthenticatorInfoList.add(authenticatorInfo);
                    }
                }
            }
        }
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        return trimmedAuthenticatorInfoList.toArray(new AuthenticatorInfo[trimmedAuthenticatorInfoList.size()]);
    }

    public static @NonNull StringBuilder getStringBuilder(Authenticator[][] authenticators, int i) {
        StringBuilder sb = new StringBuilder();
        int authenticatorsListedCount = 0;
        for (int j = 0; j < authenticators[i].length; j++) {
            if (authenticators[i][j].getUserVerification() == FidoConstants.USER_VERIFY_NONE) {
                if (authenticators[i].length == 1) {
                    sb.append(authenticators[i][j].getTitle());
                    authenticatorsListedCount++;
                }
            } else {
                if (authenticatorsListedCount > 0) {
                    sb.append(" &\n");
                }
                sb.append(authenticators[i][j].getTitle());
                authenticatorsListedCount++;
            }
        }
        return sb;
    }
}
