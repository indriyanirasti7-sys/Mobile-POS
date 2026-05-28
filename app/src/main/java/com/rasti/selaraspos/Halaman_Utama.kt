package com.rasti.selaraspos

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.activities.LaporanActivity
import com.rasti.selaraspos.activities.AkunActivity
import com.rasti.selaraspos.activities.PrinterActivity
import com.rasti.selaraspos.activities.TransaksiActivity
import com.rasti.selaraspos.cabang.DataCabangActivity
import com.rasti.selaraspos.kategori.DataKategoriActivity
import com.rasti.selaraspos.pegawai.DataPegawaiActivity
import com.rasti.selaraspos.produk.DataProdukActivity

/**
 * Halaman Utama / Dashboard
 * Menampilkan menu navigasi dan statistik singkat
 */
class Halaman_Utama : AppCompatActivity() {

    // Menggunakan findViewById (cara lama, tidak perlu binding)
    private lateinit var tvSapaUser: TextView
    private lateinit var tvTotalTransaksi: TextView
    private lateinit var tvTotalProduk: TextView
    private lateinit var tvTotalPegawai: TextView

    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_halaman_utama) // Pastikan nama XML sesuai

        // Inisialisasi views dengan findViewById
        initViews()

        // Muat data
        tampilkanSapaUser()
        muatStatistik()
        aturNavigasiMenu()
        aturBottomNav()
    }

    /**
     * Inisialisasi semua View menggunakan findViewById
     */
    private fun initViews() {
        tvSapaUser = findViewById(R.id.tvSapaUser)
        tvTotalTransaksi = findViewById(R.id.tvTotalTransaksi)
        tvTotalProduk = findViewById(R.id.tvTotalProduk)
        tvTotalPegawai = findViewById(R.id.tvTotalPegawai)
    }

    /**
     * Tampilkan sapaan user (tanpa auth, pakai nama default)
     */
    private fun tampilkanSapaUser() {
        // Karena tidak pakai auth, kita set nama default
        // Bisa juga ambil dari SharedPreferences atau Intent
        tvSapaUser.text = "Halo, Pengguna 👋"
    }

    /**
     * Muat data statistik: total transaksi, produk, pegawai
     */
    private fun muatStatistik() {
        // Total produk
        db.child("produk").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvTotalProduk.text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Total transaksi
        db.child("transaksi").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvTotalTransaksi.text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Total pegawai
        db.child("pegawai").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvTotalPegawai.text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Atur navigasi untuk setiap card menu di dashboard
     */
    private fun aturNavigasiMenu() {
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardProduk).setOnClickListener {
            startActivity(Intent(this, DataProdukActivity::class.java))
        }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardKategori).setOnClickListener {
            startActivity(Intent(this, DataKategoriActivity::class.java))
        }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardTransaksi).setOnClickListener {
            startActivity(Intent(this, TransaksiActivity::class.java))
        }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardLaporan).setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardAkun).setOnClickListener {
            startActivity(Intent(this, AkunActivity::class.java))
        }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardPegawai).setOnClickListener {
            startActivity(Intent(this, DataPegawaiActivity::class.java))
        }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardCabang).setOnClickListener {
            startActivity(Intent(this, DataCabangActivity::class.java))
        }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardPrinter).setOnClickListener {
            startActivity(Intent(this, PrinterActivity::class.java))
        }
    }

    /**
     * Atur navigasi Bottom Navigation Bar
     */
    private fun aturBottomNav() {
        findViewById<android.widget.LinearLayout>(R.id.navBeranda).setOnClickListener {
            // Sudah di beranda
        }
        findViewById<android.widget.LinearLayout>(R.id.navTransaksi).setOnClickListener {
            startActivity(Intent(this, TransaksiActivity::class.java))
        }
        findViewById<android.widget.LinearLayout>(R.id.navLaporan).setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }
        findViewById<android.widget.LinearLayout>(R.id.navAkun).setOnClickListener {
            startActivity(Intent(this, AkunActivity::class.java))
        }
    }
}