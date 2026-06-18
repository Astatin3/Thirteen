/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.database.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

@Dao
@Suppress("FunctionName")
interface FavoriteDao {
    /**
     * Get all the favorite items.
     */
    @Query(
        """
            SELECT audio_uri
            FROM favorite
            ORDER BY sort_order
        """
    )
    fun getAll(): Flow<List<Uri>>

    /**
     * Set the sort order for a favorite item.
     */
    @Query("UPDATE favorite SET sort_order = :sortOrder WHERE audio_uri = :audioUri")
    suspend fun _setItemOrder(audioUri: Uri, sortOrder: Int)

    /**
     * Get the next sort order for a new favorite item.
     */
    @Query("SELECT COALESCE(MAX(sort_order), -1) + 1 FROM favorite")
    suspend fun _getNextSortOrder(): Int

    /**
     * Check whether this item is a favorite.
     */
    @Query(
        """
            SELECT audio_uri
            FROM favorite
            WHERE audio_uri = :audioUri
        """
    )
    suspend fun _contains(audioUri: Uri): Uri?

    /**
     * Check whether this item is a favorite.
     */
    suspend fun contains(audioUri: Uri): Boolean = _contains(audioUri) != null

    /**
     * Check whether this item is a favorite.
     */
    @Query(
        """
            SELECT audio_uri
            FROM favorite
            WHERE audio_uri = :audioUri
        """
    )
    fun _containsFlow(audioUri: Uri): Flow<Uri?>

    /**
     * Check whether this item is a favorite.
     */
    fun containsFlow(audioUri: Uri): Flow<Boolean> = _containsFlow(audioUri).map { it != null }

    /**
     * Add this item to favorites.
     */
    @Query(
        """
            INSERT INTO favorite (audio_uri, added_at, sort_order)
            VALUES (:audioUri, :addedAt, :sortOrder)
        """
    )
    suspend fun _add(audioUri: Uri, addedAt: Instant = Instant.now(), sortOrder: Int)

    /**
     * Add this item to favorites.
     */
    suspend fun add(audioUri: Uri, addedAt: Instant = Instant.now()) =
        _add(audioUri, addedAt, _getNextSortOrder())

    /**
     * Remove this item from favorites.
     */
    @Query(
        """
            DELETE
            FROM favorite
            WHERE audio_uri = :audioUri
        """
    )
    suspend fun remove(audioUri: Uri)
}
