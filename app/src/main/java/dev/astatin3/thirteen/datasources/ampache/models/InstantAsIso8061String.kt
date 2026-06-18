/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.datasources.ampache.models

import kotlinx.serialization.Serializable
import dev.astatin3.thirteen.datasources.ampache.serializers.Iso8601InstantSerializer
import java.time.Instant

typealias InstantAsIso8061String = @Serializable(with = Iso8601InstantSerializer::class) Instant
