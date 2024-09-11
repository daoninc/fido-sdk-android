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

package com.daon.fido.sdk.sample.basic.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.daon.fido.sdk.sample.basic.R;
import com.daon.sdk.authenticator.Authenticator;
import com.daon.sdk.authenticator.ErrorCodes;
import com.daon.sdk.authenticator.VerificationAttemptParameters;
import com.daon.sdk.authenticator.capture.CaptureArguments;
import com.daon.sdk.authenticator.capture.EmbeddedView;
import com.daon.sdk.authenticator.capture.controller.ControllerAware;
import com.daon.sdk.authenticator.controller.AuthenticationInstance;
import com.daon.sdk.authenticator.controller.AuthenticatorError;
import com.daon.sdk.authenticator.controller.CaptureCompleteListener;
import com.daon.sdk.authenticator.controller.CaptureCompleteResult;
import com.daon.sdk.authenticator.controller.CaptureControllerProtocol;
import com.daon.sdk.authenticator.controller.ClientLockingProtocol;
import com.daon.sdk.authenticator.controller.LockResult;
import com.daon.sdk.authenticator.util.BusyIndicator;
import com.google.android.material.snackbar.Snackbar;

/**
 * Base class for fragments which use a capture controller (see {@link CaptureControllerProtocol})
 * to control capture.
 */
public abstract class BaseCaptureFragment extends Fragment implements VerificationAttemptParameters, EmbeddedView, CaptureArguments, ControllerAware, AuthenticationInstance {

    protected enum ExplicitPermission {
        GRANTED, DENIED
    }

    private CaptureControllerProtocol controller;
    private CaptureCallback callback;
    private boolean cancelledByParentActivity;
    private boolean onStartCalledPreResume;

    /**
     * Ensures that the active controller is retrieved when a fragment is restored.
     *
     * <p>Derived fragments which override this method MUST call <code>
     * super.onCreate(savedInstanceState)</code>
     *
     * @param savedInstanceState saved instance state
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            setController(Authenticator.Instance.getActiveController(getContext(), getInstanceId()));
        }
        super.onCreate(savedInstanceState);
    }

    public boolean isCancelledByParentActivity() {
        return cancelledByParentActivity;
    }

    public void setCancelledByParentActivity(boolean cancelledByParentActivity) {
        this.cancelledByParentActivity = cancelledByParentActivity;
    }

    public CaptureControllerProtocol getController() {
        return controller;
    }

    public void setController(CaptureControllerProtocol controller) {
        this.controller = controller;
    }

    public void cancel() {
        terminateParentActivityWithError(ErrorCodes.ERROR_CANCELED, null);
        if (controller != null) {
            controller.cancelCapture();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // The fragments are embedded within CaptureActivity
        // which implements the CaptureCallback interface. The callback interface allows
        // the fragment to notify the parent activity when authentication has completed
        try {
            callback = (CaptureCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement CaptureCallback");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // See onResume() for use
        onStartCalledPreResume = true;
        start();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (!onStartCalledPreResume) {
            // Handle the case where a system dialog pops up. Here onResume is called
            // without onStart being called first. Usually onStart is called first.
            start();
        } else {
            onStartCalledPreResume = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        stop();
    }

    @Override
    public String getAuthenticatorId() {
        Bundle args = getArguments();
        if (args != null) return args.getString(EXTRA_ID);
        return null;
    }

    /**
     * @return the fragment instance ID
     */
    protected String getHandlerId() {
        Bundle args = getArguments();
        if (args != null) return args.getString(EXTRA_HANDLER_ID);
        return null;
    }


    @Override
    public String getInstanceId() {
        return getHandlerId();
    }

    /**
     * Ensure that the controller is started when the fragment becomes visible.
     */
    protected void start() {
        if (controller != null) {
            controller.startCapture();
        }
    }

    protected void stop() {
        Log.d("DAON", "fragment stop()");
    }

    /**
     * Returns the input extensions for this authentication
     *
     * @return a bundle of extensions
     */
    protected Bundle getExtensions() {
        Bundle args = getArguments();
        if (args != null) return args.getBundle(EXTRA_EXTENSIONS);
        return null;
    }


    /**
     * Returns a boolean extension value
     *
     * @param key          extension key
     * @param defaultValue default value
     * @return the value of the extension if it is found, otherwise the default value
     */
    public boolean getBooleanExtension(String key, boolean defaultValue) {
        Bundle extensions = getExtensions();
        if (extensions != null) {
            String value = extensions.getString(key);
            if (value != null) return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    public boolean checkPermissions(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity() != null) {
                if (getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    requestPermission(permission);
                    return false;
                }
            }
        }

        // We have permission...
        return true;
    }

