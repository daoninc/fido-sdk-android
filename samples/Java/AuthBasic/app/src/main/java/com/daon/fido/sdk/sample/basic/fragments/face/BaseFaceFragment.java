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
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.daon.fido.sdk.sample.basic.R;
import com.daon.fido.sdk.sample.basic.fragments.BaseCaptureFragment;
import com.daon.fido.sdk.sample.basic.fragments.face.camera.CameraXWrapper;
import com.daon.sdk.authenticator.Authenticator;
import com.daon.sdk.authenticator.ErrorCodes;
import com.daon.sdk.authenticator.controller.AuthenticatorError;
import com.daon.sdk.authenticator.controller.LockResult;
import com.daon.sdk.device.IXAErrorCodes;
import com.daon.sdk.face.Config;
import com.daon.sdk.face.LivenessResult;
import com.daon.sdk.face.QualityResult;
import com.daon.sdk.face.Result;
import com.daon.sdk.face.YUV;
import com.daon.sdk.faceauthenticator.FaceErrorCodes;
import com.daon.sdk.faceauthenticator.YUVTools;
import com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol;

/** @noinspection SpellCheckingInspection */
public abstract class BaseFaceFragment extends BaseCaptureFragment implements FaceControllerProtocol.FaceAnalysisListener {
    protected static final int RESUME_PROCESSING_DELAY_MILLIS = 500;
    private static final long MESSAGE_DELAY_MS = 500;

    private ImageView overlay = null;
    private ImageView qualityIndicator;
    protected TextView info_text;
    private TextView warningTextView;
    private ExplicitPermission explicitCameraPermission = null;
    private boolean cameraPermissionGranted;
    private final Handler handler = new Handler();
    private boolean enableInfoText = true;

    private CameraXWrapper cameraXWrapper;
    private long nextMessageUpdate;

    public FaceControllerProtocol getController() {
        return (FaceControllerProtocol) super.getController();
    }

    /**
     * @return number of degrees clockwise from portrait (0 degrees) that the camera image is rotated
     */
    protected int getCameraRotationDegrees() {
        return YUVTools.mirroredAngle(cameraXWrapper.getDegreesToRotate());
    }

    protected void createPreview() {

        if (getActivity() != null) {
            ViewGroup layout = getActivity().findViewById(R.id.preview);

            if (getController().getPassiveLivenessEvent() == FaceControllerProtocol.LivenessEvent.SERVER && getController().getType() == Authenticator.Type.ADOS) {
                ConstraintLayout.LayoutParams constraintParams = (ConstraintLayout.LayoutParams) layout.getLayoutParams();
                constraintParams.dimensionRatio = "W,9:16";
                layout.setLayoutParams(constraintParams);
            }

            if (layout != null) {
                FrameLayout cameraOverlayLayout = new FrameLayout(getActivity());
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                cameraOverlayLayout.setLayoutParams(layoutParams);

                PreviewView previewView = getActivity().findViewById(R.id.view_finder);
                cameraXWrapper = new CameraXWrapper(previewView, getActivity(), this);

                int res = getResources().getIdentifier(getActivity().getPackageName() + ":drawable/preview_overlay", null, null);
                if (res > 0) {
                    overlay = new ImageView(getActivity());
                    FrameLayout.LayoutParams faceCaptureOverlayLayoutParams = new FrameLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, Gravity.CENTER);
                    overlay.setLayoutParams(faceCaptureOverlayLayoutParams);
                    overlay.setBackgroundResource(res);
                    cameraOverlayLayout.addView(overlay);
                }

                info_text = new TextView(getActivity());
                FrameLayout.LayoutParams faceCaptureInfoLayoutParams = new FrameLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.TOP);
                info_text.setPadding(10, 10, 10, 10);
                info_text.setLayoutParams(faceCaptureInfoLayoutParams);
                info_text.setBackgroundColor(Color.argb(50, 0, 0, 0));
                info_text.setVisibility(View.GONE);
                cameraOverlayLayout.addView(info_text);

                qualityIndicator = new ImageView(getActivity());
                FrameLayout.LayoutParams faceCaptureIndicatorLayoutParams = new FrameLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.END);
                qualityIndicator.setPadding(25, 25, 25, 25);
                faceCaptureIndicatorLayoutParams.topMargin = 150;
                qualityIndicator.setLayoutParams(faceCaptureIndicatorLayoutParams);
                qualityIndicator.setImageResource(R.drawable.image_quality_indicator);
                qualityIndicator.setVisibility(View.GONE);
                cameraOverlayLayout.addView(qualityIndicator);

