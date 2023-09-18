package org.github.overtane.soundbrowser

import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso
import org.github.overtane.soundbrowser.details.SoundDetailsUi

@BindingAdapter("imageUrl")
fun bindImage(view: ImageView, url: String) {
    if (url.isNotEmpty()) {
        Picasso.get()
            .load(url)
            .error(R.drawable.user_placeholder_error)
            .into(view)
    }
}

@BindingAdapter("soundLink")
fun bindSoundLink(view: TextView, details: SoundDetailsUi?) {
    details?.let {
        val link = "<a href=\"${details.url}\"\\>${details.name}<\\a>"
        view.apply {
            text = Html.fromHtml(link, 0)
            movementMethod = LinkMovementMethod.getInstance()
        }
    }
}
