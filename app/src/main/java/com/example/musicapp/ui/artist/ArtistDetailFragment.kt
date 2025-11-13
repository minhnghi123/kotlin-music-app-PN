package com.example.musicapp.ui.artist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.artists.ArtistDetailResponse
import com.example.musicapp.models.artists.ArtistResponse
import com.example.musicapp.network.ApiClient
import com.example.musicapp.ui.home.SongAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArtistDetailFragment : Fragment() {

    private lateinit var rvSongs: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var tvArtistName: TextView
    private lateinit var ivArtistCover: ImageView
    private lateinit var btnShowMoreSong: TextView

    // ---- Hot singers
    private lateinit var rvHotSingers: RecyclerView
    private lateinit var hotArtistAdapter: ArtistAdapter

    // ---- Information about artist
    private lateinit var tvArtistCountry: TextView
    private lateinit var tvArtistRealName: TextView

    private var artistId: String? = null
    private var fullSongList: List<com.example.musicapp.models.songs.Song> = emptyList()
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        artistId = arguments?.getString("artistId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_artist_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvSongs = view.findViewById(R.id.rvSongsByArtist)
        tvArtistName = view.findViewById(R.id.tvArtistName)
        ivArtistCover = view.findViewById(R.id.ivArtistCover)
        btnShowMoreSong = view.findViewById(R.id.btnShowMoreSong)

        // Songs RecyclerView
        rvSongs.layoutManager = LinearLayoutManager(requireContext())
        songAdapter = SongAdapter(emptyList()) { song ->
            (activity as? com.example.musicapp.MainActivity)?.showMiniPlayer(song)
        }
        rvSongs.adapter = songAdapter

        // Nút xem thêm / thu gọn
        btnShowMoreSong.setOnClickListener {
            toggleSongs()
        }

        // Hot singers RecyclerView
        rvHotSingers = view.findViewById(R.id.rvHotSingers)
        rvHotSingers.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        hotArtistAdapter = ArtistAdapter(emptyList()) { artist ->
            if (artist._id == artistId) {
                Toast.makeText(requireContext(), "Đang xem ${artist.fullName}", Toast.LENGTH_SHORT).show()
                return@ArtistAdapter
            }
            val frag = ArtistDetailFragment().apply {
                arguments = Bundle().apply { putString("artistId", artist._id) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, frag)
                .addToBackStack("ARTIST_DETAIL")
                .commit()
        }
        rvHotSingers.adapter = hotArtistAdapter

        if (artistId != null) {
            loadArtistSongs(artistId!!)
            loadHotSingers(artistId!!) // bỏ singer hiện tại
        } else {
            Toast.makeText(requireContext(), "Không có artistId", Toast.LENGTH_SHORT).show()
        }

        // Thông tin ca sĩ
        tvArtistRealName = view.findViewById(R.id.tvArtistRealName)
        tvArtistCountry = view.findViewById(R.id.tvArtistCountry)
    }

    private fun loadArtistSongs(artistId: String) {
        android.util.Log.d("ArtistDetailFragment", "=== Loading artist: $artistId ===")
        
        ApiClient.api.getArtistDetail(artistId).enqueue(object : Callback<ArtistDetailResponse> {
            override fun onResponse(
                call: Call<ArtistDetailResponse>,
                response: Response<ArtistDetailResponse>
            ) {
                if (!isAdded) return
                
                android.util.Log.d("ArtistDetailFragment", "Response code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    
                    android.util.Log.d("ArtistDetailFragment", "Artist: ${body.artist.fullName}")
                    android.util.Log.d("ArtistDetailFragment", "Country: ${body.artist.country}")
                    android.util.Log.d("ArtistDetailFragment", "Cover: ${body.artist.coverImage}")
                    android.util.Log.d("ArtistDetailFragment", "Songs: ${body.songs.size}")
                    
                    // Update UI
                    tvArtistName.text = body.artist.fullName
                    tvArtistRealName.text = "Tên thật: ${body.artist.fullName}"
                    tvArtistCountry.text = "Quốc gia: ${body.artist.country}"
                    
                    // Load artist cover
                    Glide.with(requireContext())
                        .load(body.artist.coverImage)
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_default_album_art)
                        .centerCrop()
                        .into(ivArtistCover)

                    // Convert SongForArtist to Song
                    fullSongList = body.songs.map { s ->
                        android.util.Log.d("ArtistDetailFragment", "  Song: ${s.title}")
                        android.util.Log.d("ArtistDetailFragment", "    Artists: ${s.artist.size}")
                        s.artist.forEach {
                            android.util.Log.d("ArtistDetailFragment", "      - ${it.fullName}")
                        }
                        
                        com.example.musicapp.models.songs.Song(
                            _id = s._id,
                            title = s.title,
                            artist = s.artist, // SongForArtistDeserializer đã parse đúng
                            album = s.album ?: "",
                            topic = s.topic ?: emptyList(),
                            fileUrl = s.fileUrl ?: "",
                            coverImage = s.coverImage ?: "",
                            likes = s.likes ?: emptyList(),
                            lyrics = s.lyrics,
                            description = s.description,
                            status = s.status ?: "active",
                            deleted = s.deleted ?: false,
                            createdAt = s.createdAt ?: "",
                            updatedAt = s.updatedAt ?: ""
                        )
                    }

                    showLimitedSongs()
                } else {
                    android.util.Log.e("ArtistDetailFragment", "Response not successful")
                    Toast.makeText(requireContext(), "Lỗi tải artist detail", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ArtistDetailResponse>, t: Throwable) {
                if (!isAdded) return
                android.util.Log.e("ArtistDetailFragment", "API error: ${t.message}", t)
                Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // chỉ hiển thị 5 bài
    private fun showLimitedSongs() {
        val limitedList = if (fullSongList.size > 5) fullSongList.take(5) else fullSongList
        songAdapter.submit(limitedList)
        btnShowMoreSong.visibility = if (fullSongList.size > 5) View.VISIBLE else View.GONE
        btnShowMoreSong.text = "Xem thêm"
        isExpanded = false
    }

    // toggle xem thêm / thu gọn
    private fun toggleSongs() {
        if (isExpanded) {
            showLimitedSongs()
        } else {
            songAdapter.submit(fullSongList)
            btnShowMoreSong.text = "Thu gọn"
            isExpanded = true
        }
    }

    // hot singers
    private fun loadHotSingers(currentArtistId: String) {
        ApiClient.api.getHotArtists().enqueue(object : Callback<ArtistResponse> {
            override fun onResponse(call: Call<ArtistResponse>, response: Response<ArtistResponse>) {
                if (!isAdded) return
                if (response.isSuccessful && response.body()?.data != null) {
                    val allArtists = response.body()!!.data
                    val filteredArtists = allArtists.filter { it._id != currentArtistId }
                    hotArtistAdapter.updateArtists(filteredArtists)
                }
            }

            override fun onFailure(call: Call<ArtistResponse>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Không tải được danh sách ca sĩ: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayArtistInfo(artist: com.example.musicapp.models.artists.Artist) {
        tvArtistName.text = artist.fullName
        tvArtistCountry.text = artist.country

        Glide.with(requireContext())
            .load(artist.coverImage)
            .placeholder(R.drawable.ic_user)
            .error(R.drawable.ic_default_album_art)
            .centerCrop()
            .into(ivArtistCover)
    }

    private fun displayArtistSongs(songs: List<com.example.musicapp.models.songs.Song>) {
        val adapter = SongAdapter(songs) { song ->
            (activity as? com.example.musicapp.MainActivity)?.showMiniPlayer(song)
        }
        rvSongs.adapter = adapter
    }
}
