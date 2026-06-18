/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen

import android.app.Application
import androidx.media3.common.util.UnstableApi
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.MainScope
import dev.astatin3.thirteen.database.ThirteenDatabase
import dev.astatin3.thirteen.repositories.MediaRepository
import dev.astatin3.thirteen.repositories.OutputConfigurationRepository
import dev.astatin3.thirteen.repositories.ProvidersRepository
import dev.astatin3.thirteen.repositories.ResumptionPlaylistRepository
import dev.astatin3.thirteen.ui.coil.ThumbnailMapper

@androidx.annotation.OptIn(UnstableApi::class)
class ThirteenApplication : Application(), SingletonImageLoader.Factory {
    private val coroutineScope = MainScope()
    private val database by lazy { ThirteenDatabase.get(applicationContext) }
    val providersRepository by lazy {
        ProvidersRepository(applicationContext, coroutineScope, database)
    }
    val mediaRepository by lazy {
        MediaRepository(applicationContext, coroutineScope, providersRepository, database)
    }
    val resumptionPlaylistRepository by lazy { ResumptionPlaylistRepository(database) }
    val outputConfigurationRepository by lazy { OutputConfigurationRepository() }

    override fun onCreate() {
        super.onCreate()

        // Observe dynamic colors changes
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun newImageLoader(context: PlatformContext) = ImageLoader.Builder(this)
        .components {
            add(ThumbnailMapper)
        }
        .build()
}
