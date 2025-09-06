package com.example.musicapp.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.models.Song
import com.example.musicapp.ui.home.SongAdapter
import com.example.musicapp.ui.home.SongViewModel

class SearchFragment : Fragment() {

    private lateinit var etSearch: AutoCompleteTextView
    private lateinit var spinnerFilter: Spinner
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var searchAdapter: SongAdapter

    private lateinit var songViewModel: SongViewModel
    private lateinit var tvNoResult: TextView

    private var allSongs: List<Song> = emptyList()
    private var suggestions = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        etSearch = view.findViewById(R.id.etSearch)
        spinnerFilter = view.findViewById(R.id.spinnerFilter)
        rvSearchResults = view.findViewById(R.id.rvSearchResults)
        tvNoResult = view.findViewById(R.id.tvNoResult)

        // Setup RecyclerView
        rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        searchAdapter = SongAdapter(emptyList())
        rvSearchResults.adapter = searchAdapter

        // Setup spinner lọc
        val filters = listOf("Tất cả", "Thể loại", "Nghệ sĩ")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filters
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = spinnerAdapter

        // Gợi ý từ khóa
        val suggestionAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)
        etSearch.setAdapter(suggestionAdapter)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    val results = searchSongs(query)
                    searchAdapter.updateData(results)

                    // cập nhật gợi ý
                    suggestions.clear()
                    suggestions.addAll(results.map { it.title })
                    suggestionAdapter.notifyDataSetChanged()

                    tvNoResult.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    searchAdapter.updateData(emptyList())
                    tvNoResult.visibility = View.GONE
                }
            }
        })

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    val results = searchSongs(query)
                    searchAdapter.updateData(results)

                    if (results.isEmpty()) {
                        Toast.makeText(requireContext(), "Không có kết quả tìm kiếm", Toast.LENGTH_SHORT).show()
                    }
                }

                // Ẩn bàn phím sau khi bấm search
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(etSearch.windowToken, 0)

                true // báo là đã xử lý action
            } else {
                false
            }
        }

        // ViewModel
        songViewModel = ViewModelProvider(this)[SongViewModel::class.java]
        songViewModel.songs.observe(viewLifecycleOwner) { songs ->
            allSongs = songs
            searchAdapter.updateData(allSongs)
        }

        // Load dữ liệu từ API
        songViewModel.fetchSongs()

        // Xử lý tìm kiếm
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    val results = searchSongs(query)
                    searchAdapter.updateData(results)

                    if (results.isEmpty()) {
                        tvNoResult.visibility = View.VISIBLE
                        rvSearchResults.visibility = View.GONE
                    } else {
                        tvNoResult.visibility = View.GONE
                        rvSearchResults.visibility = View.VISIBLE
                    }
                } else {
                    // khi ô tìm kiếm trống
                    searchAdapter.updateData(emptyList())
                    tvNoResult.visibility = View.GONE
                    rvSearchResults.visibility = View.VISIBLE
                }
            }
        })

        return view
    }

    private fun searchSongs(query: String): List<Song> {
        val filter = spinnerFilter.selectedItem?.toString() ?: "Tất cả"
        return allSongs.filter { song ->
            when (filter) {
                "Tất cả" -> song.title.contains(query, true)
                        || song.artist.fullName.contains(query, true)
                        || song.album.contains(query, true)
                "Thể loại" -> song.topic.any { it.contains(query, true) }
                "Nghệ sĩ" -> song.artist.fullName.contains(query, true)
                else -> false
            }
        }
    }
}