    protected void requestPermission(String permission) {
        requestPermission.launch(permission);
    }

    protected void onPermissionResult(Boolean result) {
    }

    final ActivityResultLauncher<String> requestPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), this::onPermissionResult);

    /**
     * Show a message in a {@link Snackbar}
     *
     * @param id message resource ID
     */
    protected void showMessage(int id) {
        if (id != 0 && getActivity() != null) {
            showMessage(getString(id));
        }
    }

    /**
     * Show a message in a {@link Snackbar}
     *
     * @param message message string
     */
    protected void showMessage(final String message) {
        if (message != null && !message.isEmpty() && getActivity() != null) {
            Snackbar sb = Snackbar.make(getActivity().findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
            View snackbarView = sb.getView();
            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setMaxLines(5);
            sb.show();
        }
    }

    /** @noinspection unused*/
    @SuppressLint("MissingPermission")
    protected void vibrate() {
        Activity activity = getActivity();
        if (activity != null) {
            Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) vibrator.vibrate(200);
        }
    }

    /**
     * Enable of disable a wait UI.
     *
     * <p>The default wait UI is a transparent dialog with a spinner. Override this method to replace
     * the wait UI. Also override {@link #isAuthenticateWait()}.
     *
     * @param start true to start waiting, false to stop waiting
     */
    protected void onAuthenticateWait(boolean start) {
        setBusy(start);
    }

    /**
     * Is the wait UI being displayed.
     *
     * <p>Override this if {@link #onAuthenticateWait(boolean)} is overridden.
     *
     * @return true if the wait UI is being displayed, false otherwise
     */
    protected boolean isAuthenticateWait() {
        return BusyIndicator.isBusy();
    }

    protected void setBusy(boolean enable) {
        if (enable) {
            BusyIndicator.setBusy(getActivity(), null);
        } else {
            BusyIndicator.setNotBusy(getActivity());
        }
    }

    /**
     * This class controls the default reaction to capture complete events.
     *
     * <p>Derived fragments will generally override the protected methods of this class to customize
     * the user experience.
     */
    public class DefaultCaptureCompleteListener implements CaptureCompleteListener {
        @Override
        public void onCaptureComplete(CaptureCompleteResult result) {
            if (getController() != null) {
                disableWaitUI();
                switch (result.getType()) {
                    case CaptureCompleteResult.Type.TERMINATE_SUCCESS:
                        onTerminateSuccess(result);
                        break;
                    case CaptureCompleteResult.Type.TERMINATE_FAILURE:
                        onTerminateFailure();
                        break;
                    case CaptureCompleteResult.Type.SERVER_VALIDATION_ERROR:
                        onServerAuthenticationFailed(result);
                        break;
                    case CaptureCompleteResult.Type.CLIENT_VALIDATION_ERROR:
                        onClientAuthenticationFailed(result);
                        break;
                    case CaptureCompleteResult.Type.CLIENT_ERROR:
                        onClientError(result);
                        break;
                }
            }
        }

        /**
         * Disable the wait user interface if it is being displayed
         */
        protected void disableWaitUI() {
            if (isAuthenticateWait()) {
                onAuthenticateWait(false);
            }
        }

        /**
         * Handle a server-based capture terminated due to a capture data validation error (a {@link
         * CaptureCompleteResult} with a type of {@link
         * CaptureCompleteResult.Type#SERVER_VALIDATION_ERROR})
         *
         * <p>Default behaviour is to display the error and retries remaining then ready the UI for
         * recapture
         *
         * @param result capture complete result
         */
        protected void onServerAuthenticationFailed(CaptureCompleteResult result) {
            reportRetriesRemaining(result);

            // Ready UI for another capture
            onRecapture();
        }

        /**
         * Handle a successful capture (a {@link CaptureCompleteResult} with a type of {@link
         * CaptureCompleteResult.Type#TERMINATE_SUCCESS})
         *
         * <p>Default behaviour is to show a generic success message ({@link
         * #getCaptureSuccessMessageId()}) depending on {@link #showMessageOnTerminate()} and terminate
         * the parent activity if necessary
         *
         * @param result capture complete result
         */
        protected void onTerminateSuccess(CaptureCompleteResult result) {
            reportCaptureSucceeded(getCaptureSuccessMessageId());
            terminateParentActivity();
        }

        /**
         * Report client authentication failed with attempts remaining
         *
         * @param result capture complete result
         */
        protected void reportClientAuthenticationFailed(final CaptureCompleteResult result) {
            if (result.getInfo().getBoolean(CaptureCompleteResult.InfoKey.IS_WARN_ATTEMPT, false) && isDisplayWarnAttemptEnabled()) {
                reportCaptureWarning(getCaptureWarningMessageId());
            } else {
                reportRetriesRemaining(result);
            }
        }

        /**
         * Report capture warning message
         *
         * @param resMsgWarning generic warning message ID
         */
        protected void reportCaptureWarning(final int resMsgWarning) {
            showMessage(resMsgWarning);
        }

        /**
         * Report capture failure message
         *
         * @param resMsgFailed generic failure message ID
         */
        protected void reportCaptureFailed(final int resMsgFailed) {
            if (showMessageOnTerminate()) {
                showMessage(resMsgFailed);
            }
        }

        /**
         * Report capture succeeded message
         *
         * @param resMsgSuccess generic capture success message ID
         */
        protected void reportCaptureSucceeded(final int resMsgSuccess) {
            if (showMessageOnTerminate()) {
                showMessage(resMsgSuccess);
            }
        }

        /**
         * Report the number of retries remaining and authentication error
         *
         * @param result captureResult
         */
        protected void reportRetriesRemaining(CaptureCompleteResult result) {
            String authenticationError = getAuthenticationErrorMessage(result);
            String retryMessage = getRetryMessage(result);
            if (authenticationError == null) {
                showMessage(retryMessage);
            } else {
                showMessage(authenticationError + "\n" + retryMessage);
            }
        }

        /**
         * Get the error string to display when user capture data validation fails
         *
         * @param result validation capture result
         * @return the error string to display when user capture data validation fails
         */
        protected String getAuthenticationErrorMessage(CaptureCompleteResult result) {
            return result.getError().getMessage();
        }

        /**
         * Get the retry message, e.g. "1 retry remaining" to display when user capture data validation
         * fails
         *
         * @param result validation capture result
         * @return the retry message to display when user capture data validation fails
         */
        protected String getRetryMessage(CaptureCompleteResult result) {
            int numberOfRetries = result.getInfo().getInt(CaptureCompleteResult.InfoKey.RETRIES_REMAINING, -1);
            if (numberOfRetries > 0) {
                if (numberOfRetries == 1) {
                    return getResources().getString(R.string.retry_remaining);
                } else {
                    return getResources().getString(R.string.retries_remaining, numberOfRetries);
                }
            } else {
                return getResources().getString(R.string.try_again);
            }
        }

        /**
         * Handle a server-based capture terminated due to a failure (e.g. server error) (a {@link
         * CaptureCompleteResult} with a type of {@link CaptureCompleteResult.Type#TERMINATE_FAILURE})
         *
         * <p>Default behaviour is to display nothing and terminate the parent activity if necessary
         */
        protected void onTerminateFailure() {
            cancel();
        }

        /**
         * Handle a client-based capture terminated due to a capture data validation error (a {@link
         * CaptureCompleteResult} with a type of {@link
         * CaptureCompleteResult.Type#CLIENT_VALIDATION_ERROR})
         *
         * <p>Default behaviour if the authenticator is not yet locked is to report a capture warning
         * ({@link #getCaptureWarningMessageId()}) if a number of successive attempts failed, otherwise
         * show the error and number of retries remaining. The UI is then made ready for recapture. If
         * the authenticator is locked then an error may be displayed depending on {@link
         * #showMessageOnTerminate()} and capture is completed with an error and the parent activity is
         * terminated if necessary.
         *
         * @param result capture complete result
         */
        protected void onClientAuthenticationFailed(CaptureCompleteResult result) {
            Log.d("DAON", "fragment onClientAuthenticationFailed");
            if (result.getLockInfo().getState() == Authenticator.Lock.UNLOCKED) {
                reportClientAuthenticationFailed(result);

                // Ready UI for another capture
                onRecapture();
            } else {
                onAuthenticatorLocked(result);
            }
        }

        /**
         * Handle a capture which fails unexpectedly (a {@link CaptureCompleteResult} with a type of
         * {@link CaptureCompleteResult.Type#CLIENT_ERROR})
         *
         * <p>Default behaviour is to show a generic failed message ({@link
         * #getCaptureFailedMessageId()}) depending on {@link #showMessageOnTerminate()} and terminate
         * the parent activity if necessary
         *
         * @param result capture complete result
         */
        protected void onClientError(CaptureCompleteResult result) {
            reportCaptureFailed(getCaptureFailedMessageId());
            terminateParentActivityWithError(result.getError().getCode(), result.getError().getMessage());
        }
    }

    /**
     * This method is called after a validation failure to reset the UI ready to capture data again.
     * To be overridden as necessary.
     */
    protected void onRecapture() {
    }

    /**
     * @return Whether to display a warn attempt after 3 successive failed attempts.
     * @noinspection SameReturnValue
     */
    protected boolean isDisplayWarnAttemptEnabled() {
        return true;
    }

    /**
     * @return default capture success message ID. To be overridden.
     */
    protected int getCaptureSuccessMessageId() {
        return 0;
    }

    /**
     * @return default capture failed message ID. To be overridden.
     */
    protected int getCaptureFailedMessageId() {
        return 0;
    }

    /**
     * @return default warning message ID on three subsequent user data validation failures. To be
     * overridden.
     */
    protected int getCaptureWarningMessageId() {
        return 0;
    }

    /**
     * Handles when an authenticator is locked as a result of user data validation. Checks the lock
     * state, displays a relevant error depending on {@link #showMessageOnTerminate()}, and terminate
     * the controller, and optionally the parent activity with an error if the authenticator is
     * locked.
     *
     * @param result authenticator lock state info resulting from a capture failure
     */
    protected void onAuthenticatorLocked(LockResult result) {
        if (result == null || result.getLockInfo() == null) {
            return;
        }
        if (getController() != null) {
            ClientLockingProtocol.LockInfo lockInfo = result.getLockInfo();
            if (lockInfo.getState() != Authenticator.Lock.UNLOCKED) {
                if (lockInfo.getState() == Authenticator.Lock.TEMPORARY) {
                    reportAuthenticatorLocked(result);
                    completeCaptureWithError(new AuthenticatorError(ErrorCodes.ERROR_LOCKOUT, getString(R.string.authenticator_locked_temp, lockInfo.getSeconds())));
                } else if (lockInfo.getState() == Authenticator.Lock.PERMANENT) {
                    reportAuthenticatorLocked(result);
                    completeCaptureWithError(new AuthenticatorError(ErrorCodes.ERROR_LOCKOUT, getString(R.string.authenticator_locked)));
                } else {
                    reportAuthenticatorLocked(result);
                    completeCaptureWithError(new AuthenticatorError(ErrorCodes.ERROR_MAX_ATTEMPTS, getString(R.string.authenticator_max_attempts)));
                }
            }
        }
    }

    /**
     * Report authenticator locked message
     *
     * @param result authenticator lock state info resulting from a capture failure
     */
    protected void reportAuthenticatorLocked(LockResult result) {
        if (showMessageOnTerminate()) {
            switch (result.getLockInfo().getState()) {
                case PERMANENT:
                    showMessage(R.string.authenticator_locked);
                    break;
                case TEMPORARY:
                    showMessage(getString(R.string.authenticator_locked_temp, result.getLockInfo().getSeconds()));
                    break;
            }
        }
    }

    /**
     * Complete capture with an error. Notify SDK and parent activity.
     *
     * @param error error
     */
    protected void completeCaptureWithError(AuthenticatorError error) {
        terminateParentActivityWithError(error.getCode(), error.getMessage());
    }

    /**
     * Terminate the parent activity on success
     */
    protected void terminateParentActivity() {
        if (callback != null && getController() != null) {
            callback.onCaptureComplete(getController().getResponseExtensions());
        }
    }

    /**
     * Terminate the parent activity on error
     *
     * @param code    error code
     * @param message error message
     */
    protected void terminateParentActivityWithError(final int code, final String message) {
        if (callback != null && getController() != null) {
            callback.onCaptureFailed(getController().getResponseExtensions(), code, message);
        }
    }

    /**
     * By default, the controller is not destroyed nor is the SDK notified when the fragment is
     * destroyed. This is because a fragment may be destroyed and re-created several times during an
     * authentication session. This behaviour may be changed by overriding {@link
     * #destroyControllerOnDestroy()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (destroyControllerOnDestroy()) {
            Authenticator.Instance.destroyAuthenticatorInstance(getContext(), getInstanceId());
        }
        callback = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * @return Override and set to true to ensure that the controller is destroyed when the fragment
     * is destroyed. Only recommended if the fragment will never be destroyed during an
     * authentication.
     * @noinspection SameReturnValue
     */
    public boolean destroyControllerOnDestroy() {
        return false;
    }

    /**
     * @return show messages to the user when a fragment terminates. Defaults to false as generally
     * the message will be handled by the SDK calling back the application.
     * @noinspection SameReturnValue
     */
    public boolean showMessageOnTerminate() {
        return false;
    }
}
