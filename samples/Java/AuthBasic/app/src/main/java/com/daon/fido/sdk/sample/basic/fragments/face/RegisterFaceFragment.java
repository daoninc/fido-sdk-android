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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;

import com.daon.fido.sdk.sample.basic.R;
import com.daon.sdk.authenticator.ErrorCodes;
import com.daon.sdk.authenticator.controller.AuthenticatorError;
import com.daon.sdk.authenticator.controller.LockResult;
import com.daon.sdk.face.Result;
import com.daon.sdk.face.YUV;
import com.daon.sdk.faceauthenticator.FaceErrorCodes;
import com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol;

public class RegisterFaceFragment extends BaseFaceFragment {
    private enum PhotoMode {
        DETECT, TAKE, RETAKE
    }

    private PhotoMode photoMode = PhotoMode.DETECT;
    private Button takePhotoButton;
    private Button doneButton;
    private YUV capturedImage;

    private ImageView photo;
    private ImageView check;

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.daon_register_face, container, false);

        if (v != null) {
            takePhotoButton = v.findViewById(R.id.takePhotoButton);
            takePhotoButton.setOnClickListener(v1 -> {
                if (photoMode == PhotoMode.TAKE)
                    takePhoto();
                else
                    retakePhoto();
            });

            doneButton = v.findViewById(R.id.doneButton);
            doneButton.setOnClickListener(v12 -> enroll());

            getController().setImageQualityChecker((result, controllerConfiguration) ->
                    result.getQualityResult().hasAcceptableQuality() &&
                    result.getQualityResult().isFaceCentered() &&
                    result.isDeviceUpright() &&
                    result.getQualityResult().hasAcceptableEyeDistance());
        }

        return v;
    }

    @Override
    public void onImageAnalyzed(YUV yuv, Result result, boolean isQualityImage) {
        super.onImageAnalyzed(yuv, result, isQualityImage);

        if (!isEnableInfoText()) return;

        if (result.hasMask()) setWarning(R.string.face_mask_detected);

        if (!getController().isLivenessEnabled()) {

            checkAndUpdateQualityInfo(result);

            // If liveness is not enabled, stop when a quality image is found.
            // Present user with option to take photo.

            if (takePhotoButton != null) {
                boolean showButton = isQualityImage && result.getQualityResult().isFaceCentered();
                takePhotoButton.setVisibility(showButton ? View.VISIBLE : View.INVISIBLE);
                if (showButton) {
                    capturedImage = yuv;
                    photoMode = PhotoMode.TAKE;
                    hideInfo();
                    hideWarning();
                }
            }
        } else {
            if (checkAndUpdateQualityInfo(result)) {
                updateInfo(result);
            }
        }

        setWarning(getAlertMessage(result.getLivenessResult().getAlert()));
    }

    @Override
    public void onLivenessEvent(FaceControllerProtocol.LivenessEventInfo info) {
        if (!getController().getExpectedLivenessEvents().isEmpty()) {
            if (info.allLivenessEventsDetected()) {
                if (takePhotoButton != null) takePhotoButton.setVisibility(View.VISIBLE);
                hideInfo();
                hideWarning();
                photoMode = PhotoMode.TAKE;
            }
        }
    }

    private void takePhoto() {
        stopCapture();
        stopCameraPreview();
        hideInfo();
        hideQualityIndicator();
        enableOverlay(false);

        photoMode = PhotoMode.RETAKE;

        if (takePhotoButton != null) takePhotoButton.setText(R.string.photo_retake);

        if (doneButton != null) doneButton.setVisibility(View.VISIBLE);

        if (getController().isLivenessEnabled()) {
            if (getController() != null) {
                capturedImage = getController().captureImage();
            }
        }

        setPreviewImage(capturedImage.toBitmap());
    }

    private void setPreviewImage(Bitmap bmp) {
        if (getActivity() != null) {
            ViewGroup layout = getActivity().findViewById(R.id.preview);
            if (layout != null) {

                photo = new ImageView(getActivity());

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(layout.getLayoutParams().width, layout.getLayoutParams().height);
                photo.setLayoutParams(params);
                photo.setImageBitmap((rotate(bmp)));

                check = new ImageView(getActivity());
                params = new FrameLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.END);
                check.setLayoutParams(params);
                check.setImageResource(R.drawable.verified);

                layout.addView(photo);
                layout.addView(check);
            }
        }
    }

    protected void removePreview() {

        setPreviewFrameCapture(false);

        if (photo != null) photo.setImageDrawable(null);
        if (check != null) check.setImageDrawable(null);
    }

    @ExperimentalGetImage
    private void retakePhoto() {
        getController().resumeRegistrationProcessing();
        hideInfo();
        removePreview();
        startCameraPreview();
        enableOverlay(true);
        setInfoUpdate(true);
        capturedImage = null;

        photoMode = PhotoMode.DETECT;

        if (takePhotoButton != null) {
            takePhotoButton.setVisibility(View.INVISIBLE);
            takePhotoButton.setText(R.string.photo_take);
        }

        if (doneButton != null) doneButton.setVisibility(View.GONE);
    }

    @ExperimentalGetImage
    private void retakePhotoDelayed() {
        new Handler().postDelayed(this::retakePhoto, RESUME_PROCESSING_DELAY_MILLIS);
    }

    private void enroll() {
        showMessage(R.string.face_enroll);

        if (doneButton != null) doneButton.setVisibility(View.GONE);

        if (takePhotoButton != null) takePhotoButton.setVisibility(View.INVISIBLE);

        if (getController() != null) {
            onAuthenticateWait(true);
            getController().registerImage(capturedImage, getCameraRotationDegrees(), new EnrolResultListener());
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    protected void onRecapture() {
        stopCameraPreview();
        stopCapture();
        enableOverlay(false);
        retakePhotoDelayed();
    }

    private class EnrolResultListener implements FaceControllerProtocol.EnrolResultListener {
        @OptIn(markerClass = ExperimentalGetImage.class)
        @Override
        public void onEnrolResult(int errorCode, Result result, YUV collectedImage) {
            switch (errorCode) {
                case ErrorCodes.NO_ERROR:
                    stopCameraPreview();
                    stopCapture();
                    setCheckMark();
                    break;
                case ErrorCodes.ERROR_ENROLL_QUALITY:
                    onAuthenticateWait(false);
                    showMessage(R.string.face_quality);
                    retakePhotoDelayed();
                    break;
                default:
                    onAuthenticateWait(false);
                    showMessage(R.string.face_enroll_failed);
                    retakePhotoDelayed();
                    break;
            }
        }
    }

    private void setCheckMark() {
        if (getActivity() != null) {
            ViewGroup layout = getActivity().findViewById(R.id.preview);
            if (layout != null) {
                ImageView check = new ImageView(getActivity());
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.END);
                check.setLayoutParams(params);
                check.setImageResource(R.drawable.verified);
                layout.addView(check);
            }
        }
    }

    @Override
    protected int getCaptureFailedMessageId() {
        return R.string.face_enroll_failed;
    }

    @Override
    protected int getCaptureSuccessMessageId() {
        return R.string.face_enroll_complete;
    }

    @Override
    public void onFailure(AuthenticatorError error, LockResult result) {
        Log.d("DAON", "RegisterFaceFragment onFailure errorCode :" + error.getCode());
        switch (error.getCode()) {
            case FaceErrorCodes.FACE_LIVENESS_AT_REG_TIMEOUT:
                showMessage(R.string.face_verify_timeout);
                setInfo(R.string.face_verify_timeout, R.color.red);
                setInfoUpdate(false);
                onRecapture();

                break;
            case FaceErrorCodes.FACE_LOST_FACE_CONTINUITY:
                showMessage(R.string.face_tracking_lost);
                setInfo(R.string.face_tracking_lost, R.color.red);
                setInfoUpdate(false);
                onRecapture();

                break;
        }
    }
}
