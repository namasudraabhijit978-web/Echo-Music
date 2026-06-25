/*

Copyright 2026 Chronos Developers

Licensed under the Apache License, Version 2.0 (the "License");

you may not use this file except in compliance with the License.

You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software

distributed under the License is distributed on an "AS IS" BASIS,

WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

See the License for the specific language governing permissions and

limitations under the License.
*/


package com.echomusic.app.playback

import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.echomusic.app.R
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class EchoMusicService : MediaSessionService(), MediaSession.Callback {

private var mediaSession: MediaSession? = null  
private var player: ExoPlayer? = null  

// Custom Command for the Notification Favorite Button  
private val favoriteCommand = SessionCommand("CUSTOM_ACTION_FAVORITE", Bundle.EMPTY)  

override fun onCreate() {  
    super.onCreate()  

    val audioAttributes = AudioAttributes.Builder()  
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)  
        .setUsage(C.USAGE_MEDIA)  
        .build()  

    player = ExoPlayer.Builder(this)  
        .setAudioAttributes(audioAttributes, true)  
        .setHandleAudioBecomingNoisy(true) // Pauses when earphones are disconnected  
        .build()  

    // Create Custom Favorite Button for Notification  
    val favoriteButton = CommandButton.Builder()  
        .setDisplayName("Favorite")  
        .setSessionCommand(favoriteCommand)  
        .setIconResId(R.drawable.ic_notification_favorite)  
        .build()  

    mediaSession = MediaSession.Builder(this, player!!)  
        .setCallback(this)  
        .setCustomLayout(listOf(favoriteButton)) // Adding button to layout  
        .build()  
}  

override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {  
    return mediaSession  
}  

// Handle Custom Actions clicked from Notification  
override fun onCustomCommand(  
    session: MediaSession,  
    controller: MediaSession.ControllerInfo,  
    customCommand: SessionCommand,  
    args: Bundle  
): ListenableFuture<SessionResult> {  
    if (customCommand.customAction == favoriteCommand.customAction) {  
        // Yahan par aap Intent broadcast kar sakte hain ya ViewModel ko signal bhej sakte hain  
        // ki user ne notification se Like button dabaya hai.  
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))  
    }  
    return super.onCustomCommand(session, controller, customCommand, args)  
}  

override fun onTaskRemoved(rootIntent: Intent?) {  
    val currentPlayer = player ?: return  
    if (!currentPlayer.playWhenReady || currentPlayer.mediaItemCount == 0) {  
        stopSelf()  
    }  
}  

override fun onDestroy() {  
    mediaSession?.run {  
        player.release()  
        release()  
        mediaSession = null  
    }  
    super.onDestroy()  
}

}