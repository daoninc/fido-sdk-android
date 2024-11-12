package com.daon.fido.sdk.sample.kt.face

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.sdk.sample.kt.BaseViewModel
import com.daon.sdk.authenticator.Authenticator
import com.daon.sdk.authenticator.ErrorCodes
import com.daon.sdk.authenticator.controller.CaptureCompleteListener
import com.daon.sdk.authenticator.controller.CaptureCompleteResult
import com.daon.sdk.authenticator.exception.ControllerInitializationException
import com.daon.sdk.face.LivenessResult
import com.daon.sdk.face.Result
import com.daon.sdk.face.capture.FaceDetectionListener
import com.daon.sdk.face.capture.PhotoListener
import com.daon.sdk.faceauthenticator.controller.FaceControllerProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class representing the UI state for the face capture process.
 */
data class FaceUIState(
    val retakePhotoEnabled: Boolean = false,
    val doneButtonEnabled: Boolean = false,
    val infoTextVisible: Boolean = false,
    val infoText: String = "",
    val previewImageVisible: Boolean = false,
    val previewImage: Bitmap? = null,
    val isEnrollment: Boolean = true,
    val inProgress: Boolean = false,
    val recaptureEnabled: Boolean = false
)

/**
 * Data class representing the state of the face capture completion.
 */
data class FaceCaptureCompleteState(
    val captureComplete: Boolean = false,
    val captureMessage: String? = null
)

