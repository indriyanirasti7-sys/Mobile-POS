package com.rasti.selaraspos.kategori

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.rasti.selaraspos.RoleHelper
import com.rasti.selaraspos.databinding.ActivityModKategoriBinding
import com.rasti.selaraspos.model.ModelKategori

class ModKategoriActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModKategoriBinding
    private val db = FirebaseDatabase.getInstance().reference.child("kategori")
    private var mode = "TAMBAH"
    private var idEdit = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!RoleHelper.isAdmin(this)) {
            Toast.makeText(this, "⛔ Hanya Admin yang bisa mengelola kategori", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding = ActivityModKategoriBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = intent.getStringExtra("MODE") ?: "TAMBAH"
        binding.tvJudulModKategori.text = if (mode == "EDIT") "Edit Kategori" else "Tambah Kategori"

        if (mode == "EDIT") {
            idEdit = intent.getStringExtra("ID_KATEGORI") ?: ""
            binding.etNamaKategori.setText(intent.getStringExtra("NAMA_KATEGORI") ?: "")
        }

        binding.btnSimpanKategori.setOnClickListener { simpan() }
        binding.btnKembali.setOnClickListener { finish() }
    }

    private fun simpan() {
        val nama = binding.etNamaKategori.text.toString().trim()
        if (nama.isEmpty()) { binding.tilNamaKategori.error = "Nama wajib diisi"; return }
        binding.tilNamaKategori.error = null

        binding.progressSimpanKategori.visibility = View.VISIBLE
        binding.btnSimpanKategori.isEnabled = false

        if (mode == "TAMBAH") {
            val id = db.push().key ?: return
            db.child(id).setValue(ModelKategori(id, nama))
                .addOnSuccessListener {
                    binding.progressSimpanKategori.visibility = View.GONE
                    Toast.makeText(this, "✅ Kategori ditambahkan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    binding.progressSimpanKategori.visibility = View.GONE
                    binding.btnSimpanKategori.isEnabled = true
                    Toast.makeText(this, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.child(idEdit).child("namaKategori").setValue(nama)
                .addOnSuccessListener {
                    binding.progressSimpanKategori.visibility = View.GONE
                    Toast.makeText(this, "✅ Kategori diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    binding.progressSimpanKategori.visibility = View.GONE
                    binding.btnSimpanKategori.isEnabled = true
                    Toast.makeText(this, "Gagal memperbarui", Toast.LENGTH_SHORT).show()
                }
        }
    }
}