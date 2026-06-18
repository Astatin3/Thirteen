/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.ui.coil

import coil3.map.Mapper
import coil3.request.Options
import dev.astatin3.thirteen.models.Thumbnail

object ThumbnailMapper : Mapper<Thumbnail, Any> {
    override fun map(data: Thumbnail, options: Options) = data.bitmap ?: data.uri
}