                layout.addView(cameraOverlayLayout);
                layout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        enableOverlay(true);
        setInfoUpdate(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        hideInfo();
        hideQualityIndicator();
        enableOverlay(false);
    }

    @Override
    protected void onPermissionResult(Boolean result) {
        if (result) {
            explicitCameraPermission = ExplicitPermission.GRANTED;
            cameraPermissionGranted = true;
        } else {
            explicitCameraPermission = ExplicitPermission.DENIED;
            completeCaptureWithError(new AuthenticatorError(IXAErrorCodes.ERROR_HW_UNAVAILABLE, getString(R.string.face_camera_access_denied)));
        }
    }

    @ExperimentalGetImage
    @Override
    protected void start() {
        super.start();
        if (explicitCameraPermission == null) {
            if (checkPermissions(Manifest.permission.CAMERA)) {
                cameraPermissionGranted = true;
            }
        } else {
            if (explicitCameraPermission != ExplicitPermission.GRANTED) {
                if (getController() != null) {
                    getController().completeCaptureWithError(new AuthenticatorError(IXAErrorCodes.ERROR_HW_UNAVAILABLE, getString(R.string.face_camera_access_denied)));
                }
            }
        }

        if (cameraPermissionGranted) {
            startCameraPreview();
        }
    }

    @Override
    protected void stop() {
        super.stop();
        stopCameraPreview();
        stopCapture();
    }

    /**********************************
     * FaceAnalysisListener callbacks
     *********************************/

