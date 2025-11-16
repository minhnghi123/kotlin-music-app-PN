package com.example.musicapp.ui.topic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.songs.ApiListResponse
import com.example.musicapp.models.songs.Song
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.home.SongAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopicSongsFragment : Fragment() {

    private var topicId: String = ""
    private var topicName: String = ""
    private var topicImage: String? = null

    private lateinit var rvSongs: RecyclerView
    private lateinit var tvTopicName: TextView
    private lateinit var ivTopicCover: ImageView
    private lateinit var btnShowMoreSong: TextView

    private var fullSongList: List<Song> = emptyList()
    private lateinit var songAdapter: SongAdapter
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicId = arguments?.getString("topicId") ?: ""
        topicName = arguments?.getString("topicName") ?: ""
        topicImage = arguments?.getString("topicImage")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_topic_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTopicName = view.findViewById(R.id.tvTopicName)
        ivTopicCover = view.findViewById(R.id.ivTopicCover)
        rvSongs = view.findViewById(R.id.rvSongs)
        btnShowMoreSong = view.findViewById(R.id.btnShowMoreSong)

        tvTopicName.text = topicName

        // Load ảnh topic nếu có
        Glide.with(requireContext())
            .load(topicImage)
            .placeholder(R.drawable.ic_default_album_art)
            .centerCrop()
            .into(ivTopicCover)

        // Setup RecyclerView
        rvSongs.layoutManager = LinearLayoutManager(requireContext())
        songAdapter = SongAdapter(emptyList()) { song ->
            (activity as? com.example.musicapp.MainActivity)?.showMiniPlayer(song)
        }
        rvSongs.adapter = songAdapter

        btnShowMoreSong.setOnClickListener { toggleSongs() }

        // Fetch songs
        fetchSongsForTopic()
    }

    private fun fetchSongsForTopic() {
        ApiClient.api.getSongs().enqueue(object : Callback<ApiListResponse<Song>> {
            override fun onResponse(
                call: Call<ApiListResponse<Song>>,
                response: Response<ApiListResponse<Song>>
            ) {
                if (!isAdded) return

                val allSongs = response.body()?.data ?: emptyList()

                Log.d("TopicSongs", "=== Topic ID: $topicId ===")

                // Filter theo topicId
                fullSongList = allSongs.filter { song ->
                    song.topic.contains(topicId)
                }

                Log.d("TopicSongs", "Found ${fullSongList.size} songs for this topic.")

                // Hiển thị
                showLimitedSongs()
            }

            override fun onFailure(call: Call<ApiListResponse<Song>>, t: Throwable) {
                Log.e("TopicSongs", "API error: ${t.message}")
            }
        })
    }

    private fun showLimitedSongs() {
        val limited = if (fullSongList.size > 5) fullSongList.take(5) else fullSongList

        songAdapter.submit(limited)
        btnShowMoreSong.visibility = if (fullSongList.size > 5) View.VISIBLE else View.GONE
        btnShowMoreSong.text = "Xem thêm"
        isExpanded = false
    }

    private fun toggleSongs() {
        if (isExpanded) {
            showLimitedSongs()
        } else {
            songAdapter.submit(fullSongList)
            btnShowMoreSong.text = "Thu gọn"
            isExpanded = true
        }
    }

    companion object {
        fun newInstance(topicId: String, topicName: String, topicImage: String?): TopicSongsFragment {
            val f = TopicSongsFragment()
            f.arguments = Bundle().apply {
                putString("topicId", topicId)
                putString("topicName", topicName)
                putString("topicImage", topicImage)
            }
            return f
        }
    }
}
