package com.rasti.selaraspos.kategori

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelKategori

class ModKategoriActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance("https://selaraspos-6ce12-default-rtdb.firebaseio.com/")
    private val myref = database.getReference("kategori")

    private lateinit var btnBack: ImageView
    private lateinit var tvJudul: TextView
    private lateinit var etNamaKategori: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: Button

    private var isEditMode = false
    private var currentKategoriId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_kategori)

        init()
        setupSpinner()
        checkEditMode()
        setupClickListeners()
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        tvJudul = findViewById(R.id.judul)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun setupSpinner() {
        // Jika menggunakan array dari strings.xml
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.status_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter
    }

    private fun checkEditMode() {
        // Cek apakah ada data yang dikirim dari DataKategoriActivity (mode edit)
        if (intent.hasExtra("idkategori")) {
            isEditMode = true
            currentKategoriId = intent.getStringExtra("idkategori")
            val namaKategori = intent.getStringExtra("namaKategori")
            val status = intent.getStringExtra("status")

            // Ubah judul menjadi "Edit Kategori"
            tvJudul.text = "Edit Kategori"

            // Isi data ke form
            etNamaKategori.setText(namaKategori)

            // Set spinner sesuai status
            val statusPosition = if (status == "Aktif") 0 else 1
            spinnerStatus.setSelection(statusPosition)
        } else {
            isEditMode = false
            tvJudul.text = "Tambah Kategori"
        }
    }

    private fun setupClickListeners() {
        // Tombol back
        btnBack.setOnClickListener {
            onBackPressed()  // Kembali ke halaman sebelumnya
        }

        // Tombol simpan
        btnSimpan.setOnClickListener {
            if (isEditMode) {
                updateKategori()
            } else {
                cekValidasiDanSimpan()
            }
        }
    }

    private fun cekValidasiDanSimpan() {
        val nama = etNamaKategori.text.toString().trim()
        val status = spinnerStatus.selectedItem.toString()

        if (nama.isEmpty()) {
            etNamaKategori.error = "Nama Kategori Tidak Boleh Kosong"
            etNamaKategori.requestFocus()
            return
        }

        if (status == "Pilih Status") {
            Toast.makeText(this, "Status harus dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        simpanKategoriBaru(nama, status)
    }

    private fun simpanKategoriBaru(nama: String, status: String) {
        val id = myref.push().key!!

        val kategori = ModelKategori(
            idkategori = id,
            namaKategori = nama,
            status = status
        )

        myref.child(id).setValue(kategori)
            .addOnSuccessListener {
                Toast.makeText(this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke halaman sebelumnya
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal menambahkan kategori: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateKategori() {
        val nama = etNamaKategori.text.toString().trim()
        val status = spinnerStatus.selectedItem.toString()

        if (nama.isEmpty()) {
            etNamaKategori.error = "Nama Kategori Tidak Boleh Kosong"
            etNamaKategori.requestFocus()
            return
        }

        if (currentKategoriId == null) {
            Toast.makeText(this, "Error: ID Kategori tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val kategori = ModelKategori(
            idkategori = currentKategoriId,
            namaKategori = nama,
            status = status
        )

        myref.child(currentKategoriId!!).setValue(kategori)
            .addOnSuccessListener {
                Toast.makeText(this, "Kategori berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke halaman sebelumnya
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal mengupdate kategori: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}