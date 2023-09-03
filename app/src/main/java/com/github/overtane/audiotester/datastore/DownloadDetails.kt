package com.github.overtane.audiotester.datastore

import android.net.Uri
import java.io.Serializable

data class DownloadDetails(
    val id : Long,
    val filename : String,
    val url : String,
    var status : Int
) : Serializable
{
    var bytes : Int = 0
    var localUri : Uri? = null
}