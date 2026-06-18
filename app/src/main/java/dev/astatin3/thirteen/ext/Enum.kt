/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.ext

/**
 * Get the previous value.
 */
internal inline fun <reified E : Enum<E>> E.previous() = enumValues<E>().previous(
    this
) ?: throw Exception("No enum values")

/**
 * Get the next value.
 */
internal inline fun <reified E : Enum<E>> E.next() = enumValues<E>().next(
    this
) ?: throw Exception("No enum values")
