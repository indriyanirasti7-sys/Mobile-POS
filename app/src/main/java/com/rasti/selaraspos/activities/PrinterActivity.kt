package com.rasti.selaraspos.activities

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.model.ModelPrinter
import com.rasti.selaraspos.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

    class PrinterActivity : AppCompatActivity() {

        private lateinit var tvStatusPrinter: TextView
        private lateinit var tvNamaPrinterTerhubung: TextView
        private lateinit var btnScan: Button
        private lateinit var btnDisconnect: Button
        private lateinit var btnTestPrint: Button
        private lateinit var rvPrinterList: RecyclerView
        private lateinit var layoutEmptyPrinter: LinearLayout
        private lateinit var layoutScanning: LinearLayout
        private lateinit var btnBack: ImageButton

        // Bluetooth
        private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        private var bluetoothSocket: BluetoothSocket?   = null
        private var outputStream: OutputStream?         = null
        private var printerTerhubung: ModelPrinter?     = null

        // UUID standar untuk printer Bluetooth Serial Port Profile
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        // List printer tersedia
        private val listPrinter = mutableListOf<ModelPrinter>()
        private lateinit var adapterPrinter: AdapterPrinterItem

        private val scope = CoroutineScope(Dispatchers.Main)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_printer)

            initViews()
            setupAdapter()

            btnBack.setOnClickListener { finish() }
            btnScan.setOnClickListener { scanPrinter() }
            btnDisconnect.setOnClickListener { disconnectPrinter() }
            btnTestPrint.setOnClickListener { testPrint() }
        }

        private fun initViews() {
            tvStatusPrinter         = findViewById(R.id.tvStatusPrinter)
            tvNamaPrinterTerhubung  = findViewById(R.id.tvNamaPrinterTerhubung)
            btnScan                 = findViewById(R.id.btnScanPrinter)
            btnDisconnect           = findViewById(R.id.btnDisconnectPrinter)
            btnTestPrint            = findViewById(R.id.btnTestPrint)
            rvPrinterList           = findViewById(R.id.rvPrinterList)
            layoutEmptyPrinter      = findViewById(R.id.layoutEmptyPrinter)
            layoutScanning          = findViewById(R.id.layoutScanning)
            btnBack                 = findViewById(R.id.btnBack)
        }

        private fun setupAdapter() {
            adapterPrinter = AdapterPrinterItem(listPrinter) { printer ->
                connectPrinter(printer)
            }
            rvPrinterList.layoutManager = LinearLayoutManager(this)
            rvPrinterList.adapter = adapterPrinter
        }

        // ===== SCAN PERANGKAT BLUETOOTH =====
        private fun scanPrinter() {
            // Cek permission Bluetooth
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                        200
                    )
                    return
                }
            }

            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Perangkat tidak mendukung Bluetooth", Toast.LENGTH_SHORT).show()
                return
            }

            if (!bluetoothAdapter!!.isEnabled) {
                Toast.makeText(this, "Hidupkan Bluetooth terlebih dahulu", Toast.LENGTH_SHORT).show()
                return
            }

            // Tampilkan indikator scanning
            layoutScanning.visibility    = View.VISIBLE
            layoutEmptyPrinter.visibility = View.GONE
            listPrinter.clear()
            adapterPrinter.notifyDataSetChanged()

            // Ambil perangkat yang sudah dipasangkan (paired)
            scope.launch {
                delay(1500) // Simulasi scanning
                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter!!.bondedDevices
                pairedDevices?.forEach { device ->
                    listPrinter.add(
                        ModelPrinter(
                            namaPrinter = device.name ?: "Unknown Device",
                            alamatMac = device.address
                        )
                    )
                }
                layoutScanning.visibility = View.GONE
                adapterPrinter.notifyDataSetChanged()

                if (listPrinter.isEmpty()) {
                    layoutEmptyPrinter.visibility = View.VISIBLE
                }
            }
        }

        // ===== CONNECT KE PRINTER =====
        private fun connectPrinter(printer: ModelPrinter) {
            Toast.makeText(this, "Menghubungkan ke ${printer.namaPrinter}…", Toast.LENGTH_SHORT).show()

            scope.launch {
                val sukses = withContext(Dispatchers.IO) {
                    try {
                        val device = bluetoothAdapter?.getRemoteDevice(printer.alamatMac)
                        bluetoothSocket = device?.createRfcommSocketToServiceRecord(SPP_UUID)
                        bluetoothSocket?.connect()
                        outputStream = bluetoothSocket?.outputStream
                        true
                    } catch (e: IOException) {
                        bluetoothSocket?.close()
                        false
                    }
                }

                if (sukses) {
                    printerTerhubung = printer.copy(statusTerhubung = true)
                    updateStatusUI(true, printer.namaPrinter)
                    Toast.makeText(this@PrinterActivity, "✅ Terhubung ke ${printer.namaPrinter}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PrinterActivity, "❌ Gagal terhubung", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ===== DISCONNECT PRINTER =====
        private fun disconnectPrinter() {
            try {
                outputStream?.close()
                bluetoothSocket?.close()
            } catch (_: IOException) {}
            outputStream         = null
            bluetoothSocket      = null
            printerTerhubung     = null
            updateStatusUI(false, "")
            Toast.makeText(this, "Printer diputuskan", Toast.LENGTH_SHORT).show()
        }

        // ===== TEST PRINT =====
        private fun testPrint() {
            if (outputStream == null) {
                Toast.makeText(this, "Belum terhubung ke printer", Toast.LENGTH_SHORT).show()
                return
            }
            scope.launch {
                val berhasil = withContext(Dispatchers.IO) {
                    try {
                        val struk = buildStrukTest()
                        outputStream?.write(struk.toByteArray())
                        outputStream?.flush()
                        true
                    } catch (e: IOException) {
                        false
                    }
                }
                if (berhasil) Toast.makeText(this@PrinterActivity, "✅ Test print dikirim!", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this@PrinterActivity, "❌ Gagal print", Toast.LENGTH_SHORT).show()
            }
        }

        // ===== BUILD STRUK TEST =====
        private fun buildStrukTest(): String {
            return """
            |================================
            |       SELARAS POS
            |================================
            |Test Print Struk
            |Tanggal: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}
            |--------------------------------
            |Produk Test       1 x 10.000
            |--------------------------------
            |Total:            Rp 10.000
            |================================
            |     Terima Kasih!
            |================================
            |
            |
        """.trimMargin()
        }

        // ===== UPDATE STATUS UI =====
        private fun updateStatusUI(connected: Boolean, namaPrinter: String) {
            if (connected) {
                tvStatusPrinter.text      = "● Terhubung"
                tvStatusPrinter.setTextColor(getColor(R.color.success))
                tvNamaPrinterTerhubung.text = namaPrinter
                btnDisconnect.isEnabled   = true
                btnDisconnect.alpha       = 1f
                btnTestPrint.isEnabled    = true
                btnTestPrint.alpha        = 1f
            } else {
                tvStatusPrinter.text      = "● Tidak Terhubung"
                tvStatusPrinter.setTextColor(getColor(R.color.error))
                tvNamaPrinterTerhubung.text = "–"
                btnDisconnect.isEnabled   = false
                btnDisconnect.alpha       = 0.4f
                btnTestPrint.isEnabled    = false
                btnTestPrint.alpha        = 0.4f
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            scope.cancel()
            try { bluetoothSocket?.close() } catch (_: IOException) {}
        }

        // ===== INNER ADAPTER PRINTER ITEM =====
        inner class AdapterPrinterItem(
            private val list: List<ModelPrinter>,
            private val onClick: (ModelPrinter) -> Unit
        ) : RecyclerView.Adapter<AdapterPrinterItem.VH>() {

            inner class VH(v: View) : RecyclerView.ViewHolder(v) {
                val tvNama: TextView = v.findViewById(android.R.id.text1)
                val tvMac: TextView = v.findViewById(android.R.id.text2)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
                val v = LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_2, parent, false)
                // Terapkan style minimal di dalam kartu
                v.setBackgroundResource(R.drawable.bg_card)
                v.setPadding(32, 24, 32, 24)
                val lp = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(0, 0, 0, 12)
                v.layoutParams = lp
                return VH(v)
            }

            override fun onBindViewHolder(holder: VH, position: Int) {
                val printer = list[position]
                holder.tvNama.text = printer.namaPrinter
                holder.tvNama.setTextColor(getColor(R.color.text_primary))
                holder.tvMac.text  = printer.alamatMac
                holder.tvMac.setTextColor(getColor(R.color.text_secondary))
                holder.itemView.setOnClickListener { onClick(printer) }
            }

            override fun getItemCount() = list.size
        }
}
