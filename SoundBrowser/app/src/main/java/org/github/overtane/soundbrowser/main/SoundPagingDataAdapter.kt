package org.github.overtane.soundbrowser.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import org.github.overtane.soundbrowser.databinding.FragmentSoundItemBinding

class SoundPagingDataAdapter(private val clickListener: SoundItemClickListener) :
    PagingDataAdapter<SoundListItem, SoundItemViewHolder>(DIFFCALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SoundItemViewHolder(
        FragmentSoundItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: SoundItemViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(position, item, clickListener)
        }
    }

    companion object {
        private val DIFFCALLBACK = object : DiffUtil.ItemCallback<SoundListItem>() {
            override fun areItemsTheSame(oldItem: SoundListItem, newItem: SoundListItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: SoundListItem,
                newItem: SoundListItem
            ): Boolean =
                oldItem == newItem
        }
    }

}