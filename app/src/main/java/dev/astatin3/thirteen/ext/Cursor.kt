/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.ext

import android.database.Cursor
import dev.astatin3.thirteen.models.ColumnIndexCache

fun <T> Cursor?.mapEachRow(
    mapping: (ColumnIndexCache) -> T,
) = this?.use { cursor ->
    if (!cursor.moveToFirst()) {
        return@use emptyList<T>()
    }

    val columnIndexCache = ColumnIndexCache(cursor)

    val data = buildList {
        do {
            add(mapping(columnIndexCache))
        } while (cursor.moveToNext())
    }

    data.toList()
} ?: emptyList()
