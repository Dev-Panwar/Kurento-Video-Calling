package dev.panwar.kurentogroupvideocalling

import com.google.gson.Gson
import dev.panwar.kurentogroupvideocalling.models.Message
import dev.panwar.kurentogroupvideocalling.models.User
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import java.net.URISyntaxException

class SocketManager {
    private var socket: Socket? = null
    val SOCKET_URL = "http://192.168.156.145:3000"
    private val participants = mutableMapOf<String, User>()

    init {
        try {
            socket = IO.socket(SOCKET_URL)
            socket?.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }
    fun emitMessage(message: JSONObject){
        socket!!.emit("message",message)

        socket!!.on("message") { args ->
            val data = args[0] as JSONObject
            val event = data.getString("event")

            when (event) {
                "newParticipantArrived" -> {
                    val userid = data.getString("userid")
                    val username = data.getString("username")
                    receiveVideo(userid, username)
                }
                "existingParticipants" -> {
                    val userid = data.getString("userid")
                    val existingUsers = data.getJSONArray("existingUsers")
                    onExistingParticipants(userid, existingUsers)
                }
                "receiveVideoAnswer" -> {
                    val senderid = data.getString("senderid")
                    val sdpAnswer = data.getString("sdpAnswer")
                    onReceiveVideoAnswer(senderid, sdpAnswer)
                }
                "candidate" -> {
                    val userid = data.getString("userid")
                    val candidate = data.getString("candidate")
                    addIceCandidate(userid, candidate)
                }
            }
        }
    }

    private fun receiveVideo(userid: String, username: String) {
        // Create VideoRenderer and add it to your UI
        // Initialize PeerConnectionFactory
        val factory = PeerConnectionFactory.builder().createPeerConnectionFactory()

        // Create PeerConnection
        val rtcConfig = PeerConnection.RTCConfiguration(arrayListOf())
        val rtcPeer = factory.createPeerConnection(rtcConfig, object : PeerConnectionAdapter("chalja yaar") {
            override fun onIceCandidate(iceCandidate: IceCandidate) {
                super.onIceCandidate(iceCandidate)
                // Send ICE candidate to server
                val message = JSONObject()
                message.put("event", "candidate")
                message.put("userid", userid)
                message.put("roomName","a" )
                message.put("candidate", iceCandidate.sdp)
                emitMessage(message)
            }
        })

        // Create VideoTrack and add it to PeerConnection
        // Start rendering video
        // Add rtcPeer to participants map
        participants[userid] = rtcPeer?.let { User(userid, username, it) }!!
    }

    private fun onExistingParticipants(userid: String, existingUsers: JSONArray) {
        // Iterate through existingUsers JSONArray and call receiveVideo() for each user
    }

    private fun onReceiveVideoAnswer(senderid: String, sdpAnswer: String) {
        participants[senderid]?.rtcPeer?.setRemoteDescription(SdpAdapter("remoteDescription"), SessionDescription(
            SessionDescription.Type.ANSWER, sdpAnswer)
        )
    }

    private fun addIceCandidate(userid: String, candidate: String) {
        participants[userid]?.rtcPeer?.addIceCandidate(IceCandidate("", 0, candidate))
    }

    fun disconnectSocket(){
        socket?.disconnect()
        socket?.off()
    }


}