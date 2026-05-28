package com.rasti.selaraspos.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.LoginActivity
import com.rasti.selaraspos.R
import com.rasti.selaraspos.RoleHelper
import com.rasti.selaraspos.adapters.AdapterLaporan
import com.rasti.selaraspos.databinding.ActivityAkunBinding
import com.rasti.selaraspos.databinding.ActivityLaporanBinding
import com.rasti.selaraspos.model.ModelLaporan
import com.rasti.selaraspos.model.ModelTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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
                    // Hapus data lokal
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

                    binding.tvNamaAkun.text = nama
                    binding.tvEmailAkun.text = email
                    binding.tvRoleAkun.text = role.replaceFirstChar { it.uppercase() }

                    // Badge role
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
                }
            })
    }

    private fun dialogEditProfil() {
        val uid = auth.currentUser?.uid ?: return
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profil, null)
        val etNama = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNamaEdit)
        etNama.setText(binding.tvNamaAkun.text.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit Nama")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val namaBaru = etNama.text.toString().trim()
                if (namaBaru.isEmpty()) { Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show(); return@setPositiveButton }

                db.child("pegawai").child(uid).child("namaPegawai").setValue(namaBaru)
                    .addOnSuccessListener {
                        // Perbarui juga SharedPreferences
                        getSharedPreferences("user_prefs", MODE_PRIVATE).edit()
                            .putString("nama", namaBaru).apply()
                        Toast.makeText(this, "✅ Nama diperbarui!", Toast.LENGTH_SHORT).show()
                        muatDataAkun()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}