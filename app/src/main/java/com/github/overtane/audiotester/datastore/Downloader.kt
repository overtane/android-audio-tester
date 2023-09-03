package com.github.overtane.audiotester.datastore

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log

private const val TAG: String = "Downloader"

class Downloader(context: Context) {

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE)
            as DownloadManager

    private var downloadData: DownloadDetails? = null // latest download

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)?.let {
                if (it == downloadData?.id) {
                    query(it)
                    Log.d(
                        TAG,
                        "$downloadData, ${downloadData?.bytes}B, ${downloadData?.localUri}"
                    )
                }
            }
        }
    }

    init {
        context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    fun download(url: String, filename: String): Long {
        if (url != downloadData?.url) {
            cancel() // cancel previous in case still ongoing
            val request =
                DownloadManager.Request(Uri.parse(url))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
            val id = downloadManager.enqueue(request)
            downloadData = DownloadDetails(id, filename, url, DownloadManager.STATUS_PENDING)
            Log.d(TAG, "Start download $downloadData")
            return id
        }
        return 0
    }

    private fun cancel() {
        downloadData?.let {
            downloadManager.remove(it.id)
        }
    }

    private fun query(id: Long) {
        val query = DownloadManager.Query().setFilterById(id)
        val cursor = downloadManager.query(query)
        val statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val sizeColumn = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

        if (cursor.moveToFirst()) {
            val status =
                if (statusColumn > 0) cursor.getInt(statusColumn) else DownloadManager.STATUS_FAILED
            downloadData?.status = status
            val bytes = if (sizeColumn > 0) cursor.getInt(sizeColumn) else 0
            downloadData?.bytes = bytes
            downloadData?.localUri = downloadManager.getUriForDownloadedFile(id)
        }
        cursor.close()
    }

    companion object {
        private var instance: Downloader? = null

        @JvmStatic
        fun getInstance(context: Context): Downloader {
            if (instance == null) {
                instance = Downloader(context)
            }
            return instance!!
        }
    }
}