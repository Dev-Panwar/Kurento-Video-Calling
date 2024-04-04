package dev.panwar.kurentogroupvideocalling

import android.content.Context
import android.util.AttributeSet
import org.webrtc.VideoTrack

import org.webrtc.SurfaceViewRenderer

// Custom view for displaying a video track
class RtcVideoView : SurfaceViewRenderer {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    // Method to set the video track to display in this view
    fun setVideoTrack(videoTrack: VideoTrack?) {
        // Clear any previous rendering
        clearImage()
        // Set the video track to render in this view
        videoTrack?.addSink(this)
    }
}