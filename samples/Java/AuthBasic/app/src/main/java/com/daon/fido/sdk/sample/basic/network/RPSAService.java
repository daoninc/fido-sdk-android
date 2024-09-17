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

package com.daon.fido.sdk.sample.basic.network;

import static com.daon.fido.sdk.sample.basic.network.tasks.CreateAuthRequestTask.*;
import static com.daon.fido.sdk.sample.basic.network.tasks.CreateRegRequestTask.*;
import static com.daon.fido.sdk.sample.basic.network.tasks.UserLoginWithFIDOTask.*;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.daon.fido.client.sdk.ErrorInfo;
import com.daon.fido.client.sdk.Fido;
import com.daon.fido.client.sdk.IXUAFCommService;
import com.daon.fido.client.sdk.IXUAFCommServiceListener;
import com.daon.fido.client.sdk.ServerCommResult;
import com.daon.fido.client.sdk.model.Operation;
import com.daon.fido.client.sdk.model.UafProtocolMessageBase;
import com.daon.fido.client.sdk.uaf.UafMessageUtils;
import com.daon.fido.sdk.sample.basic.network.exception.CommunicationsException;
import com.daon.fido.sdk.sample.basic.network.exception.ServerError;
import com.daon.fido.sdk.sample.basic.model.CreateAccount;
import com.daon.fido.sdk.sample.basic.model.CreateAccountResponse;
import com.daon.fido.sdk.sample.basic.model.CreateSessionResponse;
import com.daon.fido.sdk.sample.basic.model.DeleteAccountResponse;
import com.daon.fido.sdk.sample.basic.model.GetAuthenticatorResponse;
import com.daon.fido.sdk.sample.basic.model.ListAuthenticatorsResponse;
import com.daon.fido.sdk.sample.basic.network.tasks.CreateAuthRequestTask;
import com.daon.fido.sdk.sample.basic.network.tasks.CreateAuthenticatorTask;
import com.daon.fido.sdk.sample.basic.network.tasks.CreateRegRequestTask;
import com.daon.fido.sdk.sample.basic.network.tasks.CreateTransactionAuthRequestTask;
import com.daon.fido.sdk.sample.basic.network.tasks.DeregisterAuthenticatorTask;
import com.daon.fido.sdk.sample.basic.network.tasks.GetPolicyTask;
import com.daon.fido.sdk.sample.basic.network.tasks.SubmitFailedAttemptTask;
import com.daon.fido.sdk.sample.basic.network.tasks.UserLoginWithFIDOTask;
import com.daon.fido.sdk.sample.basic.network.tasks.ValidateTransactionAuthTask;

public class RPSAService implements IXUAFCommService, AuthRequestListener, RegistrationRequestListener, CreateSessionListener {

    private static final String TAG = "RPSAService";  // Resources
    private static final String SERVER_RESOURCE_CREATE_ACCOUNT = "accounts";
    private static final String SERVER_RESOURCE_AUTHENTICATORS = "authenticators";
    private static final String SERVER_RESOURCE_SESSIONS = "sessions";
    private static final String SERVER_RESOURCE_LIST_AUTHENTICATORS = "listAuthenticators";
    public static final String KEY_APP_ID = "fidoAppId";
    public static String sessionId = null;

    private final Context context;
    private String mRegRequestId;
    private String mAuthRequestId;
    private CreateSessionResponse mCreateSessionResponse;
    private final HTTP http;

    enum State {
        login, transaction
    }

    private State mState;

    private static RPSAService instance = null;

    private RPSAService(Context context) {
        this.context = context;
        http = new HTTP(context);
    }

    public static RPSAService getInstance(Context context) {
        if (instance == null) instance = new RPSAService(context);
        return instance;
    }


    @Override
    public void serviceRequestRegistrationWithUsername(String username, IXUAFCommServiceListener commServiceListener) {
        CreateRegRequestTask mCreateRegReqTask = new CreateRegRequestTask(context, http, commServiceListener, this);
        mCreateRegReqTask.execute();
    }

    @Override
    public void serviceRegister(String registrationResponse, IXUAFCommServiceListener commServiceListener) {
        CreateAuthenticatorTask mCreateAuthTask = new CreateAuthenticatorTask(http, commServiceListener, registrationResponse, mRegRequestId);
        mCreateAuthTask.execute();
    }

    @Override
    public void serviceRequestAuthenticationWithParams(Bundle params, IXUAFCommServiceListener commServiceListener) {
        if (sessionId != null) {
            mState = State.transaction;
            CreateTransactionAuthRequestTask mCreateTransactionAuthRequestTask = new CreateTransactionAuthRequestTask(http, commServiceListener, this);
            mCreateTransactionAuthRequestTask.execute();
        } else {
            mState = State.login;
            CreateAuthRequestTask mCreateAuthRequestTask = new CreateAuthRequestTask(context, http, commServiceListener, params, this);
            mCreateAuthRequestTask.execute();
        }
    }