/**
* FaceViewModel manages the face capture process, registration and authentication.
* @param application The application context.
* @param fido The IXUAF instance.
* @param prefs The SharedPreferences instance.
*/
@HiltViewModel
class FaceViewModel @Inject constructor(
    application: Application,
    private val fido: IXUAF,
    private val prefs: SharedPreferences
): BaseViewModel(application) {

    private val _faceUIState = MutableStateFlow(
        FaceUIState()
        )
    val faceUIState = _faceUIState

    private val _faceCaptureState = MutableStateFlow(
        FaceCaptureCompleteState()
    )
    val faceCaptureState = _faceCaptureState


    lateinit var faceController: FaceControllerProtocol
    private lateinit var  lifecycleOwner: LifecycleOwner

    /**
     * Starts the face capture process.
     * @param lifecycleOwner The LifecycleOwner associated with the capture process.
     * @param previewView The PreviewView to display the camera preview.
     */
    fun startCapture(lifecycleOwner: LifecycleOwner, previewView: PreviewView? = null) {

      try {
          val aaidValue = prefs.getString("selectedAaid", null)
          faceController =
              aaidValue?.let { fido.getController(getApplication(), it) } as FaceControllerProtocol

          this.lifecycleOwner = lifecycleOwner

          faceController.startFaceCapture(
              getApplication<Application>().baseContext,
              lifecycleOwner,
              previewView,
              Bundle(),
              FaceDetectionHandler(),
              PhotoHandler(),
              CaptureCompleteHandler()
          )

          getAuthenticationMode()
      } catch (e: ControllerInitializationException) {
          _faceCaptureState.update { currentUIState ->
                currentUIState.copy(
                    captureMessage = "Error: ${e.message}",
                    captureComplete = true
                )
            }

          cancelCurrentOperation()
      }
    }

    /**
     * Stops the face capture process.
     */
    fun stopCapture() {
        if (this::faceController.isInitialized)
            faceController.stopFaceCapture()
    }

    /**
     * Handles the face detection result.
     * */
   inner class FaceDetectionHandler: FaceDetectionListener {
       override fun faceDetection(result: Result) {
           var msg = getQualityMessage(result)
           val detected = getLivenessMessage(result)
           if (detected.isNotEmpty()) {
               msg = "$msg\n\n$detected"
           }
           _faceUIState.update { currentUIState ->
               currentUIState.copy(
                   infoTextVisible = true,
                   infoText = msg
               )
           }
       }
   }

    /**
     * Handles the photo capture result.
      */
   inner class PhotoHandler: PhotoListener {
       override fun photo(bitmap: Bitmap) {
           Log.d("DAON", "PhotoListenerImpl photo")
           _faceUIState.update { currentUIState ->
               currentUIState.copy(
                   previewImageVisible = true,
                   previewImage = bitmap,
                   retakePhotoEnabled = true,
                   doneButtonEnabled = true,
                   infoText = "",
                   infoTextVisible = false
               )
           }
       }

   }

    /**
     * Handles the face capture completion result.
      */
   inner class CaptureCompleteHandler: CaptureCompleteListener {
       override fun onCaptureComplete(result: CaptureCompleteResult?) {
           if (result != null) {
               when (result.type) {
                   CaptureCompleteResult.Type.TERMINATE_SUCCESS -> {
                       resetFaceUIState()
                       _faceCaptureState.update { currentUIState ->
                            currentUIState.copy(
                                 captureComplete = true,
                            )
                          }
                     }
                   CaptureCompleteResult.Type.SERVER_VALIDATION_ERROR -> {
                       if (result.error.message != null) {
                           _faceCaptureState.update { currentUIState ->
                               currentUIState.copy(
                                   captureMessage = result.error.message +
                                           "\n" + getRetriesRemainingMessage(result)
                               )
                           }
                       } else {
                           _faceCaptureState.update { currentUIState ->
                               currentUIState.copy(
                                   captureMessage =  getRetriesRemainingMessage(result)
                               )
                           }
                       }

                       onRecapture()
                   }
                   CaptureCompleteResult.Type.TERMINATE_FAILURE -> {
                       resetFaceUIState()
                       _faceCaptureState.update { currentUIState ->
                           currentUIState.copy(
                               captureComplete = true,
                               )
                       }
                       faceController.cancelCapture()
                     }
                   CaptureCompleteResult.Type.CLIENT_ERROR -> {
                       resetFaceUIState()
                       if (faceController.isRegistration) {
                           _faceCaptureState.update { currentUIState ->
                               currentUIState.copy(
                                   captureComplete = true,
                                   captureMessage = "Face Registration Failed"
                               )
                           }
                       } else {
                           _faceCaptureState.update { currentUIState ->
                               currentUIState.copy(
                                   captureComplete = true,
                                   captureMessage = "Face Authentication Failed"
                               )
                           }
                       }

                   }
                   CaptureCompleteResult.Type.CLIENT_VALIDATION_ERROR -> {
                       if (result.lockInfo.state == Authenticator.Lock.UNLOCKED) {
                           if (result.info.getBoolean(CaptureCompleteResult.InfoKey.IS_WARN_ATTEMPT,
                                   false)) {
                               _faceCaptureState.update { currentUIState ->
                                   currentUIState.copy(
                                       captureMessage =
                                       "Detecting multiple failed matches, please adjust your device or lighting condition and try again."
                                   )
                               }
                           } else {
                               _faceCaptureState.update { currentUIState ->
                                   currentUIState.copy(
                                       captureMessage = getRetriesRemainingMessage(result)
                                   )
                               }
                           }
                           onRecapture()
                       } else {
                           resetFaceUIState()
                           if (result.lockInfo.state == Authenticator.Lock.TEMPORARY) {
                               _faceCaptureState.update { currentUIState ->
                                   currentUIState.copy(
                                       captureComplete = true,
                                       captureMessage = "Too many authentication attempts. The authenticator is locked for ${result.lockInfo.seconds} seconds. Please try later."
                                   )
                               }
                           } else if (result.lockInfo.state == Authenticator.Lock.PERMANENT) {
                               _faceCaptureState.update { currentUIState ->
                                   currentUIState.copy(
                                       captureComplete = true,
                                       captureMessage = "Too many authentication attempts. The authenticator is locked."
                                   )
                               }
                           }
                       }
                   }
               }
           }
       }

   }

    /**
     * Returns the message indicating the number of retries remaining.
     * @param result The CaptureCompleteResult.
     * @return The message indicating the number of retries remaining.
     */
    private fun getRetriesRemainingMessage(result: CaptureCompleteResult): String {
        val retryMessage: String
        val numberOfRetries = result.info.getInt(CaptureCompleteResult.InfoKey.RETRIES_REMAINING, -1)
        retryMessage = if (numberOfRetries > 0) {
            if (numberOfRetries ==1) {
                "1 retry remaining"
            } else {
                "$numberOfRetries retries remaining"
            }
        } else {
            "Please try again later"
        }
        return retryMessage
    }

    /**
     * Returns the message indicating the quality of the captured image.
     * @param result The Result of the capture.
     * @return The message indicating the quality of the captured image.
     */
    private fun getQualityMessage(result: Result) : String {
        if (!result.isDeviceUpright) {
            return "Hold device upright"
        } else if (result.qualityResult.hasMask()) {
            return "Remove medical mask"
        } else if (!result.qualityResult.isFaceCentered) {
            return "Keep face centered"
        } else if (!result.qualityResult.hasAcceptableEyeDistance()) {
            return "Move device closer"
        } else if (!result.qualityResult.hasAcceptableQuality()) {
            val goodLighting = result.qualityResult.hasAcceptableExposure() &&
                    result.qualityResult.hasUniformLighting() &&
                    result.qualityResult.hasAcceptableGrayscaleDensity()

            if (!goodLighting) {
                return "Improve lighting conditions"
            }

            return "Low quality image"

        } else if (result.livenessResult.alert == LivenessResult.ALERT_FACE_TOO_FAR) {
            return "Move device closer"
        } else if (result.livenessResult.alert == LivenessResult.ALERT_FACE_TOO_NEAR) {
            return "Move device further away"
        }

        return "Look alive!"
    }

    // Returns the message indicating the liveness of the captured image.
    private fun getLivenessMessage(result: Result) : String {
        if (result.livenessResult.isBlink)
            return "Blink detected"
        else if (result.livenessResult.isPassive)
            return "Passive liveness detected"
        else if (result.livenessResult.spoofDetected())
            return "Spoof detected"
        return ""
    }

    /**
     * Resets the UI state and face capture process.
     */
    fun onRecapture() {
        _faceUIState.update { currentUIState ->
            currentUIState.copy(
                recaptureEnabled = true
            )
        }
    }

    fun onRecapture(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        stopCapture()
        resetFaceUIState()
        resetFaceCaptureState()
        startCapture(lifecycleOwner, previewView)
    }

    // resets the UI state to its initial values
    fun resetFaceUIState() {
        _faceUIState.update { currentUIState ->
            currentUIState.copy(
                retakePhotoEnabled = false,
                doneButtonEnabled = false,
                infoTextVisible = false,
                infoText = "",
                previewImageVisible = false,
                previewImage = null,
                isEnrollment = false,
                inProgress = false,
                recaptureEnabled = false
            )
        }
    }

    // resets the face capture state to its initial values
    fun resetFaceCaptureState() {
        _faceCaptureState.update { currentUIState ->
            currentUIState.copy(
                captureComplete = false,
                captureMessage = null
            )
        }
    }

    /**
     * Gets the authentication mode.
     */
    private fun getAuthenticationMode() {
            _faceUIState.update { currentUIState ->
                currentUIState.copy(
                    isEnrollment = faceController.isEnrol
                )
            }
    }

    /**
     * Cancels the current operation.
     */
    fun cancelCurrentOperation() {
        viewModelScope.launch(Dispatchers.Default) {
            fido.cancelCurrentOperation()
        }
    }

    /**
     * Registers the captured image.
     */
    fun register() {
        _faceUIState.update { currentUIState ->
            currentUIState.copy(
                inProgress = true,
                retakePhotoEnabled = false,
                doneButtonEnabled = false
            )
        }
        faceController.register { errorCode, result, image ->
            _faceUIState.update { currentUIState ->
                currentUIState.copy(
                    inProgress = false
                )
            }
            // Handle registration response
            if (errorCode == ErrorCodes.NO_ERROR) {
                _faceCaptureState.update { currentUIState ->
                    currentUIState.copy(
                        captureMessage = "Registration Successful !!"
                    )
                }

            } else {
                _faceCaptureState.update { currentUIState ->
                    currentUIState.copy(
                        captureMessage = "Registration Failed :$errorCode"
                    )
                }
            }
        }
    }

    /**
     * Authenticates the captured image.
     */
    fun authenticate() {
        _faceUIState.update { currentUIState ->
            currentUIState.copy(
                inProgress = true,
                retakePhotoEnabled = false,
                doneButtonEnabled = false
            )
        }
        faceController.authenticate { errorCode, result, image ->
            _faceUIState.update { currentUIState ->
                currentUIState.copy(
                    inProgress = false
                )
            }
            // Handle authentication response
            if (errorCode == ErrorCodes.NO_ERROR) {
                // Authentication successful
                _faceCaptureState.update { currentUIState ->
                    currentUIState.copy(
                        captureMessage = "Authentication Successful !!"
                    )
                }
            } else {
                // Authentication failed
                _faceCaptureState.update { currentUIState ->
                    currentUIState.copy(
                        captureMessage = "Authentication Failed :$errorCode"
                    )
                }
            }

        }

    }

}