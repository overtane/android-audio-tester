package org.github.overtane.soundbrowser.main

import androidx.recyclerview.widget.RecyclerView
import org.github.overtane.soundbrowser.databinding.FragmentSoundItemBinding

class SoundItemViewHolder(private val binding: FragmentSoundItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(position: Int, data: SoundListItem, clickListener: SoundItemClickListener) {
        val actualPosition = position + 1
        binding.apply {
            this.clickListener = clickListener
            this.sound = data
            itemNumber.text = actualPosition.toString()
            executePendingBindings()
        }
    }
}