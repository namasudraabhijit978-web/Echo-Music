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

package com.echomusic.app.utils

import com.echomusic.app.model.LyricLine
import java.util.regex.Pattern

object LyricsParser {

    // Regex pattern to match LRC timestamps like [01:23.45]
    private val LRC_PATTERN = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")

    fun parseLrc(lrcContent: String): List<LyricLine> {
        val lines = lrcContent.split("\n")
        val lyrics = mutableListOf<LyricLine>()

        for (line in lines) {
            val matcher = LRC_PATTERN.matcher(line)
            if (matcher.find()) {
                val min = matcher.group(1)?.toLong() ?: 0
                val sec = matcher.group(2)?.toLong() ?: 0
                val milStr = matcher.group(3) ?: "0"
                
                // Handling 2-digit vs 3-digit milliseconds
                val mil = if (milStr.length == 2) milStr.toLong() * 10 else milStr.toLong()
                
                val timeMs = (min * 60 * 1000) + (sec * 1000) + mil
                val text = matcher.group(4)?.trim() ?: ""
                
                if (text.isNotEmpty()) {
                    lyrics.add(LyricLine(timeMs, text))
                }
            }
        }
        return lyrics.sortedBy { it.timeMs }
    }

    // Yeh dummy data hai testing ke liye.
    // Jab app chale, toh aapko lyrics scroll hote hue dikhenge!
    fun getDummyLyrics(): List<LyricLine> {
        val dummyLrc = """
            [00:05.00] Welcome to Echo Music Player
            [00:10.00] This is a sample synchronized lyric
            [00:15.00] Built with Jetpack Compose & Media3
            [00:20.00] Enjoy your premium audio experience
            [00:25.00] Add actual .lrc files to see real lyrics
            [00:30.00] The music keeps playing...
            [00:35.00] Developer: Chronos Studios
            [00:40.00] Have a great day!
        """.trimIndent()
        
        return parseLrc(dummyLrc)
    }
}
