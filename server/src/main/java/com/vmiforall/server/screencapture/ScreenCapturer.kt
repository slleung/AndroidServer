package com.vmiforall.server.screencapture

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.util.DisplayMetrics
import android.view.Surface
import org.webrtc.*

private const val DISPLAY_FLAGS =
    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR

// TODO need to check if the value actually changes anything
// the client should be able to set this, but it may have to recreate the VM
// I think we can just get this value using the current display metrics of the VM
private const val VIRTUAL_DISPLAY_DPI = DisplayMetrics.DENSITY_400

// TODO use VM default?
private const val MAX_FRAME_RATE = 60

/**
 * A screen capturer for webrtc. Heavily modified from {@code org.webrtc.ScreenCapturerAndroid}.
 *
 * Instead of using the MediaProjection API. We use the display manager directly as we have system
 * privilege.
 */
class ScreenCapturer : VideoCapturer, VideoSink {

    private lateinit var surfaceTextureHelper: SurfaceTextureHelper
    private lateinit var displayManager: DisplayManager
    private lateinit var capturerObserver: CapturerObserver

    private var width = 0
    private var height = 0
    private var frameRate = 0
    private var virtualDisplay: VirtualDisplay? = null
    private var isDisposed = false

    override fun initialize(
        surfaceTextureHelper: SurfaceTextureHelper,
        applicationContext: Context,
        capturerObserver: CapturerObserver
    ) {
        checkNotDisposed()

        this.surfaceTextureHelper = surfaceTextureHelper
        this.displayManager =
            applicationContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        this.capturerObserver = capturerObserver
    }

    override fun startCapture(width: Int, height: Int, frameRate: Int) {
        checkNotDisposed()

        this.width = width
        this.height = height
        this.frameRate =
            if (frameRate > MAX_FRAME_RATE || frameRate <= 0) MAX_FRAME_RATE else frameRate

        createVirtualDisplay()

        capturerObserver.onCapturerStarted(true)
        surfaceTextureHelper.setTextureSize(width, height)
        surfaceTextureHelper.startListening(this)
    }

    override fun changeCaptureFormat(width: Int, height: Int, frameRate: Int) {
        checkNotDisposed()

        this.width = width
        this.height = height
        this.frameRate =
            if (frameRate > MAX_FRAME_RATE || frameRate <= 0) MAX_FRAME_RATE else frameRate

        if (virtualDisplay == null) {
            // Capturer is stopped, the virtual display will be created in startCaptuer().
            return
        }

        // Create a new virtual display on the surfaceTextureHelper thread to avoid interference
        // with frame processing, which happens on the same thread (we serialize events by running
        // them on the same thread).
        ThreadUtils.invokeAtFrontUninterruptibly(surfaceTextureHelper.handler) {
            virtualDisplay?.release()
            surfaceTextureHelper.setTextureSize(width, height)
            createVirtualDisplay()
        }
    }

    private fun createVirtualDisplay() {
        val surface = Surface(surfaceTextureHelper.surfaceTexture)
        surface.setFrameRate(frameRate.toFloat(), Surface.FRAME_RATE_COMPATIBILITY_DEFAULT)

        virtualDisplay = displayManager.createVirtualDisplay(
            "WebRTC_ScreenCapture", width, height,
            VIRTUAL_DISPLAY_DPI, surface, DISPLAY_FLAGS
        )
    }

    override fun stopCapture() {
        checkNotDisposed()

        ThreadUtils.invokeAtFrontUninterruptibly(surfaceTextureHelper.handler) {
            surfaceTextureHelper.stopListening()
            capturerObserver.onCapturerStopped()

            virtualDisplay?.apply {
                release()
                virtualDisplay = null
            }
        }
    }

    override fun onFrame(frame: VideoFrame?) {
        capturerObserver.onFrameCaptured(frame)
    }

    override fun isScreencast(): Boolean {
        return true
    }

    override fun dispose() {
        isDisposed = true
    }

    private fun checkNotDisposed() {
        if (isDisposed) {
            throw RuntimeException("capturer is disposed.")
        }
    }

}
