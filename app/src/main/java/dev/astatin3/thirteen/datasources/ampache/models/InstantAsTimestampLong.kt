/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.datasources.ampache.models

import kotlinx.serialization.Serializable
import dev.astatin3.thirteen.datasources.ampache.serializers.TimestampInstantSerializer
import java.time.Instant

typealias InstantAsTimestampLong = @Serializable(with = TimestampInstantSerializer::class) Instant
