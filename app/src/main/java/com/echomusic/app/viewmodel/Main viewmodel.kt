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
import androidx.media3.common.Player
import com.echomusic.app.data.repository.SongRepository
import com.echomusic.app.model.Song
import com.echomusic.app.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private var positionJob: Job? = null

    init {
        playbackController.initializeController()
        setupPlayerListener()
    }

    private fun setupPlayerListener() {
        viewModelScope.launch {
            delay(500)
            playbackController.mediaController?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) {
                        startPositionUpdates()
                    } else {
                        positionJob?.cancel()
                    }
                }
            })
        }
    }

    private fun startPositionUpdates() {
        positionJob?.cancel()
        positionJob = viewModelScope.launch {
            while (true) {
                _currentPosition.value = playbackController.currentPosition
                delay(1000)
            }
        }
    }

    fun loadSongs() {
        viewModelScope.launch {
            songRepository.getAllSongs()
                .catch { 
                    _songs.value = emptyList()
                }
                .collect { songList ->
                    _songs.value = songList
                }
        }
    }

    fun playSong(song: Song) {
        _currentSong.value = song
        playbackController.playSong(song)
    }

    fun togglePlayPause() {
        playbackController.playPause()
    }

    fun seekTo(position: Long) {
        playbackController.seekTo(position)
        _currentPosition.value = position
    }

    fun skipToNext() {
        playbackController.skipToNext()
    }

    fun skipToPrevious() {
        playbackController.skipToPrevious()
    }

    override fun onCleared() {
        super.onCleared()
        positionJob?.cancel()
        playbackController.releaseController()
    }
}
