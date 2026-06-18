/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.datasources.subsonic.models

import android.net.Uri
import kotlinx.serialization.Serializable

typealias UriAsString = @Serializable(with = UriSerializer::class) Uri
