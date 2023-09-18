package org.github.overtane.soundbrowser.main

class SoundItemClickListener(val clickListener: (id: Int) -> Unit) {
    fun onClick(id: Int) = clickListener(id)
}