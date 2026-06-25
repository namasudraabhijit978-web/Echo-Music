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

package com.echomusic.app.ui.components

import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VinylSpinner(
    artworkUri: Uri,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 360f else 0f, // Spin only when playing
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing)
        ),
        label = "vinyl_rotation"
    )

    Box(
        modifier = modifier
            .rotate(if (isPlaying) rotation else 0f)
            .clip(CircleShape)
            .background(Color.Black) // Outer Vinyl Color
            .border(4.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Grooves of the vinyl
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .border(1.dp, Color.DarkGray, CircleShape)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .border(1.dp, Color.DarkGray, CircleShape)
        )
        
        // Center Album Art
        AlbumArtImage(
            uri = artworkUri,
            modifier = Modifier
                .fillMaxSize(0.45f) // Inner circle for album cover
                .clip(CircleShape)
        )
        
        // Spindle Hole
        Box(
            modifier = Modifier
                .fillMaxSize(0.05f)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
