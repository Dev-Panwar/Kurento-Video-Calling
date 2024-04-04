package dev.panwar.kurentogroupvideocalling

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import org.webrtc.*

class MainActivity : AppCompatActivity() {

    private lateinit var socket: Socket
    private lateinit var roomNameEditText: EditText
    private lateinit var userNameEditText: EditText
    private lateinit var registerButton: View
    private lateinit var videoContainer: LinearLayout
    private var roomName: String? = null
    private var userName: String? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTracks: MutableMap<String, VideoTrack> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        roomNameEditText = findViewById(R.id.roomNameEditText)
        userNameEditText = findViewById(R.id.userNameEditText)
        registerButton = findViewById(R.id.registerButton)
        videoContainer = findViewById(R.id.videoContainer)

        socket = IO.socket("http://192.168.113.145:3000")
        socket.connect()

        registerButton.setOnClickListener {
            roomName = roomNameEditText.text.toString()
            userName = userNameEditText.text.toString()

            if (roomName.isNullOrEmpty() || userName.isNullOrEmpty()) {
                return@setOnClickListener
            }

            val message = JSONObject().apply {
                put("event", "joinRoom")
                put("userName", userName)
                put("roomName", roomName)
            }
            socket.emit("message", message)

            // Hide room selection UI, show meeting room UI
            findViewById<View>(R.id.roomSelection).visibility = View.GONE
            findViewById<View>(R.id.meetingRoom).visibility = View.VISIBLE
        }

        socket.on("message", onMessage)
    }

    private val onMessage = Emitter.Listener { args ->
        runOnUiThread {
            val message = args[0] as JSONObject
            when (message.getString("event")) {
                "newParticipantArrived" -> {
                    val userid = message.getString("userid")
                    val username = message.getString("username")
                    receiveVideo(userid, username)
                }
                "existingParticipants" -> {
                    val existingUsers = message.getJSONArray("existingUsers")
                    for (i in 0 until existingUsers.length()) {
                        val user = existingUsers.getJSONObject(i)
                        val userid = user.getString("id")
                        val username = user.getString("name")
                        receiveVideo(userid, username)
                    }
                }
                "receiveVideoAnswer" -> {
                    val senderid = message.getString("senderid")
                    val sdpAnswer = message.getString("sdpAnswer")
                    onReceiveVideoAnswer(senderid, sdpAnswer)
                }
                "candidate" -> {
                    val userid = message.getString("userid")
                    val candidate = message.getJSONObject("candidate")
                    addIceCandidate(userid, candidate)
                }
            }
        }
    }

    private fun receiveVideo(userid: String, username: String) {
        val rtcPeer = createPeerConnection()
        val videoView = RtcVideoView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            setVideoTrack(remoteVideoTracks[userid])

        }
        val videoContainer = findViewById<LinearLayout>(R.id.videoContainer)
        videoContainer.addView(videoView)

        val constraints = MediaConstraints()
        rtcPeer.createOffer(object : SdpObserver by SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
//                super.onCreateSuccess(sdp)
                rtcPeer.setLocalDescription(this, sdp)
                val message = JSONObject().apply {
                    put("event", "receiveVideoFrom")
                    put("userid", userid)
                    put("roomName", roomName)
                    put("sdpOffer", sdp.description)
                }
                socket.emit("message", message)
            }
        }, constraints)
    }

    private fun createPeerConnection(): PeerConnection {
        val iceServers = mutableListOf<PeerConnection.IceServer>()
        val peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        val peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(iceCandidate: IceCandidate) {
                val message = JSONObject().apply {
                    put("event", "candidate")
                    put("userid", "")
                    put("roomName", roomName)
                    put("candidate", JSONObject().apply {
                        put("candidate", iceCandidate.sdp)
                        put("sdpMid", iceCandidate.sdpMid)
                        put("sdpMLineIndex", iceCandidate.sdpMLineIndex)
                    })
                }
                socket.emit("message", message)
            }

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onAddStream(mediaStream: MediaStream) {
                val videoTracks = mediaStream.videoTracks
                if (videoTracks.size > 0) {
                    val remoteVideoTrack = videoTracks[0]
//                    val userid = remoteVideoTrack.id
//                    remoteVideoTracks[userid] = remoteVideoTrack
                }
            }

            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
        })
        return peerConnection!!
    }

    private fun onReceiveVideoAnswer(senderid: String, sdpAnswer: String) {
        val rtcPeer = createPeerConnection()
        val sdp = SessionDescription(SessionDescription.Type.ANSWER, sdpAnswer)
        rtcPeer.setRemoteDescription(SimpleSdpObserver(), sdp)
    }

    private fun addIceCandidate(userid: String, candidate: JSONObject) {
        val rtcPeer = createPeerConnection()
        val sdpMid = candidate.getString("sdpMid")
        val sdpMLineIndex = candidate.getInt("sdpMLineIndex")
        val sdp = candidate.getString("candidate")
        rtcPeer.addIceCandidate(IceCandidate(sdpMid, sdpMLineIndex, sdp))
    }
}
