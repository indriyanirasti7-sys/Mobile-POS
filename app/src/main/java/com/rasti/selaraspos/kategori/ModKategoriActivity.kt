package com.rasti.selaraspos.kategori

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelKategori

class ModKategoriActivity : AppCompatActivity() {

    // Komponen UI Manual
    private lateinit var tvJudulModKategori: TextView
    private lateinit var etNamaKategori: EditText
    private lateinit var tilNamaKategori: TextInputLayout
    private lateinit var btnSimpanKategori: Button
    private lateinit var btnKembali: ImageButton
    private lateinit var progressSimpanKategori: ProgressBar

    private val db = FirebaseDatabase.getInstance().reference.child("kategori")
    private var mode = "TAMBAH"
    private var idKategoriEdit = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_kategori) // Pastikan nama layout XML Anda benar

        // Inisialisasi Manual
        tvJudulModKategori = findViewById(R.id.tvJudulModKategori)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        tilNamaKategori = findViewById(R.id.tilNamaKategori)
        btnSimpanKategori = findViewById(R.id.btnSimpanKategori)
        btnKembali = findViewById(R.id.btnKembali)
        progressSimpanKategori = findViewById(R.id.progressSimpanKategori)

        mode = intent.getStringExtra("MODE") ?: "TAMBAH"

        if (mode == "EDIT") {
            tvJudulModKategori.text = "Edit Kategori"
            idKategoriEdit = intent.getStringExtra("ID_KATEGORI") ?: ""
            etNamaKategori.setText(intent.getStringExtra("NAMA_KATEGORI") ?: "")
        } else {
            tvJudulModKategori.text = "Tambah Kategori"
        }

        btnSimpanKategori.setOnClickListener { simpanKategori() }
        btnKembali.setOnClickListener { finish() }
    }

    private fun simpanKategori() {
        val nama = etNamaKategori.text.toString().trim()

        if (nama.isEmpty()) {
            tilNamaKategori.error = "Nama kategori wajib diisi"
            return
        }
        tilNamaKategori.error = null

        progressSimpanKategori.visibility = View.VISIBLE
        btnSimpanKategori.isEnabled = false

        if (mode == "TAMBAH") {
            val id = db.push().key ?: return
            val kategori = ModelKategori(id, nama)
            db.child(id).setValue(kategori)
                .addOnSuccessListener {
                    progressSimpanKategori.visibility = View.GONE
                    Toast.makeText(this, "Kategori berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    progressSimpanKategori.visibility = View.GONE
                    btnSimpanKategori.isEnabled = true
                    Toast.makeText(this, "Gagal menyimpan kategori", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.child(idKategoriEdit).child("namaKategori").setValue(nama)
                .addOnSuccessListener {
                    progressSimpanKategori.visibility = View.GONE
                    Toast.makeText(this, "Kategori berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    progressSimpanKategori.visibility = View.GONE
                    btnSimpanKategori.isEnabled = true
                    Toast.makeText(this, "Gagal memperbarui kategori", Toast.LENGTH_SHORT).show()
                }
        }
    }
}