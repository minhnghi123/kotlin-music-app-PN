package com.example.musicapp.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicapp.R
import com.example.musicapp.ui.player.PlayerHolder

class MusicService : Service() {

    // Dùng player chung từ PlayerHolder; khởi tạo fallback nếu chưa có
    private val player: ExoPlayer
        get() = PlayerHolder.player

    private var isPlaying: Boolean = false
    private var currentSongTitle: String = "PN Music"
    private var currentSongArtist: String = "Now playing..."

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Nếu PlayerHolder.player chưa được khởi tạo (ví dụ user start service trực tiếp),
        // khởi tạo 1 ExoPlayer và gán cho PlayerHolder
        val p = kotlin.runCatching { PlayerHolder.player }.getOrElse {
            PlayerHolder.player = ExoPlayer.Builder(this).build().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                    true
                )
            }
            PlayerHolder.player
        }

        // Lắng nghe player để cập nhật notification tự động khi có thay đổi
        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                this@MusicService.isPlaying = isPlayingNow
                showMusicNotification()
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                mediaMetadata.title?.let { currentSongTitle = it.toString() }
                mediaMetadata.artist?.let { currentSongArtist = it.toString() }
                showMusicNotification()
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "music_channel",
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for music playback controls"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


    private fun getCurrentAlbumArt(): Bitmap? {
        // TODO: thay bằng ảnh tải từ network nếu bạn có coverImage
        return BitmapFactory.decodeResource(resources, R.drawable.ic_default_album_art)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val songTitleFromIntent = intent?.getStringExtra("SONG_TITLE")
        val songArtistFromIntent = intent?.getStringExtra("SONG_ARTIST")
        val songUrl = intent?.getStringExtra("SONG_URL")

        when (action) {
            MusicActions.ACTION_PLAY -> {
                // nếu truyền url + title thì load bài mới
                if (!songUrl.isNullOrEmpty()) {
                    playNewSong(songUrl, songTitleFromIntent, songArtistFromIntent)
                } else {
                    if (!player.isPlaying) player.play()
                }
            }
            MusicActions.ACTION_PAUSE -> {
                if (player.isPlaying) player.pause()
            }
            MusicActions.ACTION_NEXT -> {
                // TODO: logic next: nếu bạn có playlist, advance và player.play()
            }
            MusicActions.ACTION_PREV -> {
                // TODO: logic prev
            }
            else -> {
                // Start service with a song -> play it
                if (!songUrl.isNullOrEmpty()) {
                    playNewSong(songUrl, songTitleFromIntent, songArtistFromIntent)
                } else {
                    // giữ notification hiện trạng
                    showMusicNotification()
                }
            }
        }

        // Service chạy foreground để notification luôn hiện
        return START_STICKY
    }

    private fun playNewSong(url: String, title: String?, artist: String?) {
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title ?: "Unknown")
                    .setArtist(artist ?: "Unknown")
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        // metadata sẽ trigger onMediaMetadataChanged -> update notification
    }

    private fun showMusicNotification() {
        val isPlayingNow = kotlin.runCatching { player.isPlaying }.getOrDefault(false)
        val meta = kotlin.runCatching { player.mediaMetadata }.getOrNull()
        val title = meta?.title?.toString() ?: currentSongTitle
        val artist = meta?.artist?.toString() ?: currentSongArtist

        // Previous (gửi broadcast về receiver)
        val prevIntent = Intent(this, MusicControlReceiver::class.java).apply {
            action = MusicActions.ACTION_PREV
        }
        val prevPendingIntent = PendingIntent.getBroadcast(
            this, 123, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Next
        val nextIntent = Intent(this, MusicControlReceiver::class.java).apply {
            action = MusicActions.ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getBroadcast(
            this, 124, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Play / Pause
        val playPauseIntent = Intent(this, MusicControlReceiver::class.java).apply {
            action = if (isPlayingNow) MusicActions.ACTION_PAUSE else MusicActions.ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getBroadcast(
            this, 125, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlayingNow) R.drawable.ic_pause else R.drawable.ic_play
        val playPauseText = if (isPlayingNow) "Pause" else "Play"

        val albumArtBitmap = getCurrentAlbumArt()

        val notificationBuilder = NotificationCompat.Builder(this, "music_channel")
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.ic_default_album_art)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlayingNow)
            .setStyle(MediaStyle().setShowActionsInCompactView(0, 1, 2))
            .addAction(R.drawable.ic_prev, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, playPauseText, playPausePendingIntent)
            .addAction(R.drawable.ic_next, "Next", nextPendingIntent)

        albumArtBitmap?.let { notificationBuilder.setLargeIcon(it) }

        val openAppIntent = packageManager.getLaunchIntentForPackage(packageName)
        val contentPendingIntent = if (openAppIntent != null) {
            PendingIntent.getActivity(this, 126, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else null
        notificationBuilder.setContentIntent(contentPendingIntent)

        startForeground(1, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
