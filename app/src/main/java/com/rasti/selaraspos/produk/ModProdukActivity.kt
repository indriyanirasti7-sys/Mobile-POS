package com.rasti.selaraspos.produk

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelProduk
import java.net.URL
import kotlin.concurrent.thread

class ModProdukActivity : AppCompatActivity() {

    private lateinit var etNamaProduk: EditText
    private lateinit var etHargaJual: EditText
    private lateinit var etStokProduk: EditText
    private lateinit var etFotoProduk: EditText
    private lateinit var actvKategori: AutoCompleteTextView
    private lateinit var btnSimpanProduk: Button
    private lateinit var btnKembali: ImageButton
    private lateinit var imgPreviewProduk: ImageView
    private lateinit var progressSimpan: ProgressBar
    private lateinit var tilNamaProduk: TextInputLayout
    private lateinit var tilHargaJual: TextInputLayout
    private lateinit var tilStokProduk: TextInputLayout
    private lateinit var tilKategori: TextInputLayout
    private lateinit var tvJudulMod: TextView

    private val db = FirebaseDatabase.getInstance().reference
    private var mode = "TAMBAH"
    private var idProdukEdit = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_produk)

        // Inisialisasi Manual
        etNamaProduk = findViewById(R.id.etNamaProduk)
        etHargaJual = findViewById(R.id.etHargaJual)
        etStokProduk = findViewById(R.id.etStokProduk)
        etFotoProduk = findViewById(R.id.etFotoProduk)
        actvKategori = findViewById(R.id.actvKategori)
        btnSimpanProduk = findViewById(R.id.btnSimpanProduk)
        btnKembali = findViewById(R.id.btnKembali)
        imgPreviewProduk = findViewById(R.id.imgPreviewProduk)
        progressSimpan = findViewById(R.id.progressSimpan)
        tilNamaProduk = findViewById(R.id.tilNamaProduk)
        tilHargaJual = findViewById(R.id.tilHargaJual)
        tilStokProduk = findViewById(R.id.tilStokProduk)
        tilKategori = findViewById(R.id.tilKategori)
        tvJudulMod = findViewById(R.id.tvJudulMod)

        mode = intent.getStringExtra("MODE") ?: "TAMBAH"

        aturPreviewManual()

        if (mode == "EDIT") {
            tvJudulMod.text = "Edit Produk"
            // (Logika isiDataEdit menggunakan findViewById seperti di atas)
        }

        btnSimpanProduk.setOnClickListener { simpanProduk() }
        btnKembali.setOnClickListener { finish() }
    }

    private fun aturPreviewManual() {
        etFotoProduk.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val urlString = s.toString().trim()
                if (urlString.isNotEmpty()) {
                    imgPreviewProduk.visibility = View.VISIBLE
                    // Download manual di thread terpisah agar tidak freeze aplikasi
                    thread {
                        try {
                            val url = URL(urlString)
                            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                            runOnUiThread { imgPreviewProduk.setImageBitmap(bmp) }
                        } catch (e: Exception) {
                            runOnUiThread { imgPreviewProduk.setImageResource(R.drawable.placeholder) }
                        }
                    }
                } else {
                    imgPreviewProduk.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun simpanProduk() {
        // Logika simpan produk sama seperti sebelumnya,
        // cukup ganti 'binding.xxx' menjadi variabel manual (misal: 'etNamaProduk.text')
        val nama = etNamaProduk.text.toString().trim()
        // ... (sisanya sesuaikan sendiri)
    }
}