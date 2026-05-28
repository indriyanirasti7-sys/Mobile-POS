package com.rasti.selaraspos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.activities.AkunActivity
import com.rasti.selaraspos.activities.LaporanActivity
import com.rasti.selaraspos.activities.PrinterActivity
import com.rasti.selaraspos.activities.TransaksiActivity
import com.rasti.selaraspos.adapters.AdapterTransaksiTerbaru
import com.rasti.selaraspos.cabang.DataCabangActivity
import com.rasti.selaraspos.kategori.DataKategoriActivity
import com.rasti.selaraspos.model.ModelTransaksi
import com.rasti.selaraspos.pegawai.DataPegawaiActivity
import com.rasti.selaraspos.produk.DataProdukActivity

/**
 * Halaman Utama / Dashboard
 * Fitur:
 * - Sapa user berdasarkan nama dari SharedPreferences
 * - Statistik real-time (transaksi, produk, pegawai)
 * - Menu navigasi ke semua fitur
 * - Daftar 5 transaksi terbaru secara real-time dari Firebase
 */
class Halaman_Utama : AppCompatActivity() {

    private val db = FirebaseDatabase.getInstance().reference
    private lateinit var rvTransaksiTerbaru: RecyclerView
    private lateinit var cardKosong: MaterialCardView
    private val listTransaksiTerbaru = mutableListOf<ModelTransaksi>()
    private lateinit var adapterTerbaru: AdapterTransaksiTerbaru

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_halaman_utama)

        initViews()
        tampilkanSapaUser()
        muatStatistik()
        aturNavigasiMenu()
        aturBottomNav()
        muatTransaksiTerbaru()
    }

    private fun initViews() {
        // RecyclerView transaksi terbaru
        rvTransaksiTerbaru = findViewById(R.id.rvTransaksiTerbaru)
        cardKosong = findViewById(R.id.cardKosongTransaksi)

        adapterTerbaru = AdapterTransaksiTerbaru(listTransaksiTerbaru)
        rvTransaksiTerbaru.layoutManager = LinearLayoutManager(this)
        rvTransaksiTerbaru.adapter = adapterTerbaru
        rvTransaksiTerbaru.isNestedScrollingEnabled = false
    }

    /**
     * Ambil nama dari SharedPreferences (disimpan saat login)
     */
    private fun tampilkanSapaUser() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val nama = prefs.getString("nama", "Pengguna") ?: "Pengguna"
        findViewById<TextView>(R.id.tvSapaUser).text = nama
    }

    /**
     * Statistik real-time menggunakan addValueEventListener
     */
    private fun muatStatistik() {
        db.child("produk").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                findViewById<TextView>(R.id.tvTotalProduk).text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        db.child("transaksi").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                findViewById<TextView>(R.id.tvTotalTransaksi).text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        db.child("pegawai").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                findViewById<TextView>(R.id.tvTotalPegawai).text = snapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /**
     * Muat 5 transaksi terbaru dari Firebase
     * Diurutkan berdasarkan timestamp → tampil di card "Transaksi Terbaru"
     */
    private fun muatTransaksiTerbaru() {
        db.child("transaksi")
            .orderByChild("timestamp")  // urut berdasarkan waktu
            .limitToLast(5)             // ambil 5 terbaru
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listTransaksiTerbaru.clear()
                    for (snap in snapshot.children) {
                        snap.getValue(ModelTransaksi::class.java)?.let {
                            listTransaksiTerbaru.add(0, it) // tambah ke depan agar urutan terbaru dulu
                        }
                    }

                    // Tampilkan card kosong jika tidak ada data, RecyclerView jika ada
                    if (listTransaksiTerbaru.isEmpty()) {
                        cardKosong.visibility = View.VISIBLE
                        rvTransaksiTerbaru.visibility = View.GONE
                    } else {
                        cardKosong.visibility = View.GONE
                        rvTransaksiTerbaru.visibility = View.VISIBLE
                        adapterTerbaru.updateData(listTransaksiTerbaru)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun aturNavigasiMenu() {
        findViewById<MaterialCardView>(R.id.cardProduk).setOnClickListener {
            startActivity(Intent(this, DataProdukActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardKategori).setOnClickListener {
            startActivity(Intent(this, DataKategoriActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardTransaksi).setOnClickListener {
            startActivity(Intent(this, TransaksiActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardLaporan).setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardAkun).setOnClickListener {
            startActivity(Intent(this, AkunActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardPegawai).setOnClickListener {
            startActivity(Intent(this, DataPegawaiActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardCabang).setOnClickListener {
            startActivity(Intent(this, DataCabangActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cardPrinter).setOnClickListener {
            startActivity(Intent(this, PrinterActivity::class.java))
        }
    }

    private fun aturBottomNav() {
        findViewById<LinearLayout>(R.id.navBeranda).setOnClickListener {
            // Sudah di beranda
        }
        findViewById<LinearLayout>(R.id.navTransaksi).setOnClickListener {
            startActivity(Intent(this, TransaksiActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navLaporan).setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navAkun).setOnClickListener {
            startActivity(Intent(this, AkunActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Perbarui nama user jika baru diubah dari AkunActivity
        tampilkanSapaUser()
    }
}