package com.rasti.selaraspos.pelanggan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.rasti.selaraspos.databinding.ActivityModPelangganBinding
import com.rasti.selaraspos.model.ModelPelanggan

class ModPelangganActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModPelangganBinding
    private val db = FirebaseDatabase.getInstance().reference.child("pelanggan")
    private var mode = "TAMBAH"
    private var idEdit = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModPelangganBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = intent.getStringExtra("MODE") ?: "TAMBAH"
        binding.tvJudulModPelanggan.text = if (mode == "EDIT") "Edit Pelanggan" else "Tambah Pelanggan"

        if (mode == "EDIT") {
            idEdit = intent.getStringExtra("ID_PELANGGAN") ?: ""
            binding.etNamaPelanggan.setText(intent.getStringExtra("NAMA_PELANGGAN") ?: "")
            binding.etTeleponPelanggan.setText(intent.getStringExtra("TELEPON_PELANGGAN") ?: "")
            binding.etAlamatPelanggan.setText(intent.getStringExtra("ALAMAT_PELANGGAN") ?: "")
            binding.etEmailPelanggan.setText(intent.getStringExtra("EMAIL_PELANGGAN") ?: "")
        }

        binding.btnSimpanPelanggan.setOnClickListener { simpan() }
        binding.btnKembali.setOnClickListener { finish() }
    }

    private fun simpan() {
        val nama = binding.etNamaPelanggan.text.toString().trim()
        val telepon = binding.etTeleponPelanggan.text.toString().trim()
        val alamat = binding.etAlamatPelanggan.text.toString().trim()
        val email = binding.etEmailPelanggan.text.toString().trim()

        if (nama.isEmpty()) {
            binding.tilNamaPelanggan.error = "Nama wajib diisi"
            return
        }

        binding.tilNamaPelanggan.error = null
        binding.progressSimpanPelanggan.visibility = View.VISIBLE
        binding.btnSimpanPelanggan.isEnabled = false

        if (mode == "TAMBAH") {
            val id = db.push().key ?: return
            val pelanggan = ModelPelanggan(
                idPelanggan = id,
                namaPelanggan = nama,
                teleponPelanggan = telepon,
                alamatPelanggan = alamat,
                emailPelanggan = email,
                point = 0
            )
            db.child(id).setValue(pelanggan)
                .addOnSuccessListener {
                    binding.progressSimpanPelanggan.visibility = View.GONE
                    Toast.makeText(this, "✅ Pelanggan ditambahkan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    binding.progressSimpanPelanggan.visibility = View.GONE
                    binding.btnSimpanPelanggan.isEnabled = true
                    Toast.makeText(this, "❌ Gagal menambahkan", Toast.LENGTH_SHORT).show()
                }
        } else {
            val updates = mapOf(
                "namaPelanggan" to nama,
                "teleponPelanggan" to telepon,
                "alamatPelanggan" to alamat,
                "emailPelanggan" to email
            )
            db.child(idEdit).updateChildren(updates)
                .addOnSuccessListener {
                    binding.progressSimpanPelanggan.visibility = View.GONE
                    Toast.makeText(this, "✅ Pelanggan diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    binding.progressSimpanPelanggan.visibility = View.GONE
                    binding.btnSimpanPelanggan.isEnabled = true
                    Toast.makeText(this, "❌ Gagal memperbarui", Toast.LENGTH_SHORT).show()
                }
        }
    }
}