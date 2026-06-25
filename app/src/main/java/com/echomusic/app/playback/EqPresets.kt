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

data class EqPreset(val name: String, val bassLevel: Float, val virtualizerLevel: Float)

object RetroPresets {
    val presets = listOf(
        EqPreset("Flat", 0.0f, 0.0f),
        EqPreset("Acoustic", 0.2f, 0.1f),
        EqPreset("Bass Booster", 0.8f, 0.2f),
        EqPreset("Live Concert", 0.5f, 0.9f),
        EqPreset("Retro Synthwave", 0.7f, 0.6f),
        EqPreset("Studio Monitor", 0.3f, 0.1f)
    )
}
