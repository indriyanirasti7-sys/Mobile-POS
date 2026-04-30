package com.rasti.selaraspos.produk

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelProduk

class ModProdukActivity : AppCompatActivity() {

    // Sesuaikan URL Firebase jika berbeda, atau gunakan FirebaseDatabase.getInstance() saja jika google-services.json sudah benar
    val database = FirebaseDatabase.getInstance("https://selaraspos-6ce12-default-rtdb.firebaseio.com/")
    val myref = database.getReference("produk")

    private lateinit var tvJudulToolbar: TextView
    private lateinit var tvJudul: TextView
    private lateinit var etNamaProduk: EditText
    private lateinit var etHargaProduk: EditText // Tambahan untuk produk
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_produk) // Pastikan file XML ini ada

        init()

        btnSimpan.setOnClickListener {
            cekValidasi()
        }
    }

    fun init() {
        // Pastikan ID di bawah ini sesuai dengan yang ada di activity_mod_produk.xml
        tvJudulToolbar = findViewById(R.id.beranda_produk)
        tvJudul = findViewById(R.id.judul_produk)
        etNamaProduk = findViewById(R.id.etNamaProduk)
        etHargaProduk = findViewById(R.id.etHargaProduk)
        spinnerStatus = findViewById(R.id.spinnerStatusProduk)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun cekValidasi() {
        val nama = etNamaProduk.text.toString()
        val harga = etHargaProduk.text.toString()
        val status = spinnerStatus.selectedItem.toString()

        if (nama.isEmpty()) {
            etNamaProduk.error = "Nama Produk Tidak Boleh Kosong"
            etNamaProduk.requestFocus()
            return
        }

        if (harga.isEmpty()) {
            etHargaProduk.error = "Harga Tidak Boleh Kosong"
            etHargaProduk.requestFocus()
            return
        }

        if (status == "Pilih Status" || status == "") {
            Toast.makeText(this, "Status harus dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        simpan()
    }

    private fun simpan() {
        // Generate ID unik dari Firebase
        val id = myref.push().key ?: return

        // Sesuaikan parameter ModelProduk dengan constructor yang kamu buat
        val produk = ModelProduk(
            idProduk = id,
            namaProduk = etNamaProduk.text.toString().trim(),
            hargaProduk = etHargaProduk.text.toString().trim().toIntOrNull() ?: 0, // Pakai toIntOrNull agar tidak crash jika kosong
            statusProduk = spinnerStatus.selectedItem.toString().trim()
        )

        myref.child(id).setValue(produk)
            .addOnSuccessListener {
                Toast.makeText(this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                // Reset form
                etNamaProduk.setText("")
                etHargaProduk.setText("")
                spinnerStatus.setSelection(0)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menambahkan produk: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}