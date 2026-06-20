/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap

object PlaylistThumbnailCompositor {
    private const val TARGET_SIZE = 1024

    suspend fun composite(
        context: Context,
        thumbnails: List<Thumbnail>,
    ): Bitmap? {
        if (thumbnails.isEmpty()) return null

        if (thumbnails.size < 4) {
            return loadScaledBitmap(context, thumbnails.first(), TARGET_SIZE)
        }

        val tileSize = TARGET_SIZE / 2
        val result = Bitmap.createBitmap(TARGET_SIZE, TARGET_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        thumbnails.take(4).forEachIndexed { index, thumbnail ->
            loadScaledBitmap(context, thumbnail, tileSize)?.let { bitmap ->
                val x = (index % 2) * tileSize
                val y = (index / 2) * tileSize
                canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), paint)
            }
        }

        return result
    }

    private suspend fun loadScaledBitmap(
        context: Context,
        data: Thumbnail,
        targetSize: Int,
    ): Bitmap? {
        val bitmap: Bitmap = when {
            data.bitmap != null -> data.bitmap
            data.uri != null -> {
                try {
                    val result = context.imageLoader.execute(
                        ImageRequest.Builder(context)
                            .data(data.uri)
                            .allowHardware(false)
                            .build()
                    )
                    val source = result.image?.toBitmap() ?: return null
                    if (source.width == targetSize && source.height == targetSize) {
                        source
                    } else {
                        Bitmap.createScaledBitmap(source, targetSize, targetSize, true)
                    }
                } catch (_: Exception) {
                    return null
                }
            }
            else -> return null
        }

        return if (bitmap.width == targetSize && bitmap.height == targetSize) {
            bitmap
        } else {
            Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
        }
    }
}
