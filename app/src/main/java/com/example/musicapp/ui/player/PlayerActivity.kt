package com.example.musicapp.ui.player

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.data.FavoriteSongsRepository
import com.example.musicapp.models.songs.Song
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: androidx.media3.exoplayer.ExoPlayer
    private lateinit var favoriteRepository: FavoriteSongsRepository
    
    private lateinit var imgVinyl: ImageView
    private lateinit var imgAlbumArt: ImageView
    private lateinit var tvSongTitle: TextView
    private lateinit var tvArtistName: TextView
    private lateinit var btnPlayPause: FloatingActionButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnComment: LinearLayout
    private lateinit var btnShare: LinearLayout
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    
    private var vinylRotation: ObjectAnimator? = null
    private var isPlaying = false
    private var isFavorite = false
    private val handler = Handler(Looper.getMainLooper())
    private var updateSeekBarRunnable: Runnable? = null
    private var currentSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Lấy ExoPlayer từ PlayerHolder (shared instance)
        player = PlayerHolder.player
        favoriteRepository = FavoriteSongsRepository()

        initViews()
        setupListeners()
        setupPlayerListener()
        
        // Load dữ liệu ban đầu từ PlayerHolder
        PlayerHolder.currentSong?.let { song ->
            currentSong = song
            updateUI(song)
            
            // Sync trạng thái player
            isPlaying = player.isPlaying
            checkIfFavorite(song._id)
            
            // Update UI controls
            updatePlayPauseButton(isPlaying)
            
            // Set seekbar max nếu có duration
            if (player.duration > 0) {
                seekBar.max = player.duration.toInt()
                tvTotalTime.text = formatTime(player.duration.toInt())
                seekBar.progress = player.currentPosition.toInt()
                tvCurrentTime.text = formatTime(player.currentPosition.toInt())
            }
            
            if (isPlaying) {
                startVinylRotation()
                startSeekBarUpdate()
            }
        }
    }

    private fun initViews() {
        imgVinyl = findViewById(R.id.imgVinyl)
        imgAlbumArt = findViewById(R.id.imgAlbumArt)
        tvSongTitle = findViewById(R.id.tvSongTitle)
        tvArtistName = findViewById(R.id.tvArtistName)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnFavorite = findViewById(R.id.btnFavorite)
        btnBack = findViewById(R.id.btnBack)
        btnComment = findViewById(R.id.btnComment)
        btnShare = findViewById(R.id.btnShare)
        seekBar = findViewById(R.id.seekBar)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        btnPrevious.setOnClickListener {
            player.seekToPrevious()
        }

        btnNext.setOnClickListener {
            player.seekToNext()
        }

        btnFavorite.setOnClickListener {
            toggleFavorite()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvCurrentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                stopSeekBarUpdate()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    player.seekTo(it.progress.toLong())
                    if (isPlaying) {
                        startSeekBarUpdate()
                    }
                }
            }
        })

        btnComment.setOnClickListener {
            currentSong?.let { song ->
                val commentsSheet = CommentsBottomSheet.newInstance(song._id)
                commentsSheet.show(supportFragmentManager, "CommentsBottomSheet")
            }
        }

        btnShare.setOnClickListener {
            currentSong?.let { song ->
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Check out this song: ${song.title} by ${song.artist.fullName}")
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Share song via"))
            }
        }

        // Menu button (3 dots)
        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener { view ->
            showSongMenu(view)
        }
    }

    private fun setupPlayerListener() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                updatePlayPauseButton(playing)
                if (playing) {
                    startVinylRotation()
                    startSeekBarUpdate()
                } else {
                    pauseVinylRotation()
                    stopSeekBarUpdate()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        val duration = player.duration.toInt()
                        seekBar.max = duration
                        tvTotalTime.text = formatTime(duration)
                    }
                }
            }
        })
    }

    private fun showSongMenu(view: android.view.View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.song_item_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add_playlist -> {
                    currentSong?.let { song ->
                        Toast.makeText(this, "Add '${song.title}' to playlist", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.action_share -> {
                    currentSong?.let { song ->
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Check out: ${song.title} by ${song.artist.fullName}\n${song.fileUrl}")
                            type = "text/plain"
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share via"))
                    }
                    true
                }
                R.id.action_favorite -> {
                    toggleFavorite()
                    true
                }
                R.id.action_download -> {
                    currentSong?.let { song ->
                        Toast.makeText(this, "Download: ${song.title}", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.action_artist -> {
                    currentSong?.let { song ->
                        Toast.makeText(this, "Artist: ${song.artist.fullName}", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.action_album -> {
                    currentSong?.let { song ->
                        Toast.makeText(this, "Album: ${song.album}", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun checkIfFavorite(songId: String) {
        lifecycleScope.launch {
            try {
                favoriteRepository.getFavoriteSongs { songs, error, favoriteSongIds ->
                    if (favoriteSongIds != null) {
                        isFavorite = favoriteSongIds.contains(songId)
                        runOnUiThread {
                            updateFavoriteButton()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun toggleFavorite() {
        currentSong?.let { song ->
            lifecycleScope.launch {
                try {
                    if (isFavorite) {
                        favoriteRepository.removeFavoriteSong(song._id) { success, error ->
                            if (success == true) {
                                isFavorite = false
                                runOnUiThread {
                                    updateFavoriteButton()
                                    Toast.makeText(this@PlayerActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@PlayerActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        favoriteRepository.addFavoriteSong(song._id) { success, error ->
                            if (success == true) {
                                isFavorite = true
                                runOnUiThread {
                                    updateFavoriteButton()
                                    Toast.makeText(this@PlayerActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@PlayerActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@PlayerActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateFavoriteButton() {
        btnFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )
        btnFavorite.alpha = if (isFavorite) 1.0f else 0.6f
    }

    private fun updateUI(song: Song) {
        tvSongTitle.text = song.title
        tvArtistName.text = song.artist.fullName

        Glide.with(this)
            .load(song.coverImage)
            .placeholder(R.drawable.ic_default_album_art)
            .error(R.drawable.ic_default_album_art)
            .centerCrop()
            .into(imgAlbumArt)
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun startVinylRotation() {
        if (vinylRotation == null) {
            vinylRotation = ObjectAnimator.ofFloat(imgVinyl, "rotation", 0f, 360f).apply {
                duration = 10000
                repeatCount = ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()
            }
        }
        
        if (vinylRotation?.isPaused == true) {
            vinylRotation?.resume()
        } else if (vinylRotation?.isRunning != true) {
            vinylRotation?.start()
        }
    }

    private fun pauseVinylRotation() {
        vinylRotation?.pause()
    }

    private fun startSeekBarUpdate() {
        if (updateSeekBarRunnable == null) {
            updateSeekBarRunnable = object : Runnable {
                override fun run() {
                    if (isPlaying && player.duration > 0) {
                        val position = player.currentPosition.toInt()
                        val duration = player.duration.toInt()
                        
                        if (!seekBar.isPressed) {
                            seekBar.max = duration
                            seekBar.progress = position
                            tvCurrentTime.text = formatTime(position)
                            tvTotalTime.text = formatTime(duration)
                        }
                        
                        handler.postDelayed(this, 500)
                    }
                }
            }
        }
        updateSeekBarRunnable?.let { handler.post(it) }
    }

    private fun stopSeekBarUpdate() {
        updateSeekBarRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        vinylRotation?.cancel()
        vinylRotation = null
        stopSeekBarUpdate()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        PlayerHolder.currentSong?.let { song ->
            currentSong = song
            updateUI(song)
            checkIfFavorite(song._id)
        }
        isPlaying = player.isPlaying
        updatePlayPauseButton(isPlaying)
        if (isPlaying) {
            startVinylRotation()
            startSeekBarUpdate()
        }
    }
}
