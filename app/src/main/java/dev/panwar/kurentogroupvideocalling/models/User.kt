package dev.panwar.kurentogroupvideocalling.models

import org.webrtc.PeerConnection

class User(
    val id: String,
    val username: String,
    val rtcPeer: PeerConnection
)
