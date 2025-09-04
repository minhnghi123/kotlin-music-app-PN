package com.example.musicapp.ui.player

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicapp.models.songs.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerProgress(val positionMs: Long, val durationMs: Long)

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val _currentSong = MutableLiveData<Song?>(null)
    val currentSong: LiveData<Song?> = _currentSong

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _progress = MutableLiveData(PlayerProgress(0L, 0L))
    val progress: LiveData<PlayerProgress> = _progress

    val player: ExoPlayer = ExoPlayer.Builder(app).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .build(),
            true
        )
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.postValue(isPlaying)
            }
        })
    }

    fun play(song: Song) {
        // NOTE: đổi "song.streamUrl" theo model của bạn
        val mediaItem = MediaItem.Builder()
            .setUri(song.fileUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist.fullName)
                    .setArtworkUri(Uri.parse(song.coverImage ?: ""))
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _currentSong.postValue(song)
    }

    fun toggle() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    init {
        // Tick cập nhật tiến độ mỗi 500ms
        viewModelScope.launch {
            while (isActive) {
                val duration = if (player.duration > 0) player.duration else 0L
                val position = player.currentPosition
                _progress.postValue(PlayerProgress(position, duration))
                delay(500)
            }
        }
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
