package com.github.overtane.audiotester.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object UserPrefsSerializer : Serializer<StreamPrefs> {
    override val defaultValue: StreamPrefs
        get() = StreamPrefs.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): StreamPrefs {
        try {
            return StreamPrefs.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto:", exception)
        }
    }

    override suspend fun writeTo(t: StreamPrefs, output: OutputStream) = t.writeTo(output)
}