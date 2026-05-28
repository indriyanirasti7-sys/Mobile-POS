package com.rasti.selaraspos.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.LoginActivity
import com.rasti.selaraspos.R
import com.rasti.selaraspos.databinding.ActivityAkunBinding

class AkunActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAkunBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAkunBinding.inflate(layoutInflater)
        setContentView(binding.root)

        muatDataAkun()

        binding.btnEditProfil.setOnClickListener { dialogEditProfil() }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar?")
                .setPositiveButton("Logout") { _, _ ->
                    getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply()
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        binding.btnKembali.setOnClickListener { finish() }
    }

    private fun muatDataAkun() {
        val uid = auth.currentUser?.uid ?: return
        binding.progressAkun.visibility = View.VISIBLE

        db.child("pegawai").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressAkun.visibility = View.GONE

                    val nama = snapshot.child("namaPegawai").getValue(String::class.java) ?: "Pengguna"
                    val role = snapshot.child("role").getValue(String::class.java) ?: "kasir"
                    val email = auth.currentUser?.email ?: ""
                    val noHp = snapshot.child("noHp").getValue(String::class.java) ?: "-"
                    val fotoUrl = snapshot.child("fotoPegawai").getValue(String::class.java) ?: ""

                    // Ambil username dari email (sebelum @)
                    val username = email.substringBefore("@")

                    // Set data ke layout
                    binding.tvNamaAkun.text = nama
                    binding.tvRoleAkun.text = if (role == "admin") "Admin" else "Kasir"
                    binding.tvEmailAkun.text = email
                    binding.tvNoHpAkun.text = noHp
                    binding.tvUsername.text = username
                    binding.tvNamaToko.text = "Selaras POS"

                    // 🔥 LOAD AVATAR (tanpa ic_avatar_default) 🔥
                    if (fotoUrl.isNotEmpty()) {
                        Glide.with(this@AkunActivity)
                            .load(fotoUrl)
                            .circleCrop()
                            .into(binding.imgAvatar)
                    } else {
                        // Jika tidak ada foto, tampilkan inisial nama
                        binding.imgAvatar.setImageResource(R.drawable.bg_avatar)
                        // Atau set background warna
                        binding.imgAvatar.setBackgroundColor(getColor(R.color.primary))
                    }

                    // Badge role dengan Chip
                    if (role == "admin") {
                        binding.chipRole.text = "👑 Admin"
                        binding.chipRole.setChipBackgroundColorResource(R.color.primary)
                    } else {
                        binding.chipRole.text = "💼 Kasir"
                        binding.chipRole.setChipBackgroundColorResource(R.color.text_secondary)
                    }

                    // Menu admin hanya untuk admin
                    val adminVisible = if (role == "admin") View.VISIBLE else View.GONE
                    binding.layoutMenuAdmin.visibility = adminVisible
                    binding.tvLabelMenuAdmin.visibility = adminVisible
                }

                override fun onCancelled(e: DatabaseError) {
                    binding.progressAkun.visibility = View.GONE
                    Toast.makeText(this@AkunActivity, "Gagal memuat数据: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun dialogEditProfil() {
        val uid = auth.currentUser?.uid ?: return

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profil, null)
        val etNama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNamaEdit)
        val etNoHp = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNoHpEdit)

        etNama.setText(binding.tvNamaAkun.text.toString())
        etNoHp.setText(binding.tvNoHpAkun.text.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit Profil")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val namaBaru = etNama.text.toString().trim()
                val noHpBaru = etNoHp.text.toString().trim()

                if (namaBaru.isEmpty()) {
                    Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                binding.progressAkun.visibility = View.VISIBLE

                db.child("pegawai").child(uid).child("namaPegawai").setValue(namaBaru)
                    .addOnSuccessListener {
                        db.child("pegawai").child(uid).child("noHp").setValue(noHpBaru)
                            .addOnSuccessListener {
                                binding.progressAkun.visibility = View.GONE
                                getSharedPreferences("user_prefs", MODE_PRIVATE).edit()
                                    .putString("nama", namaBaru).apply()
                                Toast.makeText(this, "✅ Profil diperbarui!", Toast.LENGTH_SHORT).show()
                                muatDataAkun()
                            }
                    }
                    .addOnFailureListener {
                        binding.progressAkun.visibility = View.GONE
                        Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}