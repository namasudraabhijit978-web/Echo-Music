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

package com.echomusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.echomusic.app.ui.navigation.AppNavigation
import com.echomusic.app.ui.theme.EchoMusicTheme
import com.echomusic.app.utils.ColorExtractor
import com.echomusic.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val currentSong by mainViewModel.currentSong.collectAsState()
            
            var dominantColor by remember { mutableStateOf<Color?>(null) }
            val context = LocalContext.current

            // Album art change hote hi color extract karna
            LaunchedEffect(currentSong) {
                currentSong?.let { song ->
                    dominantColor = ColorExtractor.getDominantColor(context, song.artworkUri)
                } ?: run {
                    dominantColor = null
                }
            }

            EchoMusicTheme(customDominantColor = dominantColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = mainViewModel)
                }
            }
        }
    }
}
