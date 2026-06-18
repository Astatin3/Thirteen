/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.ext

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel

val AndroidViewModel.applicationContext: Context
    get() = getApplication<Application>().applicationContext

val AndroidViewModel.resources: Resources
    get() = getApplication<Application>().resources
