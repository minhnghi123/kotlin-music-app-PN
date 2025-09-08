package com.example.musicapp.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.musicapp.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Gắn layout fragment_settings.xml
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bắt sự kiện click
        val layoutInfo = view.findViewById<LinearLayout>(R.id.layout_info)
        layoutInfo.setOnClickListener {
            Toast.makeText(requireContext(), "PN Music v1.0 - Ứng dụng nghe nhạc", Toast.LENGTH_SHORT).show()
        }
    }
}
