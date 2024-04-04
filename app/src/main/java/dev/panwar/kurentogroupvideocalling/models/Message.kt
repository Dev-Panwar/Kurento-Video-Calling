package dev.panwar.kurentogroupvideocalling.models

data class Message(
    val event:String,
    val userName:String? = null,
    val roomName: String? = null,
    val userId: Any? = null,
    val sdpOffer: Any? = null,
    val candidate: Any? = null
)
