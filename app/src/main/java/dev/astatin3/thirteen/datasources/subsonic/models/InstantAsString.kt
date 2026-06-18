/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.datasources.subsonic.models

import kotlinx.serialization.Serializable
import java.time.Instant

typealias InstantAsString = @Serializable(with = InstantSerializer::class) Instant
