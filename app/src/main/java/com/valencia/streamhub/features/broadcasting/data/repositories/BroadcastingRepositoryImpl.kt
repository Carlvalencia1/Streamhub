package com.valencia.streamhub.features.broadcasting.data.repositories

import android.content.Context
import android.util.Log
import com.valencia.streamhub.core.session.TokenManager
import com.valencia.streamhub.features.broadcasting.data.datasources.remote.BroadcastingSignalingDataSource
import com.valencia.streamhub.features.broadcasting.data.datasources.remote.model.SignalingMessageDto
import com.valencia.streamhub.features.broadcasting.domain.entities.BroadcastingEvent
import com.valencia.streamhub.features.broadcasting.domain.repositories.BroadcastingRepository
import com.valencia.streamhub.features.streams.domain.entities.StreamResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera1Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import javax.inject.Inject

class BroadcastingRepositoryImpl @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val signalingDataSource: BroadcastingSignalingDataSource,
    private val tokenManager: TokenManager
) : BroadcastingRepository {

    private var peerConnection: PeerConnection? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var videoSource: VideoSource? = null
    private var audioSource: AudioSource? = null
    private var videoTrack: VideoTrack? = null
    private var audioTrack: AudioTrack? = null
    private var currentStreamId: String? = null

    private val _signalingEvents = MutableStateFlow<BroadcastingEvent?>(null)

    override suspend fun startBroadcasting(streamId: String): StreamResult<Unit> {
        return try {
            currentStreamId = streamId
            val token = tokenManager.getToken() ?: return StreamResult.Error("Token no disponible")

            // 1️⃣ Inicializar WebRTC factory y peer connection
            initializePeerConnectionFactory()
            createPeerConnection()

            // 2️⃣ Agregar video track (cámara)
            addVideoTrack()

            // 3️⃣ Agregar audio track (micrófono)
            addAudioTrack()

            // 4️⃣ Conectar WebSocket para señalización
            connectSignaling(streamId, token)

            // 5️⃣ Crear y enviar offer SDP
            createAndSendOffer()

            StreamResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("BroadcastingRepo", "Error al iniciar broadcasting: ${e.message}")
            runCatching { stopBroadcasting() }
            StreamResult.Error(e.message ?: "Error desconocido al iniciar broadcasting")
        }
    }

    override suspend fun stopBroadcasting(): StreamResult<Unit> {
        return try {
            videoCapturer?.stopCapture()
            peerConnection?.close()
            peerConnection = null

            videoSource?.dispose()
            audioSource?.dispose()

            signalingDataSource.disconnect()

            Log.d("BroadcastingRepo", "Broadcasting detenido")
            StreamResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("BroadcastingRepo", "Error al detener broadcasting: ${e.message}")
            StreamResult.Error(e.message ?: "Error al detener broadcasting")
        }
    }

    override fun observeSignalingEvents(): Flow<BroadcastingEvent> = flow {
        _signalingEvents.collect { event ->
            if (event != null) {
                emit(event)
            }
        }
    }

    override suspend fun sendIceCandidate(candidate: String, sdpMLineIndex: Int, sdpMid: String?): StreamResult<Unit> {
        return try {
            val message = SignalingMessageDto(
                type = "ice_candidate",
                streamId = currentStreamId,
                fromUserId = tokenManager.getUserId(),
                candidate = candidate,
                sdpMLineIndex = sdpMLineIndex,
                sdpMid = sdpMid
            )
            val sent = signalingDataSource.sendMessage(message)
            if (sent) StreamResult.Success(Unit) else StreamResult.Error("No se pudo enviar ICE candidate")
        } catch (e: Exception) {
            StreamResult.Error(e.message ?: "Error enviando ICE candidate")
        }
    }

    // --- Private helpers ---

    private fun initializePeerConnectionFactory() {
        if (peerConnectionFactory != null) return

        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        val eglBase = EglBase.create()

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(org.webrtc.DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(org.webrtc.DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()
    }

    private fun createPeerConnection() {
        if (peerConnectionFactory == null) return

        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            enableCpuOveruseDetection = true
        }

        peerConnection = peerConnectionFactory?.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
                override fun onIceConnectionReceivingChange(p0: Boolean) {}
                override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
                override fun onIceCandidate(iceCandidate: org.webrtc.IceCandidate?) {
                    iceCandidate?.let {
                        Log.d("BroadcastingRepo", "ICE candidate generado")
                        val message = SignalingMessageDto(
                            type = "ice_candidate",
                            streamId = currentStreamId,
                            fromUserId = tokenManager.getUserId(),
                            candidate = it.sdp,
                            sdpMLineIndex = it.sdpMLineIndex,
                            sdpMid = it.sdpMid
                        )
                        signalingDataSource.sendMessage(message)
                    }
                }
                override fun onIceCandidatesRemoved(p0: Array<out org.webrtc.IceCandidate>?) {}
                override fun onAddStream(p0: MediaStream?) {}
                override fun onRemoveStream(p0: MediaStream?) {}
                override fun onDataChannel(p0: org.webrtc.DataChannel?) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(p0: RtpReceiver, p1: Array<out MediaStream>) {}
            }
        )
    }

    private fun addVideoTrack() {
        if (peerConnectionFactory == null || peerConnection == null) return

        val cameraEnumerator = Camera1Enumerator(false)
        val deviceNames = cameraEnumerator.deviceNames
        var selectedDeviceName: String? = null

        for (deviceName in deviceNames) {
            if (cameraEnumerator.isFrontFacing(deviceName)) {
                selectedDeviceName = deviceName
                break
            }
        }

        if (selectedDeviceName == null && deviceNames.isNotEmpty()) {
            selectedDeviceName = deviceNames[0]
        }

        if (selectedDeviceName != null) {
            videoCapturer = cameraEnumerator.createCapturer(selectedDeviceName, null)

            videoSource = peerConnectionFactory?.createVideoSource(false)
            videoCapturer?.initialize(
                org.webrtc.SurfaceTextureHelper.create("CaptureThread", null),
                context,
                videoSource?.capturerObserver
            )
            videoCapturer?.startCapture(640, 480, 30)

            videoTrack = peerConnectionFactory?.createVideoTrack("video-track", videoSource)
            peerConnection?.addTrack(videoTrack, listOf("stream-id"))

            Log.d("BroadcastingRepo", "Video track agregado")
        }
    }

    private fun addAudioTrack() {
        if (peerConnectionFactory == null || peerConnection == null) return

        val audioConstraints = MediaConstraints()
        audioConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("echoCancellation", "true")
        )
        audioConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("noiseSuppression", "true")
        )

        audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
        audioTrack = peerConnectionFactory?.createAudioTrack("audio-track", audioSource)
        peerConnection?.addTrack(audioTrack, listOf("stream-id"))

        Log.d("BroadcastingRepo", "Audio track agregado")
    }

    private fun connectSignaling(streamId: String, token: String) {
        signalingDataSource.connect(streamId, token)
    }

    private fun createAndSendOffer() {
        if (peerConnection == null) return

        val mediaConstraints = MediaConstraints()
        mediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false")
        )
        mediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false")
        )

        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                sessionDescription?.let {
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            Log.d("BroadcastingRepo", "Offer SDP creado y establecido")
                            // Enviar offer al servidor
                            val message = SignalingMessageDto(
                                type = "offer_sdp",
                                streamId = currentStreamId,
                                fromUserId = tokenManager.getUserId(),
                                sdp = it.description
                            )
                            signalingDataSource.sendMessage(message)
                        }
                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(p0: String?) {}
                    }, it)
                }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, mediaConstraints)
    }
}

