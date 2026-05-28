package com.rasti.selaraspos.activities

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.*
import com.rasti.selaraspos.R
import com.selaraspos.adapter.AdapterKeranjang
import com.selaraspos.adapter.AdapterProdukTransaksi
import com.rasti.selaraspos.model.ModelKeranjang
import com.rasti.selaraspos.model.ModelProduk
import java.text.NumberFormat
import java.util.*

class TransaksiActivity : AppCompatActivity() {

    private lateinit var rvProdukTrx: RecyclerView
    private lateinit var rvKeranjang: RecyclerView
    private lateinit var etSearchTrx: EditText
    private lateinit var chipGroupKategori: ChipGroup
    private lateinit var tvNamaKasir: TextView
    private lateinit var tvCabangTrx: TextView
    private lateinit var tvTotalKeranjang: TextView
    private lateinit var tvJumlahItem: TextView
    private lateinit var btnBayar: Button
    private lateinit var btnKembali: ImageButton
    private lateinit var tvEmptyProdukTrx: TextView

    private val db = FirebaseDatabase.getInstance().reference
    private val listProduk = mutableListOf<ModelProduk>()
    private val listKeranjang = mutableListOf<ModelKeranjang>()
    private lateinit var adapterProduk: AdapterProdukTransaksi
    private lateinit var adapterKeranjang: AdapterKeranjang

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaksi)

        initViews()
        setupRecyclerViews()
        muatProduk()
        muatKategori()

        btnBayar.setOnClickListener {
            if (listKeranjang.isEmpty()) {
                Toast.makeText(this, "Keranjang kosong!", Toast.LENGTH_SHORT).show()
            } else {
                // Panggil fungsi dialog pembayaran Anda di sini
            }
        }
        btnKembali.setOnClickListener { finish() }
    }

    private fun initViews() {
        rvProdukTrx = findViewById(R.id.rvProdukTrx)
        rvKeranjang = findViewById(R.id.rvKeranjang)
        etSearchTrx = findViewById(R.id.etSearchTrx)
        chipGroupKategori = findViewById(R.id.chipGroupKategori)
        tvNamaKasir = findViewById(R.id.tvNamaKasir)
        tvCabangTrx = findViewById(R.id.tvCabangTrx)
        tvTotalKeranjang = findViewById(R.id.tvTotalKeranjang)
        tvJumlahItem = findViewById(R.id.tvJumlahItem)
        btnBayar = findViewById(R.id.btnBayar)
        btnKembali = findViewById(R.id.btnKembali)
        tvEmptyProdukTrx = findViewById(R.id.tvEmptyProdukTrx)
    }

    private fun setupRecyclerViews() {
        adapterProduk = AdapterProdukTransaksi(listProduk) { /* logik tambah ke keranjang */ }
        rvProdukTrx.layoutManager = GridLayoutManager(this, 2)
        rvProdukTrx.adapter = adapterProduk

        adapterKeranjang = AdapterKeranjang(listKeranjang, { _, _ -> }, { _ -> })
        rvKeranjang.layoutManager = LinearLayoutManager(this)
        rvKeranjang.adapter = adapterKeranjang
    }

    private fun muatProduk() {
        db.child("produk").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProduk.clear()
                for (snap in snapshot.children) {
                    val produk = snap.getValue(ModelProduk::class.java)
                    if (produk != null) listProduk.add(produk)
                }
                adapterProduk.updateData(listProduk)
                tvEmptyProdukTrx.visibility = if (listProduk.isEmpty()) View.VISIBLE else View.GONE
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransaksiActivity, "Gagal memuat produk", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun muatKategori() {
        // Implementasi load kategori Anda
    }
}