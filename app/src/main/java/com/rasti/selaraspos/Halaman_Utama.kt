package com.rasti.selaraspos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rasti.selaraspos.kategori.DataKategoriActivity
import com.rasti.selaraspos.produk.DataProdukActivity

class Halaman_Utama : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_halaman_utama)

        // Panggil fungsi untuk mengatur klik pada semua card
        setupCardClickListeners()
    }

    private fun setupCardClickListeners() {
        val cardTransaksi = findViewById<CardView>(R.id.cardTransaksi)
        cardTransaksi.setOnClickListener {
            Toast.makeText(this, "Membuka Halaman Transaksi", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DataProdukActivity::class.java)
            startActivity(intent)
        }



        // 5. Card Layanan (Produk)
        val cardLayanan = findViewById<CardView>(R.id.cardLayanan)
        cardLayanan.setOnClickListener {
            Toast.makeText(this, "Membuka Halaman Produk", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DataProdukActivity::class.java)
            startActivity(intent)
        }

        // 6. Card Tambahan (Kategori)
        val cardTambahan = findViewById<CardView>(R.id.cardTambahan)
        cardTambahan.setOnClickListener {
            Toast.makeText(this, "Membuka Halaman Kategori", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DataKategoriActivity::class.java)
            startActivity(intent)
        }


    }
}