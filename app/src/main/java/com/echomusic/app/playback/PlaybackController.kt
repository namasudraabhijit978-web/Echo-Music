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

package com.echomusic.app.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.echomusic.app.model.Song
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioEffectController: AudioEffectController
) {
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    var mediaController: MediaController? = null
        private set

    val currentPosition: Long
        get() = mediaController?.currentPosition ?: 0L

    fun initializeController(onPlayerReady: (Player) -> Unit) {
        val sessionToken = SessionToken(context, ComponentName(context, EchoMusicService::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        mediaControllerFuture?.addListener({
            val controller = mediaControllerFuture?.get()
            mediaController = controller
            
            controller?.addListener(object : Player.Listener {
                override fun onAudioSessionIdChanged(audioSessionId: Int) {
                    // Yahan se hardware audio session ID milti hai
                    audioEffectController.setupEffects(audioSessionId)
                }
            })
            
            controller?.let { onPlayerReady(it) }
        }, ContextCompat.getMainExecutor(context))
    }

    fun playPlaylist(songs: List<Song>, startIndex: Int) {
        val mediaItems = songs.map { song ->
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .setArtworkUri(song.artworkUri)
                .build()

            MediaItem.Builder()
                .setUri(song.mediaUri)
                .setMediaId(song.id.toString())
                .setMediaMetadata(metadata)
                .build()
        }

        mediaController?.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
        mediaController?.prepare()
        mediaController?.play()
    }

    fun playPause() {
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun skipToNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }

    fun releaseController() {
        audioEffectController.releaseEffects()
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
    }
}
