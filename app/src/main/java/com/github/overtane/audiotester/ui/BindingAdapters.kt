package com.github.overtane.audiotester.ui

import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import com.github.overtane.audiotester.player.StreamInfo

@BindingAdapter("progressPercentage")
fun bindProgress(progressBar: ProgressBar, streamInfo: StreamInfo?) {
    streamInfo?.let {
        val progress = (it.framesStreamed.toDouble() / it.totalFrames * 100).toInt()
        progressBar.progress = progress
    }
}