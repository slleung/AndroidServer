package com.vmiforall.server.services.handler

import android.os.SystemClock
import android.view.MotionEvent
import androidx.test.runner.MonitoringInstrumentation
import com.vmiforall.remotecontrol.RemoteControlProto
import com.vmiforall.server.di.IoDispatcher
import com.vmiforall.server.di.ServiceCoroutineScope
import com.vmiforall.server.models.toAndroidMotionEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

private val logger = LoggerFactory.getLogger(SendMotionEventStreamHandler::class.java)

private const val DOWN_TIME_UNSPECIFIED = -1L

/**
 * A request handler for [RemoteControlProto.SendMotionEventRequest].
 *
 * When the client wishes to inject a motion event to the remote instance, the motion event cannot
 * be used as-is. This is because the downTime and eventTime should use the current time as the time
 * base. We need to adjust the time parameters.
 */
@Singleton
class SendMotionEventStreamHandler @Inject constructor(
    @ServiceCoroutineScope private val serviceCoroutineScope: CoroutineScope,
    private val instrumentation: MonitoringInstrumentation,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RequestHandler<Flow<RemoteControlProto.SendMotionEventRequest>, RemoteControlProto.SendMotionEventStreamResponse> {

    private var currentDownTime = DOWN_TIME_UNSPECIFIED

    override suspend fun handle(requests: Flow<RemoteControlProto.SendMotionEventRequest>): RemoteControlProto.SendMotionEventStreamResponse {
        logger.info("Received motion event stream")

        requests
            .buffer(capacity = 64, BufferOverflow.DROP_OLDEST)
            .collect { request ->
                injectMotionEvent(request.motionEvent)
            }

        return RemoteControlProto.SendMotionEventStreamResponse.getDefaultInstance()
    }

    private suspend fun injectMotionEvent(motionEventProto: RemoteControlProto.MotionEvent) {
        logger.info("injected")
        withContext(ioDispatcher) {
            val androidMotionEvent: MotionEvent? =
                when (motionEventProto.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        // this event starts chain of events
                        currentDownTime = SystemClock.uptimeMillis()

                        motionEventProto.toAndroidMotionEvent(
                            currentDownTime,
                            currentDownTime
                        )
                    }
                    MotionEvent.ACTION_MOVE,
                    MotionEvent.ACTION_POINTER_UP,
                    MotionEvent.ACTION_POINTER_DOWN,
                    MotionEvent.ACTION_OUTSIDE -> {
                        // these events must have a parent down event
                        if (currentDownTime != DOWN_TIME_UNSPECIFIED) {
                            motionEventProto.toAndroidMotionEvent(
                                currentDownTime,
                                SystemClock.uptimeMillis()
                            )
                        } else {
                            null
                        }
                    }
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        // these events conclude the chain of events
                        if (currentDownTime != DOWN_TIME_UNSPECIFIED) {
                            val downTime = currentDownTime
                            currentDownTime = DOWN_TIME_UNSPECIFIED

                            motionEventProto.toAndroidMotionEvent(
                                downTime,
                                SystemClock.uptimeMillis()
                            )
                        } else {
                            null
                        }
                    }
                    MotionEvent.ACTION_HOVER_ENTER,
                    MotionEvent.ACTION_HOVER_MOVE,
                    MotionEvent.ACTION_HOVER_EXIT,
                    MotionEvent.ACTION_SCROLL,
                    MotionEvent.ACTION_BUTTON_PRESS,
                    MotionEvent.ACTION_BUTTON_RELEASE -> {
                        // these events do not use downTime
                        val currentTime = SystemClock.uptimeMillis()
                        motionEventProto.toAndroidMotionEvent(currentTime, currentTime)
                    }
                    else -> {
                        null
                    }
                }

            if (androidMotionEvent != null) {
                instrumentation.sendPointerSync(androidMotionEvent)
            } else {
                logger.warn("Motion event with action ${motionEventProto.action} dropped.")
            }
        }
    }

}
