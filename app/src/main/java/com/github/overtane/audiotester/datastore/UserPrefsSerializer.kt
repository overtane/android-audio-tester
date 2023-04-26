package com.github.overtane.audiotester.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object UserPrefsSerializer : Serializer<UserPrefs> {
    override val defaultValue: UserPrefs
        get() = UserPrefs.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserPrefs {
        try {
            return UserPrefs.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto:", exception)
        }
    }

    override suspend fun writeTo(t: UserPrefs, output: OutputStream) = t.writeTo(output)
}