package com.example.musicapp.ui.search

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.ai.MusicQueryProcessor
import com.example.musicapp.ai.VoiceSearchManager
import com.example.musicapp.models.artists.Artist
import com.example.musicapp.models.songs.Song
import com.example.musicapp.ui.home.SongViewModel
import com.example.musicapp.network.ApiClient
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.speech.RecognizerIntent
import android.util.Log
import java.text.Normalizer
import java.util.Locale
import android.os.SystemClock
import com.example.musicapp.models.topic.Topic
import com.example.musicapp.models.topic.TopicResponse
import com.example.musicapp.ui.topic.TopicAdapter
import com.example.musicapp.ui.topic.TopicSongsFragment


class SearchFragment : Fragment() {

    private lateinit var etSearch: AutoCompleteTextView
    private lateinit var layoutSuggestions: FlexboxLayout
    private lateinit var tvClearRecent: TextView
    private lateinit var tvArtistResultsTitle: TextView

    private lateinit var rvRecentSearches: RecyclerView
    private lateinit var rvArtistResults: RecyclerView

    private lateinit var songViewModel: SongViewModel
    private lateinit var recentAdapter: RecentSearchAdapter

    private var allSongs: List<Song> = emptyList()
    private var allArtists: List<Artist> = emptyList()
    private var recentSearches: MutableList<Song> = mutableListOf()

    private var suggestionAdapter: ArrayAdapter<String>? = null
    private val suggestions = mutableListOf<String>()

    private val gson = Gson()

    private lateinit var btnVoiceSearch: ImageButton
    private lateinit var voiceSearchManager: VoiceSearchManager
    private lateinit var musicQueryProcessor: MusicQueryProcessor

    private lateinit var voiceSearchLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private var voiceSearchDialog: AlertDialog? = null
    private var voiceSearchTimeout: Runnable? = null
    private val voiceHandler = Handler(Looper.getMainLooper())
    // === Topic Mode ===
    private lateinit var layoutTopics: LinearLayout
    private lateinit var rvTopics: RecyclerView
    private lateinit var topicAdapter: com.example.musicapp.ui.topic.TopicAdapter
    private var topicList: List<com.example.musicapp.models.topic.Topic> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        voiceSearchLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )?.firstOrNull()

                if (!spokenText.isNullOrEmpty()) {
                    handleVoiceSearchResult(spokenText)
                }
            }
        }

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                startVoiceSearch()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Microphone permission required for voice search",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        etSearch = view.findViewById(R.id.etSearch)
        layoutSuggestions = view.findViewById(R.id.layoutSuggestions)
        tvClearRecent = view.findViewById(R.id.tvClearRecent)
        rvRecentSearches = view.findViewById(R.id.rvRecentSearches)
        rvArtistResults = view.findViewById(R.id.rvArtistResults)
        btnVoiceSearch = view.findViewById(R.id.btnVoiceSearch)
        tvArtistResultsTitle = view.findViewById(R.id.tvArtistResultsTitle)
        layoutTopics = view.findViewById(R.id.layoutTopics)
        rvTopics = view.findViewById(R.id.rvTopics)
        rvTopics.layoutManager = LinearLayoutManager(requireContext())


        // Recent RecyclerView
        rvRecentSearches.layoutManager = LinearLayoutManager(requireContext())
        recentAdapter = RecentSearchAdapter(
            onItemClick = { song ->
                etSearch.setText(song.title)
                etSearch.setSelection(song.title.length)
                performSearch(song.title)
            },
            onMoreClick = { song ->
                Toast.makeText(requireContext(), "More: ${song.title}", Toast.LENGTH_SHORT).show()
            }
        )
        rvRecentSearches.adapter = recentAdapter

        // Artist results RecyclerView (vertical list of artist cards)
        rvArtistResults.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        // Start with empty adapter
        val searchArtistAdapter = SearchArtistAdapter(emptyList(),
            onItemClick = { artist ->
                // Mở ArtistDetail
                openArtistDetail(artist._id)
            },
            onMoreClick = { artist ->
                // tuỳ xử lý more
            }
        )
        rvArtistResults.adapter = searchArtistAdapter

        // AutoComplete Suggestions (for songs)
        suggestionAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
        etSearch.setAdapter(suggestionAdapter)
        etSearch.threshold = 1

        // ==================== SEARCH INPUT EVENTS ====================

