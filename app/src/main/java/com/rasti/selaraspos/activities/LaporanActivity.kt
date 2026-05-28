package com.rasti.selaraspos.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.LoginActivity
import com.rasti.selaraspos.R
import com.rasti.selaraspos.RoleHelper
import com.rasti.selaraspos.adapters.AdapterLaporan
import com.rasti.selaraspos.databinding.ActivityAkunBinding
import com.rasti.selaraspos.databinding.ActivityLaporanBinding
import com.rasti.selaraspos.model.ModelLaporan
import com.rasti.selaraspos.model.ModelTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// LaporanActivity
// ─────────────────────────────────────────────────────────────────────────────

class LaporanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaporanBinding
    private val db = FirebaseDatabase.getInstance().reference.child("transaksi")
    private val listLaporan = mutableListOf<ModelLaporan>()
    private lateinit var adapter: AdapterLaporan
    private var filterMode = "HARIAN"
    private var cabangFilter = "Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AdapterLaporan(listLaporan)
        binding.rvLaporan.layoutManager = LinearLayoutManager(this)
        binding.rvLaporan.adapter = adapter

        muatCabang()
        aturTombolFilter()
        muatLaporan()

        binding.btnKembali.setOnClickListener { finish() }
    }

    private fun muatCabang() {
        FirebaseDatabase.getInstance().reference.child("cabang")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf("Semua")
                    for (snap in snapshot.children) {
                        snap.child("namaCabang").getValue(String::class.java)?.let { list.add(it) }
                    }
                    val adp = ArrayAdapter(this@LaporanActivity,
                        android.R.layout.simple_dropdown_item_1line, list)
                    binding.actvFilterCabang.setAdapter(adp)
                    binding.actvFilterCabang.setText("Semua", false)
                    binding.actvFilterCabang.setOnItemClickListener { _, _, pos, _ ->
                        cabangFilter = list[pos]; muatLaporan()
                    }
                }
                override fun onCancelled(e: DatabaseError) {}
            })
    }

    private fun aturTombolFilter() {
        binding.btnHarian.setOnClickListener { filterMode = "HARIAN"; updateFilterUI(); muatLaporan() }
        binding.btnMingguan.setOnClickListener { filterMode = "MINGGUAN"; updateFilterUI(); muatLaporan() }
        binding.btnBulanan.setOnClickListener { filterMode = "BULANAN"; updateFilterUI(); muatLaporan() }
        updateFilterUI()
    }

    private fun updateFilterUI() {
        val aktif = getColor(R.color.primary)
        val nonAktif = getColor(R.color.text_secondary)
        binding.btnHarian.setTextColor(if (filterMode == "HARIAN") aktif else nonAktif)
        binding.btnMingguan.setTextColor(if (filterMode == "MINGGUAN") aktif else nonAktif)
        binding.btnBulanan.setTextColor(if (filterMode == "BULANAN") aktif else nonAktif)
    }

    private fun muatLaporan() {
        binding.progressLaporan.visibility = View.VISIBLE
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val all = mutableListOf<ModelTransaksi>()
                for (snap in snapshot.children) {
                    snap.getValue(ModelTransaksi::class.java)?.let { all.add(it) }
                }

                val filtered = all.filter { trx ->
                    cekTanggal(trx.tanggal) && (cabangFilter == "Semua" || trx.cabang == cabangFilter)
                }

                val dataLaporan = filtered.map {
                    ModelLaporan(it.idTransaksi, it.tanggal, it.namaKasir, it.cabang, it.metodePembayaran, it.total)
                }

                binding.progressLaporan.visibility = View.GONE
                binding.tvTotalPemasukan.text = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                    .format(filtered.sumOf { it.total })
                binding.tvTotalTrxLaporan.text = "${filtered.size} Transaksi"
                binding.tvEmptyLaporan.visibility = if (dataLaporan.isEmpty()) View.VISIBLE else View.GONE
                adapter.updateData(dataLaporan)
            }
            override fun onCancelled(e: DatabaseError) {
                binding.progressLaporan.visibility = View.GONE
            }
        })
    }

    private fun cekTanggal(tglStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val tgl = sdf.parse(tglStr) ?: return false
            val cal = Calendar.getInstance()
            val now = cal.time
            when (filterMode) {
                "HARIAN" -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).let {
                    it.format(tgl) == it.format(now) }
                "MINGGUAN" -> {
                    val w = cal.get(Calendar.WEEK_OF_YEAR); val y = cal.get(Calendar.YEAR)
                    cal.time = tgl
                    cal.get(Calendar.WEEK_OF_YEAR) == w && cal.get(Calendar.YEAR) == y
                }
                "BULANAN" -> SimpleDateFormat("MM/yyyy", Locale.getDefault()).let {
                    it.format(tgl) == it.format(now) }
                else -> true
            }
        } catch (_: Exception) { false }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AkunActivity
// ─────────────────────────────────────────────────────────────────────────────

