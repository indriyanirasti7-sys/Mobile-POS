package com.rasti.selaraspos.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.R
import java.util.UUID
import com.rasti.selaraspos.adapters.AdapterLaporan
import com.rasti.selaraspos.databinding.ActivityLaporanBinding
import com.rasti.selaraspos.model.ModelLaporan
import com.rasti.selaraspos.model.ModelTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

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

        // 🔥 TOMBOL SHARE 🔥
        binding.btnShareLaporan.setOnClickListener {
            shareLaporan()
        }

        // 🔥 TOMBOL PRINT 🔥
        binding.btnPrintLaporan.setOnClickListener {
            printLaporan()
        }

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

    // ===== 🔥 FUNGSI SHARE LAPORAN 🔥 =====
    private fun shareLaporan() {
        if (listLaporan.isEmpty()) {
            Toast.makeText(this, "Tidak ada data laporan untuk dibagikan", Toast.LENGTH_SHORT).show()
            return
        }

        val totalPemasukan = binding.tvTotalPemasukan.text.toString()
        val totalTransaksi = binding.tvTotalTrxLaporan.text.toString()
        val periode = when (filterMode) {
            "HARIAN" -> "Harian"
            "MINGGUAN" -> "Mingguan"
            "BULANAN" -> "Bulanan"
            else -> "Semua"
        }

        val sb = StringBuilder()
        sb.append("═══════════════════════════════════\n")
        sb.append("        📊 LAPORAN SELARAS POS 📊\n")
        sb.append("═══════════════════════════════════\n\n")
        sb.append("Periode: $periode\n")
        sb.append("Cabang: $cabangFilter\n")
        sb.append("Total Pemasukan: $totalPemasukan\n")
        sb.append("Total Transaksi: $totalTransaksi\n")
        sb.append("\n───────────────────────────────────\n")
        sb.append("📋 DETAIL TRANSAKSI:\n")
        sb.append("───────────────────────────────────\n\n")

        listLaporan.forEachIndexed { index, item ->
            sb.append("${index + 1}. ID: ${item.idTransaksi.takeLast(8)}\n")
            sb.append("   Tanggal: ${item.tanggal}\n")
            sb.append("   Kasir: ${item.namaKasir}\n")
            sb.append("   Cabang: ${item.cabang}\n")
            sb.append("   Metode: ${item.metodePembayaran}\n")
            sb.append("   Total: ${NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(item.total)}\n")
            sb.append("\n")
        }

        sb.append("═══════════════════════════════════\n")
        sb.append("      Terima kasih telah menggunakan\n")
        sb.append("           Selaras POS 🤍\n")
        sb.append("═══════════════════════════════════\n")

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan via"))
    }

    // ===== 🔥 FUNGSI PRINT LAPORAN (Ke Printer Bluetooth) 🔥 =====
    private fun printLaporan() {
        if (listLaporan.isEmpty()) {
            Toast.makeText(this, "Tidak ada data laporan untuk dicetak", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("printer_prefs", MODE_PRIVATE)
        val macAddress = prefs.getString("mac_printer", "")

        if (macAddress.isNullOrEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Printer Belum Terhubung")
                .setMessage("Apakah Anda ingin mengatur printer terlebih dahulu?")
                .setPositiveButton("Atur Printer") { _, _ ->
                    startActivity(Intent(this, PrinterActivity::class.java))
                }
                .setNegativeButton("Batal", null)
                .show()
            return
        }

        val totalPemasukan = binding.tvTotalPemasukan.text.toString()
        val totalTransaksi = binding.tvTotalTrxLaporan.text.toString()
        val periode = when (filterMode) {
            "HARIAN" -> "Harian"
            "MINGGUAN" -> "Mingguan"
            "BULANAN" -> "Bulanan"
            else -> "Semua"
        }

        val sb = StringBuilder()
        sb.append("\n\n")
        sb.append("============================\n")
        sb.append("      📊 LAPORAN PENJUALAN\n")
        sb.append("============================\n")
        sb.append("Periode: $periode\n")
        sb.append("Cabang: $cabangFilter\n")
        sb.append("Tanggal: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        sb.append("----------------------------\n")
        sb.append("Total Pemasukan: $totalPemasukan\n")
        sb.append("Total Transaksi: $totalTransaksi\n")
        sb.append("----------------------------\n")
        sb.append("DETAIL TRANSAKSI:\n")
        sb.append("----------------------------\n")

        listLaporan.forEachIndexed { index, item ->
            sb.append("${index + 1}. #${item.idTransaksi.takeLast(8)}\n")
            sb.append("   ${item.tanggal}\n")
            sb.append("   ${item.namaKasir} | ${item.metodePembayaran}\n")
            sb.append("   Total: ${NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(item.total)}\n")
            sb.append("\n")
        }

        sb.append("============================\n")
        sb.append("   Terima kasih 🤍\n")
        sb.append("   Selaras POS\n")
        sb.append("============================\n\n\n")

        // Kirim ke printer
        val btAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        val device = btAdapter?.getRemoteDevice(macAddress)

        if (device == null) {
            Toast.makeText(this, "Printer tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                val socket = device.createRfcommSocketToServiceRecord(
                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                )
                socket.connect()
                val os = socket.outputStream
                os.write(sb.toString().toByteArray(Charsets.UTF_8))
                os.flush()
                os.close()
                socket.close()
                runOnUiThread {
                    Toast.makeText(this, "✅ Laporan berhasil dicetak!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "❌ Gagal mencetak: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}