// 8) Khi focus vào ô tìm kiếm → vào Search Mode (nếu có text)
        etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (etSearch.text.toString().isNotEmpty()) {
                    showSearchMode()
                } else {
                    // không có gì để tìm, vẫn giữ Topic Mode
                    showTopicMode()
                }
            }
        }

// Khi click vào ô search -> chuyển sang Search Mode luôn
        etSearch.setOnClickListener {
            if (etSearch.text.toString().isNotEmpty()) {
                showSearchMode()
            } else {
                showTopicMode()
            }
        }

// Khi click 1 suggestion (AutoComplete)
        etSearch.setOnItemClickListener { parent, _, position, _ ->
            val selected = suggestions.getOrNull(position)
            selected?.let { sel ->
                val song = allSongs.find { it.title.equals(sel, ignoreCase = true) }
                if (song != null) {
                    addToRecentSearch(song)
                    etSearch.setText(song.title)
                    etSearch.setSelection(song.title.length)
                    performSearch(song.title)
                } else {
                    etSearch.setText(sel)
                }

                // Khi chọn suggestion -> vào Search Mode
                showSearchMode()
            }
        }

// 7 + 9) Khi thay đổi text → Search Mode nếu có chữ, Topic Mode nếu rỗng
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()

                if (query.isEmpty()) {
                    // ❗ Bước 9: Khi xóa hết text -> quay về Topic Mode
                    showTopicMode()

                    rvArtistResults.visibility = View.GONE
                    tvArtistResultsTitle.visibility = View.GONE

                    suggestions.clear()
                    suggestionAdapter?.notifyDataSetChanged()
                    return
                }

                // ❗ Bước 7: Khi có text, chuyển sang Search Mode
                showSearchMode()

                performSearch(query)

                try {
                    if (!etSearch.isPopupShowing) etSearch.showDropDown()
                } catch (_: Exception) {}
            }
        })

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                val query = etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    // ❗ Khi nhấn Search -> phải vô Search Mode
                    showSearchMode()
                    performSearch(query)
                    hideKeyboard()
                }

                true
            } else false
        }

        // ViewModel
        songViewModel = ViewModelProvider(this)[SongViewModel::class.java]
        songViewModel.songs.observe(viewLifecycleOwner) { songs ->
            allSongs = songs
            updateSuggestionsTags()
        }
        songViewModel.fetchSongs()

        // 🟢 KHỞI TẠO ADAPTER TRƯỚC
        topicAdapter = TopicAdapter(emptyList()) { topic ->
            openTopicDetail(topic)
        }
        rvTopics.adapter = topicAdapter

        // Load artists list (we use hot artists endpoint as source; adapt if you have a dedicated endpoint)
        fetchAllArtists()
        fetchTopics()
        showTopicMode()

        loadSearchHistory()
        updateRecentSearchUI()
        addSuggestionTags(getSuggestionsFromHistory())

        tvClearRecent.setOnClickListener { clearSearchHistory() }

        voiceSearchManager = VoiceSearchManager(requireContext())
        musicQueryProcessor = MusicQueryProcessor()

        btnVoiceSearch.setOnClickListener { checkPermissionAndStartVoiceSearch() }

        // Initially hide artist results area
        rvArtistResults.visibility = View.GONE

        return view
    }

    // ==================== SEARCH LOGIC ====================
    // helper: remove diacritics + lowercase + trim
    private fun normalize(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val n = Normalizer.normalize(input, Normalizer.Form.NFD)
        return Regex("\\p{InCombiningDiacriticalMarks}+")
            .replace(n, "")
            .lowercase(Locale.getDefault())
            .trim()
    }

    // helper: kiểm tra 1 trong các từ trong title có bắt đầu bằng query (normalize)
    private fun wordStartsWith(text: String, queryNorm: String): Boolean {
        if (queryNorm.isEmpty()) return false
        val tokens = text.split(Regex("\\s+"))
        for (t in tokens) {
            if (normalize(t).startsWith(queryNorm)) return true
        }
        return false
    }
    private fun performSearch(query: String) {
        val qNorm = normalize(query)
        if (qNorm.isEmpty()) {
            // nếu rỗng, reset UI
            suggestions.clear()
            suggestionAdapter?.notifyDataSetChanged()
            rvArtistResults.visibility = View.GONE
            tvArtistResultsTitle.visibility = View.GONE
            return
        }

        // Tạo danh sách pair(song, score)
        val scored = mutableListOf<Pair<Song, Int>>()

        allSongs.forEach { song ->
            val titleNorm = normalize(song.title)
            var score = 0

            // highest priority: title startsWith (toàn bộ title bắt đầu)
            if (titleNorm.startsWith(qNorm)) {
                score += 120
            }
            // title một từ bắt đầu bằng query (ví dụ query "qu" match "Quên Anh Đi")
            if (wordStartsWith(song.title, qNorm)) {
                score += 100
            }
            // title chứa query anywhere
            if (titleNorm.contains(qNorm)) {
                score += 70
            }

            // artist match: nếu một artist fullName startsWith query
            val artistMatchStarts = song.artist.any { normalize(it.fullName).startsWith(qNorm) }
            val artistMatchContains = song.artist.any { normalize(it.fullName).contains(qNorm) }
            if (artistMatchStarts) score += 90
            else if (artistMatchContains) score += 60

            // topic match
            val topicMatch = song.topic.any { normalize(it).contains(qNorm) }
            if (topicMatch) score += 40

            // nếu có ít nhất 1 điểm thì add
            if (score > 0) {
                scored.add(song to score)
            }
        }

        // Sắp xếp theo score desc, nếu bằng nhau ưu tiên theo title alphabetical
        val sorted = scored
            .sortedWith(compareByDescending<Pair<Song, Int>> { it.second }
                .thenBy { normalize(it.first.title) })
            .map { it.first }

        // Nếu muốn include songs that matched only by artist (but not title),
        // đảm bảo họ vẫn xuất hiện vì chúng đã được tính điểm ở phần artistMatch.

        // Build suggestions list: lấy title từ sorted, distinct theo title, lấy top 10
        suggestions.clear()
        suggestions.addAll(sorted.map { it.title }.distinct().take(10))
        suggestionAdapter?.notifyDataSetChanged()
        suggestionAdapter?.notifyDataSetChanged()

        etSearch.postDelayed({
            if (etSearch.isFocused && suggestions.isNotEmpty()) {
                try {
                    etSearch.showDropDown()
                } catch (_: Exception) {}
            }
        }, 120)   // 120ms để layout ổn định

        // Artist results: tìm artists matching (hiển thị list phía dưới)
        val matchedArtists = allArtists.filter { normalize(it.fullName).contains(qNorm) }
        if (matchedArtists.isNotEmpty()) {
            rvArtistResults.visibility = View.VISIBLE
            tvArtistResultsTitle.visibility = View.VISIBLE
            // cập nhật adapter của artist (SearchArtistAdapter) nếu dùng
            (rvArtistResults.adapter as? SearchArtistAdapter)?.update(matchedArtists)
        } else {
            rvArtistResults.visibility = View.GONE
            tvArtistResultsTitle.visibility = View.GONE
        }
    }

    // ==================== TAG SUGGESTIONS (chips) ====================
    private fun addSuggestionTags(suggestions: List<String>) {
        layoutSuggestions.removeAllViews()
        for (text in suggestions) {
            val tv = TextView(requireContext()).apply {
                this.text = text
                setPadding(36, 18, 36, 18)
                setBackgroundResource(R.drawable.bg_tag_suggestion)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.textColorPrimary))
                textSize = 14f
                setSingleLine(true)
                ellipsize = TextUtils.TruncateAt.END
                maxWidth = 1000
                setOnClickListener {
                    etSearch.setText(text)
                    etSearch.setSelection(text.length)
                    performSearch(text)
                }

                val params = com.google.android.flexbox.FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(12, 12, 12, 12)
                layoutParams = params
            }
            layoutSuggestions.addView(tv)
        }
    }

    private fun updateSuggestionsTags() {
        val dynamicSuggestions = allSongs.shuffled().take(6).map { it.title }
        addSuggestionTags(dynamicSuggestions)
    }

    // ==================== HISTORY ====================
    private fun addToRecentSearch(song: Song) {
        if (recentSearches.none { it.title == song.title }) {
            recentSearches.add(0, song)
            if (recentSearches.size > 10) recentSearches.removeAt(recentSearches.lastIndex)
            recentAdapter.updateData(recentSearches)
            saveSearchHistory()
        }
    }

    private fun saveSearchHistory() {
        val prefs = requireContext().getSharedPreferences("search_history", Context.MODE_PRIVATE)
        prefs.edit().putString("recent_searches_json", gson.toJson(recentSearches)).apply()
    }

    private fun loadSearchHistory() {
        val prefs = requireContext().getSharedPreferences("search_history", Context.MODE_PRIVATE)
        val json = prefs.getString("recent_searches_json", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Song>>() {}.type
            try {
                recentSearches = gson.fromJson(json, type)
            } catch (_: Exception) {
                recentSearches.clear()
            }
        }
    }

    private fun updateRecentSearchUI() {
        recentAdapter.updateData(recentSearches)
    }

    private fun getSuggestionsFromHistory(): List<String> {
        return recentSearches.take(6).map { it.title }
    }

    private fun clearSearchHistory() {
        recentSearches.clear()
        recentAdapter.updateData(recentSearches)
        val prefs = requireContext().getSharedPreferences("search_history", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Toast.makeText(requireContext(), "Đã xóa lịch sử tìm kiếm", Toast.LENGTH_SHORT).show()
    }

    // ==================== UTILS ====================
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
    }

    private fun checkPermissionAndStartVoiceSearch() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED -> startVoiceSearch()

            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(requireContext(), "Microphone permission needed for voice search", Toast.LENGTH_SHORT).show()
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }

            else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startVoiceSearch() {
        showVoiceListeningDialog()

        voiceSearchTimeout = Runnable {
            voiceSearchDialog?.dismiss()
            Toast.makeText(requireContext(), "⏱️ Voice search timeout. Please try again.", Toast.LENGTH_SHORT).show()
        }
        voiceHandler.postDelayed(voiceSearchTimeout!!, 10000)

        voiceSearchManager.startVoiceSearchWithIntent(this, voiceSearchLauncher)
    }

    private fun showVoiceListeningDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_voice_listening, null)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancelVoice)
        val imgMic = dialogView.findViewById<ImageView>(R.id.imgMicAnimation)

        voiceSearchDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            voiceSearchDialog?.dismiss()
            voiceSearchTimeout?.let { voiceHandler.removeCallbacks(it) }
        }

        val pulseAnimation = android.view.animation.AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.pulse_animation
        )
        imgMic.startAnimation(pulseAnimation)

        voiceSearchDialog?.show()
    }

    private fun handleVoiceSearchResult(spokenText: String) {
        Log.d("SearchFragment", "Voice input: $spokenText")

        voiceSearchDialog?.dismiss()
        voiceSearchTimeout?.let { voiceHandler.removeCallbacks(it) }

        showVoiceResultDialog(spokenText)

        etSearch.setText(spokenText)
        etSearch.setSelection(spokenText.length)

        val intent = musicQueryProcessor.processQuery(spokenText)
        val results = musicQueryProcessor.filterSongs(allSongs, intent)

        if (results.isNotEmpty()) {
            addToRecentSearch(results.first())
            suggestions.clear()
            suggestions.addAll(results.take(10).map { it.title })
            suggestionAdapter?.notifyDataSetChanged()
            etSearch.showDropDown()
        } else {
            Toast.makeText(requireContext(), "❌ No results for \"$spokenText\"", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showVoiceResultDialog(spokenText: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_voice_result, null)
        val tvResult = dialogView.findViewById<TextView>(R.id.tvVoiceResult)
        val btnConfirm = dialogView.findViewById<TextView>(R.id.btnConfirmVoice)
        val btnRetry = dialogView.findViewById<TextView>(R.id.btnRetryVoice)

        tvResult.text = "You said:\n\"$spokenText\""

        val resultDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnConfirm.setOnClickListener { resultDialog.dismiss() }
        btnRetry.setOnClickListener {
            resultDialog.dismiss()
            checkPermissionAndStartVoiceSearch()
        }

        resultDialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            resultDialog.dismiss()
        }, 3000)
    }

    // ==================== HELPERS: Artists loading & open detail ====================
    private fun fetchAllArtists() {
        // sử dụng API getHotArtists() làm nguồn danh sách nghệ sĩ.
        // Nếu có endpoint lấy all artists, thay bằng endpoint đó.
        ApiClient.api.getHotArtists().enqueue(object : Callback<com.example.musicapp.models.artists.ArtistResponse> {
            override fun onResponse(
                call: Call<com.example.musicapp.models.artists.ArtistResponse>,
                response: Response<com.example.musicapp.models.artists.ArtistResponse>
            ) {
                if (!isAdded) return
                if (response.isSuccessful && response.body()?.data != null) {
                    allArtists = response.body()!!.data
                } else {
                    // Không cần show lỗi ở đây
                }
            }

            override fun onFailure(call: Call<com.example.musicapp.models.artists.ArtistResponse>, t: Throwable) {
                Log.e("SearchFragment", "Failed to fetch artists: ${t.message}", t)
            }
        })
    }

    private fun fetchTopics() {
        ApiClient.api.getTopics().enqueue(object : Callback<TopicResponse> {
            override fun onResponse(call: Call<TopicResponse>, response: Response<TopicResponse>) {
                if (!isAdded) return

                if (response.isSuccessful && response.body()?.data != null) {

                    topicList = response.body()!!.data

                    // chỉ update data, không tạo lại adapter
                    topicAdapter.updateData(topicList)
                }
            }

            override fun onFailure(call: Call<TopicResponse>, t: Throwable) {
                Log.e("SearchFragment", "Failed to load topics: ${t.message}")
            }
        })
    }

    private fun openArtistDetail(artistId: String) {
        val fragment = com.example.musicapp.ui.artist.ArtistDetailFragment().apply {
            arguments = Bundle().apply {
                putString("artistId", artistId)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("ARTIST_DETAIL")
            .commit()
    }

    private fun openTopicDetail(topic: Topic) {
        val fragment = TopicSongsFragment.newInstance(
            topic.id,
            topic.title,
            topic.imgTopic
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("TOPIC_DETAIL")
            .commit()
    }

    private fun showTopicMode() {
        layoutTopics.visibility = View.VISIBLE

        layoutSuggestions.visibility = View.GONE
        rvRecentSearches.visibility = View.GONE
        tvClearRecent.visibility = View.GONE
        rvArtistResults.visibility = View.GONE
        tvArtistResultsTitle.visibility = View.GONE
    }

    private fun showSearchMode() {
        layoutTopics.visibility = View.GONE

        layoutSuggestions.visibility = View.VISIBLE
        rvRecentSearches.visibility = View.VISIBLE
        tvClearRecent.visibility = View.VISIBLE
    }

}
