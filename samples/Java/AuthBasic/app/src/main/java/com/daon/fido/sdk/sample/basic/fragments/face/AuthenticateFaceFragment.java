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

package com.daon.fido.sdk.sample.basic.fragments.face;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.ExperimentalGetImage;

import com.daon.fido.sdk.sample.basic.R;
import com.daon.sdk.authenticator.ErrorCodes;
import com.daon.sdk.face.RecognitionResult;
import com.daon.sdk.face.Result;
import com.daon.sdk.face.YUV;
import com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol;

public class AuthenticateFaceFragment extends BaseFaceFragment {
    private YUV capturedImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.daon_authenticate_face, container, false);
        getController().setImageQualityChecker((result, controllerConfiguration) ->
                result.getQualityResult().hasAcceptableQuality() &&
                result.getQualityResult().isFaceCentered() &&
                result.getQualityResult().hasAcceptableEyeDistance());
        return v;
    }

    @Override
    public void onImageAnalyzed(YUV yuv, Result result, boolean isQualityImage) {
        super.onImageAnalyzed(yuv, result, isQualityImage);

        if (!isEnableInfoText()) return;

        if (result.hasMask()) setWarning(R.string.face_mask_detected);

        if (!getController().isLivenessEnabled()) {
            checkAndUpdateQualityInfo(result);
            if (isQualityImage) {
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (capturedImage == null) {
                        capturedImage = getController().captureImage();
                        if (capturedImage != null) authenticate(capturedImage);
                    }
                }, 1500);
            }
        } else {
            if (checkAndUpdateQualityInfo(result)) updateInfo(result);
        }
        result.getLivenessResult().getAlert();
        setWarning(getAlertMessage(result.getLivenessResult().getAlert()));
    }

    @Override
    public void onLivenessEvent(FaceControllerProtocol.LivenessEventInfo info) {
        if (!getController().getExpectedLivenessEvents().isEmpty()) {
            if (info.allLivenessEventsDetected()) {
                vibrate();
                stopCameraPreview();
                stopCapture();
                reportAuthenticationInProgress();
                onAuthenticateWait(true);
                getController().authenticateImage(info.getImage(), getCameraRotationDegrees(), new VerifyResultListener());
            }
        }
    }

    protected void reportAuthenticationInProgress() {
        showMessage(R.string.face_verify);
    }

    @ExperimentalGetImage
    @Override
    protected void onRecapture() {
        stopCameraPreview();
        stopCapture();
        hideInfo();
        hideQualityIndicator();
        enableOverlay(false);
        resumeAuthenticationProcessing();
    }

    @ExperimentalGetImage
    protected void resumeAuthenticationProcessing() {
        capturedImage = null;
        getController().resumeAuthenticationProcessing();
        hideInfo();
        removePreview();
        startCameraPreview();
        enableOverlay(true);
        setInfoUpdate(true);
    }

    protected void removePreview() {
        setPreviewFrameCapture(false);
    }

    private class VerifyResultListener implements FaceControllerProtocol.VerifyResultListener {

        @Override
        public void onVerifyResult(int errorCode, RecognitionResult result, YUV collectedImage) {
            if (errorCode == ErrorCodes.NO_ERROR) {
                // Face recognized
                removePreview();
                showMessage(R.string.face_verify_complete);
            } else {
                // Face not recognized
                String msg = getString(R.string.face_verify_failed_with_score, result.getScore(), getController().getRecognitionThreshold());
                showMessage(msg);
            }
        }
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.face_verify_failed;
    }

    @Override
    protected int getCaptureSuccessMessageId() {
        return R.string.face_verify_complete;
    }

    @Override
    protected int getCaptureWarningMessageId() {
        return R.string.face_verify_warning;
    }

    private void authenticate(YUV image) {
        vibrate();
        stopCameraPreview();
        stopCapture();
        reportAuthenticationInProgress();
        onAuthenticateWait(true);
        getController().authenticateImage(image, getCameraRotationDegrees(), new VerifyResultListener());
    }

    @Override
    protected void stop() {
        super.stop();
        capturedImage = null;
    }
}
