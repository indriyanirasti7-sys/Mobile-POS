package com.rasti.selaraspos

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.rasti.selaraspos.databinding.ActivityRegisterBinding
import com.rasti.selaraspos.model.ModelPegawai

/**
 * RegisterActivity
 * Mendaftarkan akun baru ke Firebase Auth + menyimpan data ke Firebase Database
 * Gunakan halaman ini untuk membuat akun admin pertama kali
 *
 * CATATAN FIREBASE GRATIS (Spark Plan):
 * ✅ Authentication: Gratis tanpa batas
 * ✅ Realtime Database: 1 GB data, 10 GB/bulan bandwidth
 * ✅ Tidak perlu kartu kredit
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dropdown role
        val roles = listOf("admin", "kasir")
        val adapterRole = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.actvRoleRegister.setAdapter(adapterRole)
        binding.actvRoleRegister.setText("kasir", false)

        binding.btnDaftar.setOnClickListener { prosesDaftar() }
        binding.btnKembaliLogin.setOnClickListener { finish() }
    }

    private fun prosesDaftar() {
        val nama = binding.etNamaRegister.text.toString().trim()
        val email = binding.etEmailRegister.text.toString().trim()
        val password = binding.etPasswordRegister.text.toString().trim()
        val konfirmasi = binding.etKonfirmasiPassword.text.toString().trim()
        val role = binding.actvRoleRegister.text.toString().trim()
        val noHp = binding.etNoHpRegister.text.toString().trim()

        // Validasi
        if (nama.isEmpty()) { binding.tilNamaRegister.error = "Nama wajib diisi"; return }
        if (email.isEmpty()) { binding.tilEmailRegister.error = "Email wajib diisi"; return }
        if (password.length < 6) { binding.tilPasswordRegister.error = "Password minimal 6 karakter"; return }
        if (password != konfirmasi) { binding.tilKonfirmasiPassword.error = "Password tidak cocok"; return }
        if (role.isEmpty()) { binding.tilRoleRegister.error = "Role wajib dipilih"; return }

        // Reset error
        binding.tilNamaRegister.error = null
        binding.tilEmailRegister.error = null
        binding.tilPasswordRegister.error = null
        binding.tilKonfirmasiPassword.error = null

        binding.progressDaftar.visibility = View.VISIBLE
        binding.btnDaftar.isEnabled = false

        // Buat akun di Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                // Simpan data pegawai ke Firebase Realtime Database
                val pegawai = ModelPegawai(
                    idPegawai = uid,
                    namaPegawai = nama,
                    role = role,
                    noHp = noHp,
                    alamat = "",
                    email = email
                )
                db.child("pegawai").child(uid).setValue(pegawai)
                    .addOnSuccessListener {
                        binding.progressDaftar.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "Akun berhasil dibuat! Silakan login.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                    .addOnFailureListener {
                        binding.progressDaftar.visibility = View.GONE
                        binding.btnDaftar.isEnabled = true
                        Toast.makeText(this, "Gagal simpan data", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                binding.progressDaftar.visibility = View.GONE
                binding.btnDaftar.isEnabled = true
                val pesan = when {
                    e.message?.contains("already") == true -> "Email sudah digunakan"
                    e.message?.contains("email") == true -> "Format email tidak valid"
                    else -> "Gagal daftar: ${e.message}"
                }
                Toast.makeText(this, pesan, Toast.LENGTH_LONG).show()
            }
    }
}