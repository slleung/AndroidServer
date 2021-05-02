package com.vmiforall.server.models

import android.view.MotionEvent
import com.vmiforall.remotecontrol.RemoteControlProto

/**
 * Helper method for converting the protobuf serialized motion event to the android view motion
 * event.
 *
 * Note that the newly created motion event has all fields from the original motion event sent from
 * the client except the downTime and eventTime. Those should be set by the server before the motion
 * event is injected.
 */
fun RemoteControlProto.MotionEvent.toAndroidMotionEvent(
    downTime: Long,
    eventTime: Long
): MotionEvent {

    val pointerPropertiesArray =
        Array(pointerPropertiesCount) { index ->
            MotionEvent.PointerProperties().apply {
                id = pointerPropertiesList[index].id
                toolType = pointerPropertiesList[index].toolType
            }
        }

    val pointerCoordsArray =
        Array(pointerCoordsCount) { index ->
            MotionEvent.PointerCoords().apply {
                x = pointerCoordsList[index].x
                y = pointerCoordsList[index].y
                pressure = pointerCoordsList[index].pressure
                size = pointerCoordsList[index].size
                touchMajor = pointerCoordsList[index].touchMajor
                touchMinor = pointerCoordsList[index].touchMinor
                toolMajor = pointerCoordsList[index].toolMajor
                toolMinor = pointerCoordsList[index].toolMinor
                orientation = pointerCoordsList[index].orientation
            }
        }

    return MotionEvent.obtain(
        downTime,
        eventTime,
        action,
        pointerCount,
        pointerPropertiesArray,
        pointerCoordsArray,
        metaState,
        buttonState,
        xPrecision,
        yPrecision,
        deviceId,
        edgeFlags,
        source,
        flags
    )
}
