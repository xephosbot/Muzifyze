package com.xbot.musifyze.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.xbot.musifyze.data.player.AudioPlayer
import com.xbot.musifyze.data.player.ExoAudioPlayer
import com.xbot.musifyze.data.repositories.AudioRepository
import com.xbot.musifyze.data.repositories.AudioRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }

    @Provides
    fun provideAudioPlayer(player: ExoPlayer): AudioPlayer {
        return ExoAudioPlayer(player)
    }

    @Provides
    fun provideAudioRepository(@ApplicationContext context: Context): AudioRepository {
        return AudioRepositoryImpl(context)
    }
}