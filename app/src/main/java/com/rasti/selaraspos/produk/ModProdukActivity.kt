package com.rasti.selaraspos.produk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelProduk

class ModProdukActivity : AppCompatActivity() {

    private val database =
        FirebaseDatabase.getInstance(
            "https://selaraspos-6ce12-default-rtdb.firebaseio.com/"
        )

    private val myRef =
        database.getReference("produk")

    private lateinit var btnBack: ImageView
    private lateinit var btnKamera: ImageView
    private lateinit var btnGaleri: ImageView
    private lateinit var etNamaProduk: EditText
    private lateinit var etSkuProduk: EditText
    private lateinit var etBarcode: EditText
    private lateinit var etKategori: EditText
    private lateinit var etCabang: EditText
    private lateinit var etHargaBeli: EditText
    private lateinit var etProfit: EditText
    private lateinit var etHargaJual: EditText
    private lateinit var etStok: EditText
    private lateinit var cbUnlimited: CheckBox
    private lateinit var btnSimpan: Button

    // Untuk hasil gambar dari kamera/galeri
    private var selectedImageUri: Uri? = null

    // Register result untuk galeri
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Toast.makeText(this, "Gambar dipilih", Toast.LENGTH_SHORT).show()
            // TODO: Tampilkan preview gambar jika ada ImageView preview
        }
    }

    // Register result untuk kamera
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            Toast.makeText(this, "Foto diambil", Toast.LENGTH_SHORT).show()
            // TODO: Simpan bitmap dan tampilkan preview
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_produk)

        init()
        setupClick()
        setupAutoCalculation()
    }

    private fun init() {
        btnBack = findViewById(R.id.btnBack)
        btnKamera = findViewById(R.id.btnKamera)
        btnGaleri = findViewById(R.id.btnGaleri)
        etNamaProduk = findViewById(R.id.etNamaProduk)
        etSkuProduk = findViewById(R.id.etSkuProduk)
        etBarcode = findViewById(R.id.etBarcode)
        etKategori = findViewById(R.id.btnPilihKategori)
        etCabang = findViewById(R.id.btnPilihCabang)
        etHargaBeli = findViewById(R.id.etHargaBeli)
        etProfit = findViewById(R.id.etNilaiProfit)
        etHargaJual = findViewById(R.id.etHargaJual)
        etStok = findViewById(R.id.etStokProduk)
        cbUnlimited = findViewById(R.id.cbStokTakTerbatas)
        btnSimpan = findViewById(R.id.btnSimpan)
    }

    private fun setupClick() {
        btnBack.setOnClickListener {
            finish()
        }

        btnKamera.setOnClickListener {
            cameraLauncher.launch(null)
        }

        btnGaleri.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnSimpan.setOnClickListener {
            if (validateInput()) {
                simpanProduk()
            }
        }

        // Stok tak terbatas
        cbUnlimited.setOnCheckedChangeListener { _, isChecked ->
            etStok.isEnabled = !isChecked
            if (isChecked) {
                etStok.setText("")
            }
        }
    }

    private fun setupAutoCalculation() {
        // Hitung harga jual otomatis saat harga beli atau profit berubah
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hitungHargaJual()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }

        etHargaBeli.addTextChangedListener(textWatcher)
        etProfit.addTextChangedListener(textWatcher)
    }

    private fun hitungHargaJual() {
        val hargaBeli = etHargaBeli.text.toString().toIntOrNull() ?: 0
        val profit = etProfit.text.toString().toIntOrNull() ?: 0
        val hargaJual = hargaBeli + profit

        etHargaJual.setText(hargaJual.toString())
    }

    private fun validateInput(): Boolean {
        if (etNamaProduk.text.isNullOrEmpty()) {
            etNamaProduk.error = "Nama produk harus diisi"
            etNamaProduk.requestFocus()
            return false
        }

        if (etKategori.text.isNullOrEmpty()) {
            Toast.makeText(this, "Pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show()
            return false
        }

        if (etCabang.text.isNullOrEmpty()) {
            Toast.makeText(this, "Pilih cabang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!cbUnlimited.isChecked && etStok.text.isNullOrEmpty()) {
            etStok.error = "Stok harus diisi"
            etStok.requestFocus()
            return false
        }

        return true
    }

    private fun simpanProduk() {
        // Nonaktifkan tombol simpan sementara
        btnSimpan.isEnabled = false
        btnSimpan.text = "Menyimpan..."

        val id = myRef.push().key ?: return

        val produk = ModelProduk(
            idProduk = id,
            namaProduk = etNamaProduk.text.toString(),
            skuProduk = etSkuProduk.text.toString(),
            barcodeProduk = etBarcode.text.toString(),
            kategoriProduk = etKategori.text.toString(),
            cabangProduk = etCabang.text.toString(),
            hargaBeli = etHargaBeli.text.toString().toIntOrNull() ?: 0,
            nilaiProfit = etProfit.text.toString().toIntOrNull() ?: 0,
            hargaJual = etHargaJual.text.toString().toIntOrNull() ?: 0,
            stokProduk = if (cbUnlimited.isChecked) 0 else etStok.text.toString().toIntOrNull() ?: 0,
            stokTakTerbatas = cbUnlimited.isChecked
        )

        myRef.child(id).setValue(produk)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Produk berhasil disimpan",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener { error ->
                btnSimpan.isEnabled = true
                btnSimpan.text = "Simpan"
                Toast.makeText(
                    this,
                    "Gagal menyimpan: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}