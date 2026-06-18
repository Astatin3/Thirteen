/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.database.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("FunctionName")
interface PlaylistItemCrossRefDao {
    /**
     * Add an item to a playlist (creates a cross-reference).
     */
    @Transaction
    @Query(
        """
            INSERT INTO PlaylistItemCrossRef (playlist_id, audio_uri, last_modified, sort_order)
            VALUES (:playlistId, :audioUri, :lastModified, :sortOrder)
        """
    )
    suspend fun _addItemToPlaylist(
        playlistId: Long,
        audioUri: Uri,
        lastModified: Long = System.currentTimeMillis(),
        sortOrder: Int = 0,
    )

    /**
     * Remove an item from a playlist (deletes the cross-reference).
     */
    @Query("DELETE FROM PlaylistItemCrossRef WHERE playlist_id = :playlistId AND audio_uri = :audioUri")
    suspend fun _removeItemFromPlaylist(playlistId: Long, audioUri: Uri)

    /**
     * Set the sort order for an item in a playlist.
     */
    @Query("UPDATE PlaylistItemCrossRef SET sort_order = :sortOrder WHERE playlist_id = :playlistId AND audio_uri = :audioUri")
    suspend fun _setItemOrder(playlistId: Long, audioUri: Uri, sortOrder: Int)

    /**
     * Get the next sort order for a new item in a playlist.
     */
    @Query("SELECT COALESCE(MAX(sort_order), -1) + 1 FROM PlaylistItemCrossRef WHERE playlist_id = :playlistId")
    suspend fun _getNextSortOrder(playlistId: Long): Int

    /**
     * Get ordered item URIs for a playlist.
     */
    @Query("SELECT audio_uri FROM PlaylistItemCrossRef WHERE playlist_id = :playlistId ORDER BY sort_order")
    fun getOrderedItemUris(playlistId: Long): Flow<List<Uri>>

    @Query("SELECT audio_uri FROM PlaylistItemCrossRef WHERE playlist_id = :playlistId ORDER BY sort_order")
    suspend fun _getOrderedItemUrisSync(playlistId: Long): List<Uri>
}
