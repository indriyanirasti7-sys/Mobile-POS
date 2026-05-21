package com.rasti.selaraspos.produk

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
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

    // DITAMBAHKAN
    private lateinit var btnKamera: ImageView

    // DITAMBAHKAN
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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_mod_produk
        )

        init()

        setupClick()
    }

    private fun init() {

        btnBack =
            findViewById(R.id.btnBack)

        btnKamera =
            findViewById(R.id.btnKamera)

        btnGaleri =
            findViewById(R.id.btnGaleri)

        etNamaProduk =
            findViewById(R.id.etNamaProduk)

        etSkuProduk =
            findViewById(R.id.etSkuProduk)

        etBarcode =
            findViewById(R.id.etBarcode)

        etKategori =
            findViewById(R.id.btnPilihKategori)

        etCabang =
            findViewById(R.id.btnPilihCabang)

        etHargaBeli =
            findViewById(R.id.etHargaBeli)

        etProfit =
            findViewById(R.id.etNilaiProfit)

        etHargaJual =
            findViewById(R.id.etHargaJual)

        etStok =
            findViewById(R.id.etStokProduk)

        cbUnlimited =
            findViewById(R.id.cbStokTakTerbatas)

        btnSimpan =
            findViewById(R.id.btnSimpan)
    }

    private fun setupClick() {

        btnBack.setOnClickListener {

            finish()
        }

        // DITAMBAHKAN
        btnKamera.setOnClickListener {

            val intent =
                Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            startActivity(intent)
        }

        // DITAMBAHKAN
        btnGaleri.setOnClickListener {

            val intent =
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )

            startActivity(intent)
        }

        btnSimpan.setOnClickListener {

            simpanProduk()
        }
    }

    private fun simpanProduk() {

        val id =
            myRef.push().key ?: return

        val produk = ModelProduk(

            idProduk = id,

            namaProduk =
                etNamaProduk.text.toString(),

            skuProduk =
                etSkuProduk.text.toString(),

            barcodeProduk =
                etBarcode.text.toString(),

            kategoriProduk =
                etKategori.text.toString(),

            cabangProduk =
                etCabang.text.toString(),

            hargaBeli =
                etHargaBeli.text.toString()
                    .toIntOrNull() ?: 0,

            nilaiProfit =
                etProfit.text.toString()
                    .toIntOrNull() ?: 0,

            hargaJual =
                etHargaJual.text.toString()
                    .toIntOrNull() ?: 0,

            stokProduk =
                etStok.text.toString()
                    .toIntOrNull() ?: 0,

            stokTakTerbatas =
                cbUnlimited.isChecked
        )

        myRef.child(id)
            .setValue(produk)

            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Produk berhasil disimpan",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
    }
}