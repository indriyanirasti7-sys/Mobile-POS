package com.rasti.selaraspos

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.activities.AkunActivity
import com.rasti.selaraspos.activities.LaporanActivity
import com.rasti.selaraspos.activities.PrinterActivity
import com.rasti.selaraspos.activities.TransaksiActivity
import com.rasti.selaraspos.cabang.DataCabangActivity
import com.rasti.selaraspos.kategori.DataKategoriActivity
import com.rasti.selaraspos.pegawai.DataPegawaiActivity
import com.rasti.selaraspos.produk.DataProdukActivity
import java.text.SimpleDateFormat
import java.util.*

class Halaman_Utama : AppCompatActivity() {

    // ===== VIEW BINDING manual =====
    private lateinit var tvGreeting: TextView
    private lateinit var tvNamaPengguna: TextView
    private lateinit var tvTotalTransaksi: TextView
    private lateinit var tvTotalProduk: TextView
    private lateinit var tvTotalPegawai: TextView
    private lateinit var loadingOverlay: FrameLayout

    // Menu cards
    private lateinit var cardTransaksi: CardView
    private lateinit var cardLaporan: CardView
    private lateinit var cardAkun: CardView
    private lateinit var cardPegawai: CardView
    private lateinit var cardCabang: CardView
    private lateinit var cardPrinter: CardView

    // ===== BOTTOM NAVIGATION =====
    private lateinit var bottomNavigation: BottomNavigationView

    // Firebase
    private val database = FirebaseDatabase.getInstance()
    private var idKasir = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_halaman_utama)

        // Ambil ID Kasir dari SharedPreferences
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        idKasir = prefs.getString("idKasir", "") ?: ""

        initViews()
        setNamaUser()
        loadStatistik()
        setupMenuClick()
        setupBottomNavigation()
    }

    // ===== INISIALISASI VIEW =====
    private fun initViews() {
        tvGreeting = findViewById(R.id.tvGreeting)
        tvNamaPengguna = findViewById(R.id.tvNamaPengguna)
        tvTotalTransaksi = findViewById(R.id.tvTotalTransaksi)
        tvTotalProduk = findViewById(R.id.tvTotalProduk)
        tvTotalPegawai = findViewById(R.id.tvTotalPegawai)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        cardTransaksi = findViewById(R.id.cardTransaksi)
        cardLaporan = findViewById(R.id.cardLaporan)
        cardAkun = findViewById(R.id.cardAkun)
        cardPegawai = findViewById(R.id.cardPegawai)
        cardCabang = findViewById(R.id.cardCabang)
        cardPrinter = findViewById(R.id.cardPrinter)

        // Init Bottom Navigation
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    // ===== TAMPILKAN NAMA USER DARI SHAREDPREFERENCES =====
    private fun setNamaUser() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val namaLokal = prefs.getString("namaKasir", "Pengguna")
        tvNamaPengguna.text = namaLokal
    }

    // ===== LOAD STATISTIK DARI FIREBASE =====
    private fun loadStatistik() {
        showLoading(true)

        val hariIni = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Load Total Transaksi Hari Ini
        database.getReference("transaksi")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var countTransaksi = 0L
                    for (item in snapshot.children) {
                        val tanggal = item.child("tanggal").getValue(String::class.java) ?: ""
                        if (tanggal.startsWith(hariIni)) countTransaksi++
                    }
                    tvTotalTransaksi.text = countTransaksi.toString()
                    showLoading(false)
                }
                override fun onCancelled(error: DatabaseError) {
                    showLoading(false)
                }
            })

        // Load Total Produk
        database.getReference("produk").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvTotalProduk.text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Load Total Pegawai
        database.getReference("pegawai").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvTotalPegawai.text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ===== SETUP CLICK MENU GRID =====
    private fun setupMenuClick() {
        cardTransaksi.setOnClickListener {
            startActivity(Intent(this, TransaksiActivity::class.java))
        }
        cardLaporan.setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }
        cardAkun.setOnClickListener {
            startActivity(Intent(this, AkunActivity::class.java))
        }
        cardPegawai.setOnClickListener {
            startActivity(Intent(this, DataPegawaiActivity::class.java))
        }
        cardCabang.setOnClickListener {
            startActivity(Intent(this, DataCabangActivity::class.java))
        }
        cardPrinter.setOnClickListener {
            startActivity(Intent(this, PrinterActivity::class.java))
        }
    }

    // ===== SETUP BOTTOM NAVIGATION =====
    private fun setupBottomNavigation() {
        // Set item yang aktif ke Beranda
        bottomNavigation.selectedItemId = R.id.nav_beranda

        bottomNavigation.setOnItemSelectedListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.nav_beranda -> {
                    // Sudah di halaman beranda
                    true
                }
                R.id.nav_produk -> {
                    // Langsung panggil DataProdukActivity
                    startActivity(Intent(this, DataProdukActivity::class.java))
                    true
                }
                R.id.nav_kategori -> {
                    // Langsung panggil DataKategoriActivity
                    startActivity(Intent(this, DataKategoriActivity::class.java))
                    true
                }
                R.id.nav_transaksi -> {
                    startActivity(Intent(this, TransaksiActivity::class.java))
                    true
                }
                R.id.nav_akun -> {
                    startActivity(Intent(this, AkunActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // ===== SHOW LOADING OVERLAY =====
    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }
}