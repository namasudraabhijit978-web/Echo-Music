/*
 * Copyright 2026 Chronos Developers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.echomusic.app.data.repository

import com.echomusic.app.data.local.SongDao
import com.echomusic.app.data.local.SongEntity
import com.echomusic.app.model.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val songDao: SongDao
) {
    fun isFavorite(songId: Long): Flow<Boolean> {
        return songDao.isFavorite(songId)
    }

    suspend fun toggleFavorite(song: Song, isCurrentlyFavorite: Boolean) {
        val entity = SongEntity(
            id = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            albumId = song.albumId,
            duration = song.duration,
            mediaUriString = song.mediaUri.toString(),
            artworkUriString = song.artworkUri.toString()
        )

        if (isCurrentlyFavorite) {
            songDao.deleteFavorite(entity)
        } else {
            songDao.insertFavorite(entity)
        }
    }
    
    fun getAllFavorites(): Flow<List<SongEntity>> {
        return songDao.getFavorites()
    }
}
