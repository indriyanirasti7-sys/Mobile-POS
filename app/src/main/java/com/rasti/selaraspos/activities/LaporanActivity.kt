package com.rasti.selaraspos.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.model.ModelCabang
import com.rasti.selaraspos.model.ModelLaporan
import com.rasti.selaraspos.R
import com.rasti.selaraspos.adapters.AdapterLaporan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale



class LaporanActivity : AppCompatActivity() {

        private lateinit var rvLaporan: RecyclerView
        private lateinit var progressBar: ProgressBar
        private lateinit var layoutEmptyLaporan: LinearLayout
        private lateinit var tvSummaryTotalTransaksi: TextView
        private lateinit var tvSummaryTotalPenjualan: TextView
        private lateinit var btnHarian: Button
        private lateinit var btnMingguan: Button
        private lateinit var btnBulanan: Button
        private lateinit var tvFilterTanggal: TextView
        private lateinit var spinnerFilterCabang: Spinner
        private lateinit var layoutFilterTanggal: LinearLayout

        private val listLaporanOriginal = mutableListOf<ModelLaporan>()
        private val listLaporanFilter   = mutableListOf<ModelLaporan>()
        private val listCabang          = mutableListOf<ModelCabang>()
        private lateinit var adapterLaporan: AdapterLaporan

        private val database = FirebaseDatabase.getInstance()

