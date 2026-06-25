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

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.echomusic.app.data.preferences.SettingsManager
import com.echomusic.app.data.repository.FavoriteRepository
import com.echomusic.app.data.repository.SongRepository
import com.echomusic.app.model.Album
import com.echomusic.app.model.Song
import com.echomusic.app.playback.AudioEffectController
import com.echomusic.app.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val favoriteRepository: FavoriteRepository,
    private val playbackController: PlaybackController,
    private val audioEffectController: AudioEffectController,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSongs: StateFlow<List<Song>> = combine(_songs, _searchQuery) { songList, query ->
        if (query.isBlank()) songList
        else songList.filter { it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val albums: StateFlow<List<Album>> = _songs.map { songList ->
        songList.groupBy { it.albumId }.map { (albumId, songs) ->
            val firstSong = songs.first()
            Album(
                id = albumId,
                title = firstSong.album,
                artist = firstSong.artist,
                artworkUri = firstSong.artworkUri,
                songs = songs.sortedBy { it.title }
            )
        }.sortedBy { it.title }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _favoriteSongs = MutableStateFlow<List<Song>>(emptyList())
    val favoriteSongs: StateFlow<List<Song>> = _favoriteSongs.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _isCurrentSongFavorite = MutableStateFlow(false)
    val isCurrentSongFavorite: StateFlow<Boolean> = _isCurrentSongFavorite.asStateFlow()

    // Shuffle & Repeat States
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    // Audio Effect States
    private val _bassBoostLevel = MutableStateFlow(0f)
    val bassBoostLevel: StateFlow<Float> = _bassBoostLevel.asStateFlow()

    private val _virtualizerLevel = MutableStateFlow(0f)
    val virtualizerLevel: StateFlow<Float> = _virtualizerLevel.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow(0)
    val sleepTimerMinutes: StateFlow<Int> = _sleepTimerMinutes.asStateFlow()

    // Theme Settings State
    private val _themeMode = MutableStateFlow(settingsManager.getThemeMode())
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    private var positionJob: Job? = null
    private var favoriteJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        playbackController.initializeController { player ->
            setupPlayerListener(player)
        }
        loadFavorites()
    }

    private fun setupPlayerListener(player: Player) {
        viewModelScope.launch {
            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) startPositionUpdates() else positionJob?.cancel()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    mediaItem?.mediaId?.toLongOrNull()?.let { id ->
                        val song = _songs.value.find { it.id == id } ?: _favoriteSongs.value.find { it.id == id }
                        if (song != null) {
                            _currentSong.value = song
                            checkIfFavorite(id)
                        }
                    }
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _isShuffleEnabled.value = shuffleModeEnabled
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    _repeatMode.value = repeatMode
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
                .catch { _songs.value = emptyList() }
                .collect { songList -> _songs.value = songList }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            favoriteRepository.getAllFavorites().collect { entities ->
                val songs = entities.map { entity ->
                    Song(
                        id = entity.id,
                        title = entity.title,
                        artist = entity.artist,
                        album = entity.album,
                        albumId = entity.albumId,
                        duration = entity.duration,
                        mediaUri = Uri.parse(entity.mediaUriString),
                        artworkUri = Uri.parse(entity.artworkUriString)
                    )
                }
                _favoriteSongs.value = songs
            }
        }
    }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }

    fun getAlbumById(albumId: Long): Album? = albums.value.find { it.id == albumId }

    fun playSong(song: Song, playlist: List<Song> = filteredSongs.value) {
        _currentSong.value = song
        val index = playlist.indexOfFirst { it.id == song.id }
        if (index != -1) playbackController.playPlaylist(playlist, index)
        else playbackController.playPlaylist(listOf(song), 0)
        checkIfFavorite(song.id)
    }

    private fun checkIfFavorite(songId: Long) {
        favoriteJob?.cancel()
        favoriteJob = viewModelScope.launch {
            favoriteRepository.isFavorite(songId).collect { isFav -> _isCurrentSongFavorite.value = isFav }
        }
    }

    fun toggleFavorite() {
        val song = _currentSong.value ?: return
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(song, _isCurrentSongFavorite.value)
        }
    }

    fun togglePlayPause() = playbackController.playPause()
    fun seekTo(position: Long) { playbackController.seekTo(position); _currentPosition.value = position }
    fun skipToNext() = playbackController.skipToNext()
    fun skipToPrevious() = playbackController.skipToPrevious()

    fun toggleShuffle() {
        playbackController.setShuffleModeEnabled(!_isShuffleEnabled.value)
    }

    fun toggleRepeat() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        playbackController.setRepeatMode(nextMode)
    }

    fun setBassBoost(level: Float) {
        _bassBoostLevel.value = level
        audioEffectController.setBassBoostStrength((level * 1000).toInt().toShort())
    }

    fun setVirtualizer(level: Float) {
        _virtualizerLevel.value = level
        audioEffectController.setVirtualizerStrength((level * 1000).toInt().toShort())
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerMinutes.value = minutes
        if (minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                delay(minutes * 60 * 1000L)
                playbackController.pause()
                _sleepTimerMinutes.value = 0
            }
        }
    }

    fun setThemeMode(mode: Int) {
        settingsManager.setThemeMode(mode)
        _themeMode.value = mode
    }

    override fun onCleared() {
        super.onCleared()
        positionJob?.cancel()
        favoriteJob?.cancel()
        sleepTimerJob?.cancel()
        playbackController.releaseController()
    }
}
