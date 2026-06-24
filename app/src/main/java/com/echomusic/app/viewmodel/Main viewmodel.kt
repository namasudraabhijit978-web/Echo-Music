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

package com.echomusic.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echomusic.app.data.repository.SongRepository
import com.echomusic.app.model.Song
import com.echomusic.app.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playbackController: PlaybackController
) : ViewModel() {

    // UI State for songs list
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    init {
        // Initialize MediaController so it's ready before user clicks play
        playbackController.initializeController()
    }

    fun loadSongs() {
        viewModelScope.launch {
            songRepository.getAllSongs()
                .catch { 
                    // Yahan errors handle honge (e.g., permissions missing)
                    _songs.value = emptyList()
                }
                .collect { songList ->
                    _songs.value = songList
                }
        }
    }

    fun playSong(song: Song) {
        playbackController.playSong(song)
    }

    fun togglePlayPause() {
        playbackController.playPause()
    }

    override fun onCleared() {
        super.onCleared()
        // Prevent memory leaks when ViewModel is destroyed
        playbackController.releaseController()
    }
}
