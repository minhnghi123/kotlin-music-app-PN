package com.example.musicapp.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapp.R
import com.example.musicapp.models.songs.Song
import com.example.musicapp.ui.home.SongViewModel
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchFragment : Fragment() {

    private lateinit var etSearch: AutoCompleteTextView
    private lateinit var layoutSuggestions: FlexboxLayout
    private lateinit var tvClearRecent: TextView
    private lateinit var rvRecentSearches: androidx.recyclerview.widget.RecyclerView
    private lateinit var songViewModel: SongViewModel

    private lateinit var recentAdapter: RecentSearchAdapter
    private var allSongs: List<Song> = emptyList()
    private var recentSearches: MutableList<Song> = mutableListOf()
    private var suggestionAdapter: ArrayAdapter<String>? = null

    private val suggestions = mutableListOf<String>()
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Ánh xạ view
        etSearch = view.findViewById(R.id.etSearch)
        layoutSuggestions = view.findViewById(R.id.layoutSuggestions)
        tvClearRecent = view.findViewById(R.id.tvClearRecent)
        rvRecentSearches = view.findViewById(R.id.rvRecentSearches)

        // Setup RecyclerView hiển thị tìm kiếm gần đây
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

        // Gợi ý từ khóa AutoComplete
        suggestionAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
        etSearch.setAdapter(suggestionAdapter)

        // Khi người dùng nhập từ khóa
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) performSearch(query)
            }
        })

        // Khi nhấn nút Search trên bàn phím
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
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
            updateSuggestions()
        }
        songViewModel.fetchSongs()

        // Lấy và hiển thị lịch sử tìm kiếm cũ
        loadSearchHistory()
        updateRecentSearchUI()

        // Gợi ý tag ban đầu
        addSuggestionTags(getSuggestionsFromHistory())

        // Nút xóa lịch sử tìm kiếm
        tvClearRecent.setOnClickListener {
            clearSearchHistory()
        }

        return view
    }

    // ================= Xử lý tìm kiếm =================

    private fun performSearch(query: String) {
        val results = allSongs.filter { song ->
            song.title.contains(query, true) ||
                    (song.artist.any { it.fullName.contains(query, true) })
                    song.topic.any { it.contains(query, true) }
        }

        if (results.isNotEmpty()) {
            addToRecentSearch(results.first())
        }

        suggestions.clear()
        suggestions.addAll(results.map { it.title })

        suggestionAdapter?.notifyDataSetChanged()
    }

    // ================= Gợi ý tag =================

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

                val params = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(12, 12, 12, 12)
                layoutParams = params
            }
            layoutSuggestions.addView(tv)
        }
    }

    private fun updateSuggestions() {

        // Chỉ lấy những bài hát hợp lệ từ database
        val validSongs = allSongs.filter { song ->
            song.title.isNotBlank() &&
                    song.artist != null &&
                    !song.artist.joinToString { it.fullName }.isNullOrBlank()
        }

        // Nếu không có bài hợp lệ → clear UI
        if (validSongs.isEmpty()) {
            addSuggestionTags(emptyList())
            return
        }

        // Lấy ngẫu nhiên 6 bài từ database thật
        val suggestionTitles = validSongs.shuffled().take(6).map { it.title }

        addSuggestionTags(suggestionTitles)
    }

    // ================= Lưu và lấy lịch sử =================

    private fun addToRecentSearch(song: Song) {
        if (recentSearches.none { existing ->
                existing.title == song.title &&
                        existing.artist.map { it.fullName }.joinToString() ==
                        song.artist.map { it.fullName }.joinToString()
            }) {
            recentSearches.add(0, song)
            if (recentSearches.size > 10) recentSearches.removeAt(recentSearches.lastIndex)
            recentAdapter.updateData(recentSearches)
            saveSearchHistory()
        }
    }

    private fun saveSearchHistory() {
        val prefs = requireContext().getSharedPreferences("search_history", Context.MODE_PRIVATE)
        val json = gson.toJson(recentSearches)
        prefs.edit().putString("recent_searches_json", json).apply()
    }

    private fun loadSearchHistory() {
        val prefs = requireContext().getSharedPreferences("search_history", Context.MODE_PRIVATE)
        val json = prefs.getString("recent_searches_json", null)

        if (json != null) {
            try {
                val type = object : TypeToken<MutableList<Song>>() {}.type
                recentSearches = gson.fromJson(json, type)

            } catch (e: Exception) {
                // MIGRATE dữ liệu cũ (artist = object) -> (artist = List<Artist>)
                try {
                    val type = object : TypeToken<MutableList<Song>>() {}.type
                    recentSearches = gson.fromJson(json, type)
                } catch (ex: Exception) {
                    // Nếu migrate fail → clear
                    recentSearches.clear()
                }
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

    // ================= Tiện ích =================

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
    }

    override fun onPause() {
        super.onPause()
        saveSearchHistory()
    }

    override fun onResume() {
        super.onResume()
        loadSearchHistory()
        updateRecentSearchUI()
    }
}
