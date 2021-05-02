package com.vmiforall.server.services

import com.vmiforall.remotecontrol.RemoteControlGrpcKt
import com.vmiforall.remotecontrol.RemoteControlProto
import com.vmiforall.server.services.handler.SendMotionEventStreamHandler
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import java.net.CacheRequest
import javax.inject.Inject
import javax.inject.Singleton

private val logger = LoggerFactory.getLogger(RemoteControlServiceImpl::class.java)

@Singleton
class RemoteControlServiceImpl @Inject constructor(
    private val sendMotionEventStreamHandler: SendMotionEventStreamHandler
) : RemoteControlGrpcKt.RemoteControlCoroutineImplBase() {

    override suspend fun sendMotionEventStream(requests: Flow<RemoteControlProto.SendMotionEventRequest>): RemoteControlProto.SendMotionEventStreamResponse {
        return sendMotionEventStreamHandler.handle(requests)
    }

}
