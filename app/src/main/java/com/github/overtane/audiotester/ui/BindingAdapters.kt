package com.github.overtane.audiotester.ui

import android.view.View
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import com.github.overtane.audiotester.audiotrack.AudioDirection
import com.github.overtane.audiotester.audiotrack.AudioStream
import com.github.overtane.audiotester.player.StreamInfo

@BindingAdapter("progressPercentage")
fun bindProgress(progressBar: ProgressBar, streamInfo: StreamInfo?) {
    streamInfo?.let {
        val progress = (it.framesStreamed.toDouble() / it.totalFrames * 100).toInt()
        progressBar.progress = progress
    }
}

@BindingAdapter("microphoneState")
fun bindProgress(view: View, audioStream: AudioStream?) {
    audioStream?.let {
        view.isSelected = when (it.direction) {
            AudioDirection.PLAYBACK -> false
            AudioDirection.RECORD,
            AudioDirection.FULL_DUPLEX -> true
        }
    }
}