package com.rasti.selaraspos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.databinding.ActivityLoginBinding

/**
 * LoginActivity
 * - Login menggunakan Firebase Authentication (Email + Password)
 * - Setelah login, ambil data role dari node "pegawai/{uid}/role"
 * - Simpan role ke SharedPreferences agar bisa diakses di seluruh app
 * - Firebase Realtime Database GRATIS: 1 GB storage, 10 GB/bulan download
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { prosesLogin() }

        // Tombol daftar akun baru (untuk setup awal admin)
        binding.tvDaftarAkun.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun prosesLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // ── Validasi input ──────────────────────────────────────────────
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email tidak boleh kosong"
            return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            return
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password minimal 6 karakter"
            return
        }

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // ── Tampilkan loading ───────────────────────────────────────────
        binding.progressLogin.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        // ── Proses login ke Firebase Auth ──────────────────────────────
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: run {
                    selesaiLoading()
                    Toast.makeText(this, "Gagal mendapatkan UID", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Ambil role dari Firebase Database
                db.child("pegawai").child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            selesaiLoading()
                            val role = snapshot.child("role").getValue(String::class.java) ?: "kasir"
                            val nama = snapshot.child("namaPegawai").getValue(String::class.java) ?: "Pengguna"

                            // Simpan role & nama ke SharedPreferences
                            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                            prefs.edit()
                                .putString("role", role)
                                .putString("nama", nama)
                                .putString("uid", uid)
                                .apply()

                            Toast.makeText(
                                this@LoginActivity,
                                "Selamat datang, $nama!",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(Intent(this@LoginActivity, Halaman_Utama::class.java))
                            finish()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            selesaiLoading()
                            // Tetap masuk meski gagal ambil role, default kasir
                            startActivity(Intent(this@LoginActivity, Halaman_Utama::class.java))
                            finish()
                        }
                    })
            }
            .addOnFailureListener { e ->
                selesaiLoading()
                val pesan = when {
                    e.message?.contains("password") == true -> "Password salah"
                    e.message?.contains("no user") == true -> "Email tidak terdaftar"
                    e.message?.contains("network") == true -> "Tidak ada koneksi internet"
                    else -> "Login gagal: ${e.message}"
                }
                Toast.makeText(this, pesan, Toast.LENGTH_LONG).show()
            }
    }

    private fun selesaiLoading() {
        binding.progressLogin.visibility = View.GONE
        binding.btnLogin.isEnabled = true
    }
}