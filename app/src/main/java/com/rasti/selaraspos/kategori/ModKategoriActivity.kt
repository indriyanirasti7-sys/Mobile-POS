package com.rasti.selaraspos.kategori

import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelKategori


class ModKategoriActivity : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance("https://selaraspos-6ce12-default-rtdb.firebaseio.com/")
    val myref = database.getReference("kategori")

    private lateinit var tvJudulToolbar: TextView
    private lateinit var tvJudul: TextView
    private lateinit var etNamaKategori: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_kategori)

        init()

        btnSimpan.setOnClickListener {
            cekValidasi()
        }
    }

    fun init(){
        tvJudulToolbar = findViewById(R.id.beranda_kategori)
        tvJudul = findViewById(R.id.judul)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun cekValidasi() {

        val nama = etNamaKategori.text.toString()
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

        simpan()
    }

    private fun simpan() {

        val id = myref.push().key!!

        val kategori = ModelKategori(
            idkategori = id,
            namaKategori = etNamaKategori.text.toString().trim(),
            status = spinnerStatus.selectedItem.toString().trim()
        )

        myref.child(id).setValue(kategori)
            .addOnSuccessListener {
                Toast.makeText(this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                etNamaKategori.setText("")
                spinnerStatus.setSelection(0)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menambahkan kategori", Toast.LENGTH_SHORT).show()
            }
    }
}