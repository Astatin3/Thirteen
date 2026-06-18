/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.database.converters

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter

class UriConverter {
    @TypeConverter
    fun fromString(value: String?) = value?.toUri()

    @TypeConverter
    fun toString(uri: Uri?) = uri?.toString()
}
