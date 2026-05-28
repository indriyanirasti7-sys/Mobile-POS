package com.rasti.selaraspos.activities

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rasti.selaraspos.R
import com.rasti.selaraspos.adapters.AdapterPrinter
import com.rasti.selaraspos.databinding.ActivityPrinterBinding
import java.io.OutputStream
import java.util.UUID

class PrinterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrinterBinding
    private val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var os: OutputStream? = null
    private val prefs by lazy { getSharedPreferences("printer_prefs", MODE_PRIVATE) }
    private val SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val printerList = mutableListOf<BluetoothDevice>()
    private lateinit var printerAdapter: AdapterPrinter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrinterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        cekBluetooth()
        tampilkanTersimpan()
        scanPaired() // Langsung scan perangkat yang sudah dipair

        binding.btnScanPrinter.setOnClickListener { scanPaired() }
        binding.btnTestPrint.setOnClickListener { testPrint() }
        binding.btnDisconnect.setOnClickListener { disconnect() }
        binding.btnKembali.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        printerAdapter = AdapterPrinter(printerList) { device ->
            connectPrinter(device)
        }
        binding.rvPrinterList.layoutManager = LinearLayoutManager(this)
        binding.rvPrinterList.adapter = printerAdapter
    }

    private fun cekBluetooth() {
        if (btAdapter == null) {
            binding.tvStatusBluetooth.text = "❌ Bluetooth tidak didukung"
            binding.tvStatusBluetooth.visibility = View.VISIBLE
            binding.btnScanPrinter.isEnabled = false
            return
        }
        if (!btAdapter.isEnabled) {
            binding.tvStatusBluetooth.text = "⚠️ Bluetooth tidak aktif. Nyalakan Bluetooth terlebih dahulu."
            binding.tvStatusBluetooth.visibility = View.VISIBLE
            binding.btnScanPrinter.isEnabled = false
        } else {
            binding.tvStatusBluetooth.text = "✅ Bluetooth aktif"
            binding.tvStatusBluetooth.visibility = View.GONE // Sembunyikan jika aktif
        }
    }

    private fun tampilkanTersimpan() {
        val mac = prefs.getString("mac_printer", "") ?: ""
        val nama = prefs.getString("nama_printer", "") ?: ""
        if (mac.isNotEmpty()) {
            binding.tvPrinterTersimpan.text = nama
            binding.tvStatusKoneksi.text = "✅ Terhubung ke $nama"
            binding.btnTestPrint.isEnabled = true
            binding.btnDisconnect.isEnabled = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun scanPaired() {
        val paired = btAdapter?.bondedDevices ?: return
        if (paired.isEmpty()) {
            Toast.makeText(this, "Tidak ada perangkat yang dipair", Toast.LENGTH_SHORT).show()
            binding.tvEmptyPrinter.visibility = View.VISIBLE
            binding.rvPrinterList.visibility = View.GONE
            return
        }

        binding.tvEmptyPrinter.visibility = View.GONE
        binding.rvPrinterList.visibility = View.VISIBLE

        printerList.clear()
        printerList.addAll(paired)
        printerAdapter.notifyDataSetChanged()
    }

    @SuppressLint("MissingPermission")
    private fun connectPrinter(device: BluetoothDevice) {
        binding.tvStatusKoneksi.text = "Menghubungkan ke ${device.name}..."
        binding.progressPrinter.visibility = View.VISIBLE
        binding.rvPrinterList.visibility = View.GONE
        binding.tvEmptyPrinter.visibility = View.GONE

        Thread {
            try {
                socket = device.createRfcommSocketToServiceRecord(SPP)
                socket?.connect()
                os = socket?.outputStream

                prefs.edit().putString("mac_printer", device.address)
                    .putString("nama_printer", device.name).apply()

                runOnUiThread {
                    binding.progressPrinter.visibility = View.GONE
                    binding.tvStatusKoneksi.text = "✅ Terhubung ke ${device.name}"
                    binding.tvPrinterTersimpan.text = device.name
                    binding.btnTestPrint.isEnabled = true
                    binding.btnDisconnect.isEnabled = true
                    Toast.makeText(this, "Printer terhubung!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.progressPrinter.visibility = View.GONE
                    binding.tvStatusKoneksi.text = "❌ Gagal: ${e.message}"
                    binding.rvPrinterList.visibility = View.VISIBLE
                    Toast.makeText(this, "Gagal konek: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun testPrint() {
        if (os == null) {
            Toast.makeText(this, "Printer belum terhubung", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                os?.write("\n  *** SELARAS POS ***\n  Test Print OK!\n\n\n".toByteArray())
                os?.flush()
                runOnUiThread { Toast.makeText(this, "Test print berhasil!", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }.start()
    }

    private fun disconnect() {
        try {
            os?.close()
            socket?.close()
        } catch (_: Exception) {}
        os = null
        socket = null
        binding.tvStatusKoneksi.text = "Tidak Terhubung"
        binding.tvPrinterTersimpan.text = ""
        binding.btnTestPrint.isEnabled = false
        binding.btnDisconnect.isEnabled = false
        Toast.makeText(this, "Printer diputuskan", Toast.LENGTH_SHORT).show()

        // Tampilkan daftar printer lagi
        scanPaired()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }
}