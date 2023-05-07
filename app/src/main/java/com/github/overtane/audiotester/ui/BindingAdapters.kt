package com.github.overtane.audiotester.ui

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.github.overtane.audiotester.audiostream.AudioDirection
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.player.StreamStat

@BindingAdapter("progressPercentage")
fun bindProgress(progressBar: ProgressBar, streamStat: StreamStat?) {
    if (streamStat != null) {
        val progress = (streamStat.framesStreamed.toDouble() / streamStat.totalFrames * 100).toInt()
        progressBar.progress = progress
    } else {
        progressBar.progress = 0
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

@BindingAdapter("audioDuration")
fun bindDuration(view: TextView, duration: Int) {
    view.text =
        if (duration >= 1) String.format("Duration %d s", duration) else "Duration"
}

@BindingAdapter("sineWaveFrequency")
fun bindFrequency(view: TextView, frequency : Int) {
    // TODO view active vs inActive
    view.text = when {
        frequency > 0 -> String.format("Frequency %d Hz", frequency)
        else -> "Frequency"
    }
}

@BindingAdapter("progressCheck")
fun bindProgress(progressBar: ProgressBar, newProgress : Int) {
    if (newProgress != progressBar.progress) {
        progressBar.progress = when {
            newProgress > progressBar.max -> progressBar.max
            newProgress < progressBar.min -> progressBar.min
            else -> newProgress
        }
    }
}
