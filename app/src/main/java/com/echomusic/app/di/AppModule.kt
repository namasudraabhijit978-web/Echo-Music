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

package com.echomusic.app.di

import android.content.Context
import androidx.room.Room
import com.echomusic.app.data.local.EchoDatabase
import com.echomusic.app.data.local.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideEchoDatabase(@ApplicationContext context: Context): EchoDatabase {
        return Room.databaseBuilder(
            context,
            EchoDatabase::class.java,
            "echo_music.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSongDao(database: EchoDatabase): SongDao {
        return database.songDao()
    }
}
