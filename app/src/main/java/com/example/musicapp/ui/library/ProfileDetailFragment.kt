package com.example.musicapp.ui.library

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.users.ChangePasswordRequest
import com.example.musicapp.models.users.UpdateMeRequest
import com.example.musicapp.models.users.UserResponse
import com.example.musicapp.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileDetailFragment : Fragment() {

    private lateinit var ivAvatar: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPassword: TextView
    private lateinit var switchBiometric: Switch
    private lateinit var btnEditProfile: Button
    private lateinit var btnChangePassword: Button

//Avatar Picker + Preview
    private var selectedAvatarUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAvatarUri = uri
            // Cập nhật preview cho ImageView trong dialog (nếu đang mở)
            currentAvatarPreview?.setImageURI(uri)
        }
    }

    private var currentAvatarPreview: ImageView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_detail, container, false)

        // Ánh xạ view
        ivAvatar = view.findViewById(R.id.ivDetailAvatar)
        tvUsername = view.findViewById(R.id.tvDetailUsername)
        tvEmail = view.findViewById(R.id.tvDetailEmail)
        tvPassword = view.findViewById(R.id.tvDetailPassword)
        switchBiometric = view.findViewById(R.id.switchBiometric)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        // Gọi API để lấy dữ liệu user
        fetchUserProfile()

        // Xử lý khi bấm nút "Chỉnh sửa"
        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        return view
    }
//show pop upp chinh sua
private fun showEditProfileDialog() {
    val dialogView = LayoutInflater.from(requireContext())
        .inflate(R.layout.dialog_edit_profile, null)

    val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
    val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
    val etAvatar = dialogView.findViewById<EditText>(R.id.etAvatar)
    val ivPreview = dialogView.findViewById<ImageView>(R.id.ivAvatarPreview)

    // Lưu tham chiếu để update khi chọn ảnh
    currentAvatarPreview = ivPreview

    // Gán dữ liệu hiện tại
    etUsername.setText(tvUsername.text.toString())
    etEmail.setText(tvEmail.text.toString())

    // Load avatar hiện tại từ server
    Glide.with(this).load((ivAvatar.drawable)).into(ivPreview)

    // Khi click vào ảnh → mở thư viện
    ivPreview.setOnClickListener {
        pickImageLauncher.launch("image/*")
    }

    AlertDialog.Builder(requireContext())
        .setTitle("Chỉnh sửa thông tin")
        .setView(dialogView)
        .setPositiveButton("Lưu") { _, _ ->
            val newUsername = etUsername.text.toString().trim()
            val newEmail = etEmail.text.toString().trim()

            // Nếu user chọn ảnh mới → dùng URI, ngược lại dùng link nhập tay
            val newAvatar = if (selectedAvatarUri != null) {
                selectedAvatarUri.toString()
            } else {
                etAvatar.text.toString().trim()
            }

            updateUserProfile(newUsername, newEmail, newAvatar)
        }
        .setNegativeButton("Hủy", null)
        .show()
}

    //    Fetch Data
    private fun fetchUserProfile() {
        ApiClient.api.getUserProfile().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!.data

                    // Bind data vào UI
                    tvUsername.text = user.username
                    tvEmail.text = user.email
                    tvPassword.text = "********" // luôn ẩn password
                    Glide.with(requireContext()).load(user.avatar).into(ivAvatar)

                    // giả sử server trả về có cờ 2FA thì set checked
                    switchBiometric.isChecked = false
                } else {
                    Toast.makeText(requireContext(), "Lỗi load dữ liệu", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // Update Data
    private fun updateUserProfile(username: String?, email: String?, avatar: String?) {
        val request = UpdateMeRequest(username, email, avatar)

        ApiClient.api.updateMe(request).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()!!.data
                    Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                    tvUsername.text = user.username
                    tvEmail.text = user.email
                    Glide.with(requireContext()).load(user.avatar).into(ivAvatar)
                } else {
                    Toast.makeText(requireContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Change Password
    private fun changePassword(oldPass: String, newPass: String, reNewPass: String) {
        val request = ChangePasswordRequest(oldPass, newPass, reNewPass)

        ApiClient.api.changePassword(request).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "API lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
