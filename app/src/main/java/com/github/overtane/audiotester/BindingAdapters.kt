package com.github.overtane.audiotester

import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.github.overtane.audiotester.audiostream.AudioDirection
import com.github.overtane.audiotester.audiostream.AudioSource
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.player.PlaybackStat

@BindingAdapter("progressPercentage")
fun bindProgress(progressBar: ProgressBar, playbackStat: PlaybackStat?) {
    if (playbackStat != null) {
        val progress = (playbackStat.framesStreamed.toDouble() / playbackStat.totalFrames * 100).toInt()
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

@BindingAdapter("soundLink")
fun bindSoundLink(view: TextView, details: AudioStream?) {
    val source = details?.source as AudioSource.Sound
    source?.let {
        val link = "<a href=\"${it.url}\"\\>${it.name}<\\a>"
        view.apply {
            text = Html.fromHtml(link, 0)
            movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
