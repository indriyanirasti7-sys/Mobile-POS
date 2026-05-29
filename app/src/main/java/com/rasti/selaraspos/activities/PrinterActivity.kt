package com.rasti.selaraspos.activities

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.rasti.selaraspos.R
import com.rasti.selaraspos.adapters.AdapterPrinter
import com.rasti.selaraspos.databinding.ActivityPrinterBinding
import com.rasti.selaraspos.model.ModelTransaksi
import java.io.OutputStream
import java.text.NumberFormat
import java.util.*

class PrinterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrinterBinding
    private val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val prefs by lazy { getSharedPreferences("printer_prefs", Context.MODE_PRIVATE) }
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val printerList = mutableListOf<BluetoothDevice>()
    private lateinit var printerAdapter: AdapterPrinter

    companion object {
        private var cachedTransaksi: ModelTransaksi? = null
        fun setTransaksiUntukPrint(transaksi: ModelTransaksi) {
            cachedTransaksi = transaksi
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrinterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestBluetoothPermissions()
        setupRecyclerView()
        cekBluetooth()
        scanPaired()
        tampilkanPrinterTersimpan()

        binding.btnScanPrinter.setOnClickListener { scanPaired() }
        binding.btnDisconnect.setOnClickListener { disconnect() }
        binding.btnKembali.setOnClickListener { finish() }

        // 🔥 Jika ada transaksi yang menunggu cetak, cetak otomatis 🔥
        if (cachedTransaksi != null) {
            cetakStruk(cachedTransaksi!!)
            cachedTransaksi = null
        }
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN
            )
            val needRequest = permissions.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (needRequest) {
                ActivityCompat.requestPermissions(this, permissions, 100)
            }
        }
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
            binding.btnScanPrinter.isEnabled = false
            return
        }
        if (!btAdapter.isEnabled) {
            binding.tvStatusBluetooth.text = "⚠️ Bluetooth tidak aktif"
            binding.btnScanPrinter.isEnabled = false
        } else {
            binding.tvStatusBluetooth.text = "✅ Bluetooth aktif"
            binding.btnScanPrinter.isEnabled = true
        }
    }

    private fun tampilkanPrinterTersimpan() {
        val mac = prefs.getString("mac_printer", "") ?: ""
        val nama = prefs.getString("nama_printer", "") ?: ""
        if (mac.isNotEmpty()) {
            binding.tvPrinterTersimpan.text = "Tersimpan: $nama"
            binding.tvStatusKoneksi.text = "✅ Terhubung ke $nama"
        }
    }

    @SuppressLint("MissingPermission")
    private fun scanPaired() {
        val paired = btAdapter?.bondedDevices
        if (paired.isNullOrEmpty()) {
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

        Thread {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream

                prefs.edit()
                    .putString("mac_printer", device.address)
                    .putString("nama_printer", device.name)
                    .apply()

                runOnUiThread {
                    binding.progressPrinter.visibility = View.GONE
                    binding.tvStatusKoneksi.text = "✅ Terhubung ke ${device.name}"
                    binding.tvPrinterTersimpan.text = "Tersimpan: ${device.name}"
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

    // 🔥 FUNGSI CETAK STRUK 🔥
    fun cetakStruk(transaksi: ModelTransaksi) {
        if (outputStream == null) {
            runOnUiThread {
                Toast.makeText(this, "❌ Printer belum terhubung", Toast.LENGTH_SHORT).show()
            }
            return
        }

        Thread {
            try {
                val struk = buatStruk(transaksi)
                outputStream?.write(struk.toByteArray(Charsets.UTF_8))
                outputStream?.flush()
                runOnUiThread {
                    Toast.makeText(this, "✅ Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "❌ Gagal cetak: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun buatStruk(transaksi: ModelTransaksi): String {
        val sb = StringBuilder()
        sb.append("\n\n")
        sb.append("============================\n")
        sb.append("      *** SELARAS POS ***\n")
        sb.append("============================\n")
        sb.append("Tanggal : ${transaksi.tanggal}\n")
        sb.append("Kasir   : ${transaksi.namaKasir}\n")
        sb.append("Cabang  : ${transaksi.cabang}\n")
        sb.append("----------------------------\n")
        sb.append("ITEM               QTY   HARGA\n")
        sb.append("----------------------------\n")

        transaksi.detailProduk.values.forEach { item ->
            val namaSingkat = if (item.namaProduk.length > 15)
                item.namaProduk.substring(0, 12) + "..."
            else item.namaProduk
            sb.append(String.format("%-16s %3d   %s\n", namaSingkat, item.qty, formatRupiah(item.subtotal)))
        }

        sb.append("----------------------------\n")
        sb.append(String.format("%-21s %s\n", "TOTAL:", formatRupiah(transaksi.total)))
        sb.append(String.format("%-21s %s\n", "BAYAR:", formatRupiah(transaksi.uangBayar)))
        sb.append(String.format("%-21s %s\n", "KEMBALI:", formatRupiah(transaksi.kembalian)))
        sb.append("----------------------------\n")
        sb.append("   Terima kasih sudah\n")
        sb.append("        berbelanja!\n")
        sb.append("============================\n\n\n")
        return sb.toString()
    }

    private fun formatRupiah(value: Long): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(value)
    }

    private fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (_: Exception) {}
        outputStream = null
        bluetoothSocket = null
        binding.tvStatusKoneksi.text = "Belum terhubung"
        binding.tvPrinterTersimpan.text = ""
        Toast.makeText(this, "Printer diputuskan", Toast.LENGTH_SHORT).show()
        scanPaired()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }
}