        // State filter
        private var filterMode     = "harian"  // harian / mingguan / bulanan
        private var filterTanggal  = ""        // "" = semua
        private var filterCabang   = ""        // "" = semua

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_laporan)

            initViews()
            setupAdapter()
            setupTabButtons()
            setupFilterTanggal()
            loadCabang()
            loadLaporan()

            findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        }

        private fun initViews() {
            rvLaporan                 = findViewById(R.id.rvLaporan)
            progressBar               = findViewById(R.id.progressBar)
            layoutEmptyLaporan        = findViewById(R.id.layoutEmptyLaporan)
            tvSummaryTotalTransaksi   = findViewById(R.id.tvSummaryTotalTransaksi)
            tvSummaryTotalPenjualan   = findViewById(R.id.tvSummaryTotalPenjualan)
            btnHarian                 = findViewById(R.id.btnHarian)
            btnMingguan               = findViewById(R.id.btnMingguan)
            btnBulanan                = findViewById(R.id.btnBulanan)
            tvFilterTanggal           = findViewById(R.id.tvFilterTanggal)
            spinnerFilterCabang       = findViewById(R.id.spinnerFilterCabang)
            layoutFilterTanggal       = findViewById(R.id.layoutFilterTanggal)
        }

        private fun setupAdapter() {
            adapterLaporan = AdapterLaporan(listLaporanFilter)
            rvLaporan.layoutManager = LinearLayoutManager(this)
            rvLaporan.adapter = adapterLaporan
        }

        // ===== SETUP TAB BUTTONS =====
        private fun setupTabButtons() {
            btnHarian.setOnClickListener {
                filterMode = "harian"
                updateTabUI()
                terapkanFilter()
            }
            btnMingguan.setOnClickListener {
                filterMode = "mingguan"
                updateTabUI()
                terapkanFilter()
            }
            btnBulanan.setOnClickListener {
                filterMode = "bulanan"
                updateTabUI()
                terapkanFilter()
            }
        }

        private fun updateTabUI() {
            val selected   = R.drawable.bg_tab_selected
            val unselected = R.drawable.bg_tab_unselected
            btnHarian.setBackgroundResource(if (filterMode == "harian") selected else unselected)
            btnMingguan.setBackgroundResource(if (filterMode == "mingguan") selected else unselected)
            btnBulanan.setBackgroundResource(if (filterMode == "bulanan") selected else unselected)
            btnHarian.setTextColor(getColor(if (filterMode == "harian") R.color.white else R.color.text_secondary))
            btnMingguan.setTextColor(getColor(if (filterMode == "mingguan") R.color.white else R.color.text_secondary))
            btnBulanan.setTextColor(getColor(if (filterMode == "bulanan") R.color.white else R.color.text_secondary))
        }

        // ===== FILTER TANGGAL MANUAL =====
        private fun setupFilterTanggal() {
            layoutFilterTanggal.setOnClickListener {
                val cal = Calendar.getInstance()
                DatePickerDialog(
                    this,
                    { _, year, month, day ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        cal.set(year, month, day)
                        filterTanggal = sdf.format(cal.time)
                        val display = SimpleDateFormat("dd MMM yyyy", Locale("id")).format(cal.time)
                        tvFilterTanggal.text = display
                        terapkanFilter()
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        // ===== LOAD CABANG UNTUK FILTER =====
        private fun loadCabang() {
            database.getReference("cabang")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        listCabang.clear()
                        val items = mutableListOf("Semua Cabang")
                        for (data in snapshot.children) {
                            val cabang = data.getValue(ModelCabang::class.java) ?: continue
                            listCabang.add(cabang)
                            items.add(cabang.namaCabang)
                        }
                        val spinnerAdapter = ArrayAdapter(
                            this@LaporanActivity,
                            android.R.layout.simple_spinner_item, items
                        )
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerFilterCabang.adapter = spinnerAdapter

                        spinnerFilterCabang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                                filterCabang = if (pos == 0) "" else listCabang[pos - 1].namaCabang
                                terapkanFilter()
                            }
                            override fun onNothingSelected(p: AdapterView<*>?) {}
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        // ===== LOAD DATA LAPORAN DARI FIREBASE =====
        private fun loadLaporan() {
            progressBar.visibility = View.VISIBLE
            database.getReference("transaksi")
                .orderByChild("tanggalMilis")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        listLaporanOriginal.clear()
                        for (data in snapshot.children) {
                            val laporan = ModelLaporan(
                                idTransaksi = data.child("idTransaksi").getValue(String::class.java)
                                    ?: "",
                                tanggal = data.child("tanggal").getValue(String::class.java) ?: "",
                                tanggalMilis = data.child("tanggalMilis").getValue(Long::class.java)
                                    ?: 0L,
                                total = data.child("total").getValue(Long::class.java) ?: 0L,
                                metodePembayaran = data.child("metodePembayaran")
                                    .getValue(String::class.java) ?: "",
                                namaKasir = data.child("namaKasir").getValue(String::class.java)
                                    ?: "",
                                cabang = data.child("cabang").getValue(String::class.java) ?: ""
                            )
                            listLaporanOriginal.add(laporan)
                        }
                        // Urutkan terbaru dulu
                        listLaporanOriginal.sortByDescending { it.tanggalMilis }
                        progressBar.visibility = View.GONE
                        terapkanFilter()
                    }
                    override fun onCancelled(error: DatabaseError) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@LaporanActivity, "Gagal memuat laporan", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // ===== TERAPKAN FILTER =====
        private fun terapkanFilter() {
            val sdf    = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today  = Calendar.getInstance()
            val data   = listLaporanOriginal.filter { laporan ->

                // Filter mode waktu
                val modeOk = when (filterMode) {
                    "harian" -> {
                        val hariIni = sdf.format(today.time)
                        laporan.tanggal.startsWith(hariIni)
                    }
                    "mingguan" -> {
                        val startWeek = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                        }
                        laporan.tanggalMilis >= startWeek.timeInMillis
                    }
                    "bulanan" -> {
                        val bulanIni = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(today.time)
                        laporan.tanggal.startsWith(bulanIni)
                    }
                    else -> true
                }

                // Filter tanggal manual
                val tanggalOk = filterTanggal.isEmpty() || laporan.tanggal.startsWith(filterTanggal)

                // Filter cabang
                val cabangOk = filterCabang.isEmpty() || laporan.cabang == filterCabang

                modeOk && tanggalOk && cabangOk
            }

            listLaporanFilter.clear()
            listLaporanFilter.addAll(data)
            adapterLaporan.updateData(listLaporanFilter)
            updateSummary(listLaporanFilter)
            updateEmptyState()
        }

        // ===== UPDATE SUMMARY =====
        private fun updateSummary(data: List<ModelLaporan>) {
            tvSummaryTotalTransaksi.text = data.size.toString()
            val totalPenjualan = data.sumOf { it.total }
            tvSummaryTotalPenjualan.text = formatRupiah(totalPenjualan)
        }

        private fun updateEmptyState() {
            layoutEmptyLaporan.visibility = if (listLaporanFilter.isEmpty()) View.VISIBLE else View.GONE
            rvLaporan.visibility          = if (listLaporanFilter.isEmpty()) View.GONE else View.VISIBLE
        }

        private fun formatRupiah(nominal: Long): String {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            return format.format(nominal).replace("Rp", "Rp ").replace(",00", "")
        }
}
