package com.example.musicapp.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.musicapp.R

class MiniPlayerFragment : Fragment() {

    private val playerVM: PlayerViewModel by activityViewModels()
    private lateinit var imgCover: ImageView
    private lateinit var txtTitle: TextView
    private lateinit var txtArtist: TextView
    private lateinit var btnToggle: ImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_mini_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imgCover = view.findViewById(R.id.imgMiniCover)
        txtTitle = view.findViewById(R.id.txtMiniTitle)
        txtArtist = view.findViewById(R.id.txtMiniArtist)
        btnToggle = view.findViewById(R.id.btnMiniToggle)

        // 1) Ẩn/hiện mini theo currentSong
        playerVM.currentSong.observe(viewLifecycleOwner) { song ->
            view.isVisible = song != null
            if (song != null) {
                txtTitle.text = song.title
                txtArtist.text = song.artist.fullName
                Glide.with(this)
                    .load(song.coverImage)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.drawable.img_error)
                    .into(imgCover)
            }
        }

        // 2) Đổi icon theo trạng thái play/pause
        playerVM.isPlaying.observe(viewLifecycleOwner) { playing ->
            btnToggle.setImageResource(
                if (playing) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play
            )
        }

        // 3) Nhấn play/pause
        btnToggle.setOnClickListener { playerVM.toggle() }

        // Nhấn vào mini mở màn hình Player đầy đủ
        view.setOnClickListener {
            // TODO: startActivity(PlayerActivity) nếu bạn muốn
        }
    }
}