    @Override
    public void serviceAuthenticate(String authenticationRequest, String authenticationResponse, String username, IXUAFCommServiceListener commServiceListener) {
        if (mState == State.transaction) {
            ValidateTransactionAuthTask validateTransactionAuthTask = new ValidateTransactionAuthTask(http, commServiceListener, authenticationRequest, authenticationResponse, username, mAuthRequestId);
            validateTransactionAuthTask.execute();
        } else {
            UserLoginWithFIDOTask mUserLoginWithFIDOTask = new UserLoginWithFIDOTask(http, commServiceListener, authenticationResponse, mAuthRequestId, this);
            mUserLoginWithFIDOTask.execute();
        }
    }

    @Override
    public void serviceUpdate(String authenticationResponse, String username, IXUAFCommServiceListener commServiceListener) {
        UafProtocolMessageBase[] uafRequests = UafMessageUtils.validateUafMessage(context, authenticationResponse, UafMessageUtils.OpDirection.Response, null);
        if (uafRequests[0].header.op == Operation.Reg) {
            CreateAuthenticatorTask mCreateAuthTask = new CreateAuthenticatorTask(http, commServiceListener, authenticationResponse, mRegRequestId);
            mCreateAuthTask.execute();
        } else {
            if (mState == State.transaction) {
                ValidateTransactionAuthTask validateTransactionAuthTask = new ValidateTransactionAuthTask(http, commServiceListener, null, authenticationResponse, username, mAuthRequestId);
                validateTransactionAuthTask.execute();
            } else {
                UserLoginWithFIDOTask mUserLoginWithFIDOTask = new UserLoginWithFIDOTask(http, commServiceListener, authenticationResponse, mAuthRequestId, this);
                mUserLoginWithFIDOTask.execute();
            }
        }
    }

    @Override
    public void serviceRequestRegistrationPolicy(IXUAFCommServiceListener commServiceListener) {
        GetPolicyTask mGetPolicyTask = new GetPolicyTask(http, commServiceListener);
        mGetPolicyTask.execute();
    }

    @Override
    public void serviceRequestDeregistration(String authenticatorId, IXUAFCommServiceListener commServiceListener) {
        String deviceId = Fido.getInstance(context).getDeviceInfo().getDeviceId();
        DeregisterAuthenticatorTask deregisterAuthenticatorTask = new DeregisterAuthenticatorTask(http, commServiceListener, authenticatorId, deviceId);
        deregisterAuthenticatorTask.execute();
    }

    @Override
    public void serviceSubmitFailedAuthData(Bundle params, IXUAFCommServiceListener commServiceListener) {
        SubmitFailedAttemptTask submitFailedAttemptTask = new SubmitFailedAttemptTask(http, commServiceListener, params, mAuthRequestId);
        submitFailedAttemptTask.execute();
    }

    public ServerCommResult serviceCreateAccount(CreateAccount account) {
        ServerCommResult response = new ServerCommResult();
        try {
            CreateAccountResponse createAccountResponse = http.post(SERVER_RESOURCE_CREATE_ACCOUNT, account, CreateAccountResponse.class);
            sessionId = createAccountResponse.getSessionId();
            response.setResponse(createAccountResponse.getSessionId());
        } catch (ServerError e) {
            Log.e(TAG, "serviceCreateAccount: ", e);
            ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
            response.setErrorInfo(errorInfo);

        } catch (CommunicationsException e) {
            Log.e(TAG, "serviceCreateAccount: ", e);
            ErrorInfo errorInfo = new ErrorInfo(e.getError().getCode(), e.getError().getMessage());
            response.setErrorInfo(errorInfo);
        }
        return response;
    }

    public CreateSessionResponse getCreateSessionResponse() {
        return mCreateSessionResponse;
    }

    // This is an application specific call to the RPSAServer - not added in IXUFCommService
    public ListAuthenticatorsResponse serviceRequestAuthInfoList() {
        return http.get(SERVER_RESOURCE_LIST_AUTHENTICATORS, ListAuthenticatorsResponse.class);
    }

    public void deleteSession() {
        sessionId = null;
        http.deleteResource(SERVER_RESOURCE_SESSIONS, sessionId, false);
    }

    public DeleteAccountResponse deleteAccount() {
        DeleteAccountResponse response = http.deleteResource(SERVER_RESOURCE_CREATE_ACCOUNT, sessionId, DeleteAccountResponse.class);
        sessionId = null;
        return response;
    }

    public GetAuthenticatorResponse getAuthenticator(String id) {
        return  http.get(SERVER_RESOURCE_AUTHENTICATORS, id, GetAuthenticatorResponse.class);
    }

    @Override
    public void setAuthRequestId(String authRequestId) {
        mAuthRequestId = authRequestId;
    }

    @Override
    public void setRegRequestId(String regRequestId) {
        mRegRequestId = regRequestId;
    }

    @Override
    public void setCreateSessionResponse(CreateSessionResponse response) {
        mCreateSessionResponse = response;
    }

    @Override
    public void setSessionId(String sessionId) {
        RPSAService.sessionId = sessionId;
    }
}
