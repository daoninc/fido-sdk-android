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

package com.daon.fido.sdk.sample.basic.fragments.face.camera

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.WindowManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.daon.sdk.face.YUV
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val RATIO_4_3_VALUE = 4.0 / 3.0
private const val RATIO_16_9_VALUE = 16.0 / 9.0

class CameraXWrapper(
    private val viewFinder: PreviewView,
    private val mContext: Context,
    private val owner: LifecycleOwner
) {
    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    private var displayId: Int = -1
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var rotation = 0
    private lateinit var imageAnalyserSize: Size
    private lateinit var mCameraProvider: ProcessCameraProvider


    interface PreviewCallback {
        fun onPreviewFrame(yuv: YUV?)
    }

    private var previewCallback: PreviewCallback? = null

    fun setPreviewCallback(callback: PreviewCallback?) {
        previewCallback = callback
    }

    @ExperimentalGetImage
    fun startPreview(size: Size) {
        imageAnalyserSize = size
        viewFinder.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        viewFinder.post {
            // Keep track of the display in which this view is attached
            displayId = viewFinder.display.displayId
            val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            rotation = viewFinder.display.rotation

            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

            val cameraProviderFuture = ProcessCameraProvider.getInstance(mContext)
            cameraProviderFuture.addListener({
                // CameraProvider
                mCameraProvider = cameraProviderFuture.get()
                // Preview
                preview = Preview.Builder()
                    // We request aspect ratio but no resolution
                    .setTargetAspectRatio(screenAspectRatio)
                    // Set initial target rotation
                    .setTargetRotation(rotation).build()
                // Attach the viewfinder's surface provider to preview use case
                preview?.setSurfaceProvider(viewFinder.surfaceProvider)
                // ImageAnalysis
                imageAnalyzer = ImageAnalysis.Builder()
                    // We request aspect ratio but no resolution
                    //.setTargetAspectRatio(screenAspectRatio)
                    //.setTargetResolution(Size(IMAGE_HEIGHT, IMAGE_WIDTH))
                    .setTargetResolution(Size(imageAnalyserSize.height, imageAnalyserSize.width))
                    // Set initial target rotation, we will have to call this again if rotation changes
                    // during the lifecycle of this use case
                    .setTargetRotation(rotation).build()
                    // The analyzer can then be assigned to the instance
                    .also {
                        it.setAnalyzer(cameraExecutor) { image ->
                            val yuv = YUV(image.image)
                            image.close()
                            previewCallback?.onPreviewFrame(yuv)
                        }
                    }

                // Must unbind the use-cases before rebinding them
                mCameraProvider.unbindAll()
                try {
                    // A variable number of use-cases can be passed here -
                    // camera provides access to CameraControl & CameraInfo
                    camera = mCameraProvider.bindToLifecycle(
                        owner, cameraSelector, preview, imageAnalyzer
                    )
                } catch (exc: Exception) {
                    Log.e("DAON", "Use case binding failed", exc)
                }

            }, ContextCompat.getMainExecutor(mContext))
        }
    }

    fun stopPreview() {
        if (this::mCameraProvider.isInitialized) {
            mCameraProvider.unbindAll();
            cameraExecutor.shutdown()
        }
    }

    /**
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    fun getDegreesToRotate(): Int {
        val windowManager: WindowManager =
            mContext.getSystemService(WINDOW_SERVICE) as WindowManager
        val deviceRotation = windowManager.defaultDisplay.rotation
        val surfaceRotation = when (deviceRotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }

        val jpegRotation = (surfaceRotation + rotation + 270) % 360
        return jpegRotation
    }
}

