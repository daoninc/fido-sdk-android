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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;

import com.daon.fido.sdk.sample.basic.R;
import com.daon.fido.sdk.sample.basic.fragments.BaseCaptureFragment;
import com.daon.sdk.authenticator.controller.AuthenticatorError;
import com.daon.sdk.authenticator.exception.ControllerInitializationException;
import com.daon.sdk.device.IXAErrorCodes;
import com.daon.sdk.face.LivenessResult;
import com.daon.sdk.face.Result;
import com.daon.sdk.face.capture.FaceDetectionListener;
import com.daon.sdk.face.capture.PhotoListener;
import com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol;


public abstract class BaseFaceFragment extends BaseCaptureFragment implements FaceDetectionListener, PhotoListener {
    protected static final int RESUME_PROCESSING_DELAY_MILLIS = 500;

    private ExplicitPermission explicitCameraPermission = null;
    private boolean cameraPermissionGranted;

    protected PreviewView previewView;
    protected ImageView photo;
    protected TextView infoText;
    protected Button takePhotoButton;
    protected Button doneButton;

    public FaceControllerProtocol getController() {
        return (FaceControllerProtocol) super.getController();
    }

    @Override
    protected void start() {
        super.start();
        if (explicitCameraPermission == null) {
            if (checkPermissions(Manifest.permission.CAMERA)) {
                cameraPermissionGranted = true;
            }
        } else {
            if (explicitCameraPermission != ExplicitPermission.GRANTED) {
                if (getController() != null)
                    getController().completeCaptureWithError(new AuthenticatorError(IXAErrorCodes.ERROR_HW_UNAVAILABLE, getString(R.string.face_camera_access_denied)));
                terminateParentActivityWithError(IXAErrorCodes.ERROR_HW_UNAVAILABLE, getString(R.string.face_camera_access_denied));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cameraPermissionGranted) {
            startFaceCapture();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopFaceCapture();
    }

    @Override
    protected void onPermissionResult(Boolean result) {
        if (result) {
            explicitCameraPermission = ExplicitPermission.GRANTED;
            cameraPermissionGranted = true;
        } else {
            explicitCameraPermission = ExplicitPermission.DENIED;
            getController().completeCaptureWithError(new AuthenticatorError(IXAErrorCodes.ERROR_HW_UNAVAILABLE, getString(R.string.face_camera_access_denied)));
            terminateParentActivityWithError(IXAErrorCodes.ERROR_HW_UNAVAILABLE, getString(R.string.face_camera_access_denied));
        }
    }

    protected void startFaceCapture() {
        enablePreview();

        try {
            getController().startCamera(getContext(),
                    getViewLifecycleOwner(),
                    previewView, new Bundle(),
                    this,
                    this,
                    new DefaultCaptureCompleteListener());
            getController().startFaceCapture();
        } catch (ControllerInitializationException e) {
            getController().completeCaptureWithError(new AuthenticatorError(IXAErrorCodes.ERROR_HW_UNAVAILABLE, getString(R.string.face_camera_access_denied)));
            terminateParentActivityWithError(IXAErrorCodes.ERROR_HW_UNAVAILABLE, getString(R.string.face_camera_access_denied));
        }
    }

    protected void stopFaceCapture() {
        removePreviewImage();
        if (getController() != null) {
            getController().stopFaceCapture();
        }
    }

    @Override
    public void faceDetection(@NonNull Result result) {
        String message = getQualityMessage(result);
        String livenessMessage = getLivenessMessage(result);
        if (!livenessMessage.isEmpty()) {
            message = message + " - " + livenessMessage;
        }
        setInfo(message);

    }

    @Override
    public void photo(@NonNull Bitmap bitmap) {
        hideInfo();
        disablePreview();

        setPreviewImage(bitmap);

        // retake and enroll buttons visible
        if (doneButton != null)
            doneButton.setVisibility(View.VISIBLE);

        if (takePhotoButton != null)
            takePhotoButton.setVisibility(View.VISIBLE);
    }

    private String getQualityMessage(Result result) {
        if (!result.isDeviceUpright()) {
            return "Hold device upright";
        } else if (result.getQualityResult().hasMask()) {
            return "Remove medical mask";
        } else if (!result.getQualityResult().isFaceCentered()) {
            return "Keep face centered";
        } else if (!result.getQualityResult().hasAcceptableEyeDistance()) {
            return "Move device closer";
        } else if (!result.getQualityResult().hasAcceptableQuality()) {
            boolean goodLighting = result.getQualityResult().hasAcceptableExposure() && result.getQualityResult().hasUniformLighting() && result.getQualityResult().hasAcceptableGrayscaleDensity();

            if (!goodLighting) {
                return "Improve lighting conditions";
            }

            return "Low quality image";

        } else if (result.getLivenessResult().getAlert() == LivenessResult.ALERT_FACE_TOO_FAR) {
            return "Move device closer";
        } else if (result.getLivenessResult().getAlert() == LivenessResult.ALERT_FACE_TOO_NEAR) {
            return "Move device further away";
        }

        return "Look alive!";
    }

    private String getLivenessMessage(Result result) {
        if (result.getLivenessResult().isBlink()) return "Blink detected";
        else if (result.getLivenessResult().isPassive()) return "Passive liveness detected";
        else if (result.getLivenessResult().spoofDetected()) return "Spoof detected";
        return "";
    }

    protected void vibrate() {
        Activity activity = getActivity();
        if (activity != null) {
            Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) vibrator.vibrate(200);
        }
    }

    protected void setInfo(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (infoText != null) {
                    infoText.setText(message);
                    infoText.setTextColor(getResources().getColor(R.color.white));
                    infoText.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    protected void hideInfo() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (infoText != null) {
                    infoText.setVisibility(View.GONE);
                }
            });
        }
    }

    protected void setPreviewImage(Bitmap bmp) {
        if (getActivity() != null) {
            ViewGroup layout = getActivity().findViewById(R.id.previewLayout);
            if (layout != null) {

                photo = new ImageView(getActivity());

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(layout.getLayoutParams().width, layout.getLayoutParams().height);
                photo.setLayoutParams(params);
                photo.setImageBitmap(bmp);

                layout.addView(photo);
            }
        }
    }

    protected void removePreviewImage() {
        if (photo != null)
            photo.setImageDrawable(null);

        if (takePhotoButton != null)
            takePhotoButton.setVisibility(View.GONE);

        if (doneButton != null)
            doneButton.setVisibility(View.GONE);
    }

    protected void enablePreview() {
        if (previewView != null) {
            previewView.setVisibility(View.VISIBLE);
        }
    }

    protected void disablePreview() {
        if (previewView != null) {
            previewView.setVisibility(View.GONE);
        }
    }

    protected void retakePhoto() {
        stopFaceCapture();
        startFaceCapture();
    }

    protected void retakePhotoDelayed() {
        new Handler().postDelayed(this::retakePhoto, RESUME_PROCESSING_DELAY_MILLIS);
    }

}