    @Override
    public void onImageAnalyzed(YUV yuv, Result result, boolean isQualityImage) {
        if (qualityIndicator != null) {
            if (result != null && result.isDeviceUpright() && isQualityImage)
                qualityIndicator.setVisibility(View.VISIBLE);
            else qualityIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAlert(int alert) {
        Log.d("DAON", "onAlert ---  " + alert);
    }

    protected int getAlertMessage(int alert) {

        return switch (alert) {
            case LivenessResult.ALERT_FACE_NOT_DETECTED ->
                    R.string.face_liveness_hmd_face_not_detected;
            case LivenessResult.ALERT_FACE_NOT_CENTERED ->
                    R.string.face_liveness_hmd_face_not_centered;
            case LivenessResult.ALERT_MOTION_TOO_FAST -> R.string.face_liveness_hmd_motion_too_fast;
            case LivenessResult.ALERT_MOTION_SWING_TOO_FAST ->
                    R.string.face_liveness_hmd_motion_swing_too_fast;
            case LivenessResult.ALERT_MOTION_TOO_FAR -> R.string.face_liveness_hmd_motion_too_far;
            case LivenessResult.ALERT_FACE_TOO_CLOSE_TO_EDGE ->
                    R.string.face_liveness_hmd_too_close_to_edge;
            case LivenessResult.ALERT_FACE_TOO_NEAR -> R.string.face_liveness_hmd_too_near;
            case LivenessResult.ALERT_FACE_TOO_FAR -> R.string.face_liveness_hmd_too_far;
            case LivenessResult.ALERT_LIVENESS_SPOOF -> R.string.face_liveness_hmd_spoof;
            case LivenessResult.ALERT_INSUFFICIENT_FACE_DATA ->
                    R.string.face_liveness_hmd_insufficient_face_data;
            case LivenessResult.ALERT_INSUFFICIENT_FRAME_DATA ->
                    R.string.face_liveness_hmd_insufficient_frame_data;
            case LivenessResult.ALERT_FRAME_MISMATCH -> R.string.face_liveness_hmd_frame_mismatch;
            case LivenessResult.ALERT_NO_MOVEMENT_DETECTED ->
                    R.string.face_liveness_hmd_no_movement_detected;
            case LivenessResult.ALERT_FACE_QUALITY -> R.string.face_liveness_hmd_quality;
            case LivenessResult.ALERT_TIMEOUT -> R.string.face_liveness_timeout;
            case LivenessResult.ALERT_PERFORMANCE -> R.string.face_liveness_performance;
            default -> -1;
        };
    }

    @Override
    public void onFailure(AuthenticatorError error, LockResult result) {
        stopCameraPreview();
        stopCapture();
        switch (error.getCode()) {
            case ErrorCodes.ERROR_MAX_ATTEMPTS:
                terminateParentActivityWithError(error.getCode(), error.getMessage());
            case FaceErrorCodes.FACE_LIVENESS_AT_AUTH_TIMEOUT:
            case FaceErrorCodes.FACE_REC_TIMEOUT:
            case FaceErrorCodes.FACE_VERIFY_TIMEOUT_NO_FACE_FOUND:
                showMessage(R.string.face_verify_timeout);
                setInfoUpdate(false);
                setInfo(R.string.face_verify_timeout, R.color.red);
                if (result != null) {
                    if (result.getLockInfo().getState() == Authenticator.Lock.UNLOCKED) {
                        onRecapture();
                    } else {
                        stopCameraPreview();
                        stopCapture();
                        terminateParentActivityWithError(error.getCode(), error.getMessage());
                    }
                } else {
                    stopCameraPreview();
                    stopCapture();
                    terminateParentActivityWithError(error.getCode(), error.getMessage());
                }
                break;
            case FaceErrorCodes.FACE_LOST_FACE_CONTINUITY:
                stopCameraPreview();
                stopCapture();
                showMessage(error.getMessage());
                terminateParentActivityWithError(error.getCode(), error.getMessage());
                break;
        }
    }

    /****************************************
     * End of FaceAnalysisListener callbacks
     ***************************************/

    @ExperimentalGetImage
    protected void startCameraPreview() {

        createPreview();

        if (getActivity() != null) warningTextView = getActivity().findViewById(R.id.warning);

        // Start camera preview
        //Size size;
        Size previewSize;
        if (getController().getPassiveLivenessEvent() == FaceControllerProtocol.LivenessEvent.SERVER && getController().getType() == Authenticator.Type.ADOS) {
            previewSize = new Size(1280, 720);
        } else {
            previewSize = new Size(640, 480);
        }
        cameraXWrapper.startPreview(previewSize);
        startFaceCapture(previewSize.getWidth(), previewSize.getHeight(), this);

        // Set preview frame callback and start collecting frames.
        // Frames are in the NV21 format YUV encoding.
        // 500ms delay ,to make sure the camera is ready
        handler.postDelayed(() -> setPreviewFrameCapture(true), 500);
    }

    protected void startFaceCapture(int imageWidth, int imageHeight, FaceControllerProtocol.FaceAnalysisListener listener) {

        // Setting the quality threshold
        Bundle config = new Bundle();
        config.putInt(Config.QUALITY_THRESHOLD_EYE_DISTANCE, 100); // Default is 90

        getController().setConfiguration(config);
        getController().startFaceCapture(imageWidth, imageHeight, listener, new DefaultCaptureCompleteListener());
    }

    protected void setPreviewFrameCapture(boolean on) {

        if (cameraXWrapper == null) return;

        if (!on) {
            cameraXWrapper.setPreviewCallback(null);
        } else {

            cameraXWrapper.setPreviewCallback(yuv -> {
                if (getController() != null) {
                    getController().analyzeImage(yuv);
                }
            });
        }
    }

    protected void stopCameraPreview() {
        if (cameraXWrapper != null) cameraXWrapper.stopPreview();

        setPreviewFrameCapture(false);
    }

    protected void stopCapture() {
        if (getController() != null) {
            getController().stopFaceCapture();
        }
    }

    protected void setInfo(int resid) {
        if (resid > 0) {
            setInfo(resid, R.color.white);
        }
    }

    protected void setInfo(final int resid, final int color) {
        if (resid > 0) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (info_text != null) {
                        info_text.setText(resid);
                        info_text.setTextColor(getResources().getColor(color));
                        info_text.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    protected void vibrate() {
        Activity activity = getActivity();
        if (activity != null) {
            Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) vibrator.vibrate(200);
        }
    }

    protected void setWarning(int resId) {
        if (resId < 0) {
            hideWarning();
            return;
        }
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (warningTextView != null) {
                    warningTextView.setText(resId);
                    warningTextView.setTextColor(Color.RED);
                    warningTextView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    protected void hideWarning() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (warningTextView != null) {
                    warningTextView.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    protected void setInfoUpdate(boolean enable) {
        enableInfoText = enable;
    }

    /** @noinspection BooleanMethodIsAlwaysInverted*/
    boolean isEnableInfoText() {
        return enableInfoText;
    }

    protected void hideInfo() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (info_text != null) {
                    info_text.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    protected void hideQualityIndicator() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (qualityIndicator != null) {
                    qualityIndicator.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    protected void enableOverlay(boolean enable) {
        if (overlay != null) overlay.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    protected void updateInfo(final Result result) {
        if (result.isTrackingFace()) {
            if (getController().getExpectedLivenessEvents().contains(FaceControllerProtocol.LivenessEvent.BLINK)) {
                if (getController().getExpectedLivenessEvents().contains(FaceControllerProtocol.LivenessEvent.PASSIVE)) {
                    if (getController().getDetectedLivenessEvents().contains(FaceControllerProtocol.LivenessEvent.BLINK)) {
                        if (getController().getDetectedLivenessEvents().contains(FaceControllerProtocol.LivenessEvent.PASSIVE)) {
                            setInfo(R.string.photo_info, R.color.white);
                        } else {
                            setInfo(R.string.face_blink_detected_liveness_not_detected, R.color.white);
                        }
                    } else {
                        if (getController().getDetectedLivenessEvents().contains(FaceControllerProtocol.LivenessEvent.PASSIVE)) {
                            setInfo(R.string.face_blink_not_detected_liveness_detected, R.color.white);
                        } else {
                            setInfo(R.string.face_blink_liveness_not_detected, R.color.white);
                        }
                    }
                } else {
                    if (getController().getDetectedLivenessEvents().contains(FaceControllerProtocol.LivenessEvent.BLINK)) {
                        setInfo(R.string.photo_info, R.color.white);
                    } else {
                        setInfo(R.string.face_blink_not_detected, R.color.white);
                    }
                }
            } else {
                if (getController().getDetectedLivenessEvents().contains(FaceControllerProtocol.LivenessEvent.PASSIVE)) {
                    setInfo(R.string.photo_info, R.color.white);
                } else {
                    setInfo(R.string.face_liveness_not_detected, R.color.white);
                }
            }
        }
    }

    protected boolean checkAndUpdateQualityInfo(Result result) {

        QualityResult quality = result.getQualityResult();

        if (System.currentTimeMillis() < nextMessageUpdate) return false;

        nextMessageUpdate = System.currentTimeMillis() + MESSAGE_DELAY_MS;

        if (!quality.isFaceCentered()) {
            setInfo(R.string.face_not_centered);
        } else if (!quality.hasAcceptableEyeDistance()) {
            setInfo(R.string.move_closer);
        } else if (!result.getQualityResult().hasAcceptableQuality()) {
            boolean goodLighting = quality.hasAcceptableExposure() &&
                    quality.hasUniformLighting() &&
                    quality.hasAcceptableGrayscaleDensity();
            if (!goodLighting) {
                setInfo(R.string.face_quality_non_uniform_lighting);
            } else {
                setInfo(R.string.face_quality_unknown);
            }
        } else {
            // No quality errors
            setInfo(-1);
            return true;
        }

        return false;
    }

    /**
     * Rotate bitmap.
     *
     * @param bitmap the source bitmap.
     * @return the rotated bitmap.
     */
    protected Bitmap rotate(Bitmap bitmap) {
        return rotate(bitmap, cameraXWrapper.getDegreesToRotate());
    }

    /**
     * Rotate bitmap.
     *
     * @param bitmap  the source bitmap.
     * @param degrees the number of degrees to rotate.
     * @return the rotated bitmap.
     */
    protected Bitmap rotate(Bitmap bitmap, float degrees) {
        if (bitmap == null) return null;

        Matrix mtx = new Matrix();

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        mtx.postRotate(degrees);
        mtx.postScale(-1.0f, 1.0f);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }
}
