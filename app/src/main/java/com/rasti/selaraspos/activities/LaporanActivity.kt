package com.rasti.selaraspos.activities

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.database.*
import com.rasti.selaraspos.R
import com.selaraspos.adapter.AdapterLaporan
import com.rasti.selaraspos.model.ModelLaporan
import com.rasti.selaraspos.model.ModelTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class LaporanActivity : AppCompatActivity() {

    // Komponen UI
    private lateinit var rvLaporan: RecyclerView
    private lateinit var actvFilterCabang: MaterialAutoCompleteTextView
    private lateinit var btnHarian: MaterialButton
    private lateinit var btnMingguan: MaterialButton
    private lateinit var btnBulanan: MaterialButton
    private lateinit var btnKembali: ImageButton
    private lateinit var tvEmptyLaporan: TextView
    private lateinit var tvTotalPemasukan: TextView
    private lateinit var tvTotalTrxLaporan: TextView
    private lateinit var progressLaporan: ProgressBar

    private val db = FirebaseDatabase.getInstance().reference.child("transaksi")
    private val listLaporan = mutableListOf<ModelLaporan>()
    private lateinit var adapter: AdapterLaporan

    private var filterMode = "HARIAN"
    private var cabangFilter = "Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan)

        initViews()
        setupRecyclerView()
        muatCabang()
        aturFilter()
        muatLaporan()

        btnKembali.setOnClickListener { finish() }
    }

    private fun initViews() {
        rvLaporan = findViewById(R.id.rvLaporan)
        actvFilterCabang = findViewById(R.id.actvFilterCabang)
        btnHarian = findViewById(R.id.btnHarian)
        btnMingguan = findViewById(R.id.btnMingguan)
        btnBulanan = findViewById(R.id.btnBulanan)
        btnKembali = findViewById(R.id.btnKembali)
        tvEmptyLaporan = findViewById(R.id.tvEmptyLaporan)
        tvTotalPemasukan = findViewById(R.id.tvTotalPemasukan)
        tvTotalTrxLaporan = findViewById(R.id.tvTotalTrxLaporan)
        progressLaporan = findViewById(R.id.progressLaporan)
    }

    private fun setupRecyclerView() {
        adapter = AdapterLaporan(listLaporan)
        rvLaporan.layoutManager = LinearLayoutManager(this)
        rvLaporan.adapter = adapter
    }

    private fun muatCabang() {
        FirebaseDatabase.getInstance().reference.child("cabang")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listCabang = mutableListOf("Semua")
                    for (snap in snapshot.children) {
                        snap.child("namaCabang").getValue(String::class.java)?.let { listCabang.add(it) }
                    }
                    val adp = ArrayAdapter(this@LaporanActivity, android.R.layout.simple_dropdown_item_1line, listCabang)
                    actvFilterCabang.setAdapter(adp)
                    actvFilterCabang.setText("Semua", false)
                    actvFilterCabang.setOnItemClickListener { _, _, position, _ ->
                        cabangFilter = listCabang[position]
                        muatLaporan()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun aturFilter() {
        val listener = View.OnClickListener { v ->
            filterMode = when (v.id) {
                R.id.btnHarian -> "HARIAN"
                R.id.btnMingguan -> "MINGGUAN"
                else -> "BULANAN"
            }
            highlightFilter()
            muatLaporan()
        }
        btnHarian.setOnClickListener(listener)
        btnMingguan.setOnClickListener(listener)
        btnBulanan.setOnClickListener(listener)
        highlightFilter()
    }

    private fun highlightFilter() {
        val primary = ContextCompat.getColor(this, R.color.primary)
        val variant = ContextCompat.getColor(this, R.color.surface_variant)
        val onPrimary = ContextCompat.getColor(this, R.color.text_on_primary)
        val onSurface = ContextCompat.getColor(this, R.color.text_primary)

        val buttons = listOf(btnHarian, btnMingguan, btnBulanan)
        val modes = listOf("HARIAN", "MINGGUAN", "BULANAN")

        buttons.forEachIndexed { index, btn ->
            val isActive = modes[index] == filterMode
            btn.backgroundTintList = ColorStateList.valueOf(if (isActive) primary else variant)
            btn.setTextColor(if (isActive) onPrimary else onSurface)
        }
    }

    private fun muatLaporan() {
        progressLaporan.visibility = View.VISIBLE
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filtered = snapshot.children.mapNotNull { it.getValue(ModelTransaksi::class.java) }
                    .filter { cekTanggal(it.tanggal) && (cabangFilter == "Semua" || it.cabang == cabangFilter) }

                val dataLaporan = filtered.map {
                    ModelLaporan(it.idTransaksi, it.tanggal, it.namaKasir, it.cabang, it.metodePembayaran, it.total)
                }

                progressLaporan.visibility = View.GONE
                tvTotalPemasukan.text = formatRupiah(filtered.sumOf { it.total })
                tvTotalTrxLaporan.text = "${filtered.size} Transaksi"
                tvEmptyLaporan.visibility = if (dataLaporan.isEmpty()) View.VISIBLE else View.GONE
                adapter.updateData(dataLaporan)
            }

            override fun onCancelled(error: DatabaseError) {
                progressLaporan.visibility = View.GONE
                Toast.makeText(this@LaporanActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cekTanggal(tglStr: String): Boolean {
        return try {
            val date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(tglStr) ?: return false
            val cal = Calendar.getInstance()
            val now = Calendar.getInstance()
            cal.time = date

            when (filterMode) {
                "HARIAN" -> cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                "MINGGUAN" -> cal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR) && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                "BULANAN" -> cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                else -> true
            }
        } catch (e: Exception) { false }
    }

    private fun formatRupiah(harga: Long) = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(harga)
}