package com.vmiforall.server.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import kotlinx.coroutines.Dispatchers
import org.slf4j.LoggerFactory
import javax.inject.Inject

private val logger = LoggerFactory.getLogger(RemoteControlService::class.java)

@AndroidEntryPoint
class RemoteControlService: Service() {

    @Inject
    lateinit var remoteControlServiceImpl: RemoteControlServiceImpl

    private val server by lazy {
        NettyServerBuilder.forPort(8080)
//            .useTransportSecurity()
            .addService(remoteControlServiceImpl)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        logger.debug("RemoteControlServer started")
        super.onCreate()
        server.start()

        // keep a ref to this bro
//        val screenCapturer = ScreenCapturer()
//        val rootEglBase: EglBase = EglBase.create()
//        val surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
//        screenCapturer.initialize(surfaceTextureHelper, applicationContext, object : CapturerObserver {
//            override fun onCapturerStarted(p0: Boolean) {
//                logger.debug("onCapturerStarted")
//            }
//
//            override fun onCapturerStopped() {
//                logger.debug("onCapturerStopped")
//            }
//
//            override fun onFrameCaptured(p0: VideoFrame?) {
//                logger.debug("onFrameCaptured")
//                p0?.let {
//                    logger.debug(p0.toString())
//                }
//            }
//        })
//
//        screenCapturer.startCapture(320, 240, 60)

    }

    override fun onDestroy() {
        logger.debug("RemoteControlServer destroyed")
        super.onDestroy()
        server.shutdown()
    }

}
