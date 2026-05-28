package com.rasti.selaraspos.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.R
import com.rasti.selaraspos.RoleHelper
import com.rasti.selaraspos.adapters.AdapterKeranjang
import com.rasti.selaraspos.adapters.AdapterProdukTransaksi
import com.rasti.selaraspos.databinding.ActivityTransaksiBinding
import com.rasti.selaraspos.databinding.DialogPembayaranBinding
import com.rasti.selaraspos.model.ModelDetailTransaksi
import com.rasti.selaraspos.model.ModelKeranjang
import com.rasti.selaraspos.model.ModelProduk
import com.rasti.selaraspos.model.ModelTransaksi
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * TransaksiActivity - Halaman POS
 * Setelah transaksi berhasil disimpan ke Firebase, data otomatis muncul
 * di "Transaksi Terbaru" di dashboard karena menggunakan ValueEventListener.
 */
class TransaksiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransaksiBinding
    private val db = FirebaseDatabase.getInstance().reference

    private val listProduk = mutableListOf<ModelProduk>()
    private val listKeranjang = mutableListOf<ModelKeranjang>()
    private val listCabang = mutableListOf<String>()

    private lateinit var adapterProduk: AdapterProdukTransaksi
    private lateinit var adapterKeranjang: AdapterKeranjang

    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tampilkan nama kasir dari SharedPreferences
        binding.tvNamaKasir.text = RoleHelper.getNama(this)

        setupRecyclerViews()
        muatProduk()
        muatKategori()
        muatCabang()

        binding.etSearchTrx.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {
                adapterProduk.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnBayar.setOnClickListener {
            if (listKeranjang.isEmpty()) {
                Toast.makeText(this, "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
            } else {
                tampilkanDialogPembayaran()
            }
        }

        binding.btnKembali.setOnClickListener { finish() }
    }

    private fun setupRecyclerViews() {
        adapterProduk = AdapterProdukTransaksi(listProduk) { produk -> tambahKeKeranjang(produk) }
        binding.rvProdukTrx.layoutManager = GridLayoutManager(this, 2)
        binding.rvProdukTrx.adapter = adapterProduk

        adapterKeranjang = AdapterKeranjang(
            listKeranjang,
            onQtyChange = { item, delta -> ubahQty(item, delta) },
            onHapus = { item -> hapusItem(item) }
        )
        binding.rvKeranjang.layoutManager = LinearLayoutManager(this)
        binding.rvKeranjang.adapter = adapterKeranjang
    }

    private fun muatProduk() {
        db.child("produk").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = mutableListOf<ModelProduk>()
                for (snap in snapshot.children) {
                    snap.getValue(ModelProduk::class.java)?.let { data.add(it) }
                }
                adapterProduk.updateData(data)
                binding.tvEmptyProdukTrx.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    private fun muatKategori() {
        db.child("kategori").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.chipGroupKategori.removeAllViews()
                val chips = mutableListOf("Semua")
                for (snap in snapshot.children) {
                    snap.child("namaKategori").getValue(String::class.java)?.let { chips.add(it) }
                }
                chips.forEach { kat ->
                    val chip = com.google.android.material.chip.Chip(this@TransaksiActivity).apply {
                        text = kat
                        isCheckable = true
                        if (kat == "Semua") isChecked = true
                        setOnClickListener { adapterProduk.filterKategori(kat) }
                    }
                    binding.chipGroupKategori.addView(chip)
                }
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    private fun muatCabang() {
        db.child("cabang").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                for (snap in snapshot.children) {
                    snap.child("namaCabang").getValue(String::class.java)?.let { listCabang.add(it) }
                }
                if (listCabang.isNotEmpty()) binding.tvCabangTrx.text = listCabang[0]
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    private fun tambahKeKeranjang(produk: ModelProduk) {
        val existing = listKeranjang.find { it.idProduk == produk.idProduk }
        if (existing != null) {
            existing.qty += 1
            existing.subtotal = existing.qty * existing.hargaJual
        } else {
            listKeranjang.add(ModelKeranjang(
                idProduk = produk.idProduk,
                namaProduk = produk.namaProduk,
                hargaJual = produk.hargaJual,
                qty = 1,
                subtotal = produk.hargaJual,
                fotoProduk = produk.fotoProduk
            ))
        }
        adapterKeranjang.updateData(listKeranjang)
        hitungTotal()
        Toast.makeText(this, "${produk.namaProduk} ditambahkan", Toast.LENGTH_SHORT).show()
    }

    private fun ubahQty(item: ModelKeranjang, delta: Int) {
        val idx = listKeranjang.indexOfFirst { it.idProduk == item.idProduk }
        if (idx == -1) return
        val newQty = listKeranjang[idx].qty + delta
        if (newQty <= 0) listKeranjang.removeAt(idx)
        else {
            listKeranjang[idx].qty = newQty
            listKeranjang[idx].subtotal = newQty * listKeranjang[idx].hargaJual
        }
        adapterKeranjang.updateData(listKeranjang)
        hitungTotal()
    }

    private fun hapusItem(item: ModelKeranjang) {
        listKeranjang.removeAll { it.idProduk == item.idProduk }
        adapterKeranjang.updateData(listKeranjang)
        hitungTotal()
    }

    private fun hitungTotal() {
        val total = listKeranjang.sumOf { it.subtotal }
        binding.tvTotalKeranjang.text = formatRp(total)
        binding.tvJumlahItem.text = "${listKeranjang.size} item"
    }

    private fun tampilkanDialogPembayaran() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dBinding = DialogPembayaranBinding.inflate(layoutInflater)
        dialog.setContentView(dBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val total = listKeranjang.sumOf { it.subtotal }
        dBinding.tvTotalBayar.text = formatRp(total)

        // Isi dropdown cabang
        val adpCabang = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listCabang)
        dBinding.actvCabangBayar.setAdapter(adpCabang)
        if (listCabang.isNotEmpty()) dBinding.actvCabangBayar.setText(listCabang[0], false)

        // Hitung kembalian real-time
        dBinding.etUangBayar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {
                val bayar = s.toString().toLongOrNull() ?: 0L
                val kembalian = bayar - total
                dBinding.tvKembalian.text = formatRp(if (kembalian >= 0) kembalian else 0L)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        dBinding.btnKonfirmasiBayar.setOnClickListener {
            val metode = when (dBinding.rgMetodePembayaran.checkedRadioButtonId) {
                dBinding.rbCash.id -> "Cash"
                dBinding.rbQris.id -> "QRIS"
                dBinding.rbTransfer.id -> "Transfer"
                else -> "Cash"
            }
            val uangBayar = dBinding.etUangBayar.text.toString().toLongOrNull() ?: 0L
            val cabang = dBinding.actvCabangBayar.text.toString().trim()

            if (metode == "Cash" && uangBayar < total) {
                Toast.makeText(this, "Uang bayar kurang!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cabang.isEmpty()) {
                Toast.makeText(this, "Pilih cabang terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            simpanTransaksi(metode, uangBayar, total, cabang, dialog)
        }

        dBinding.btnBatalBayar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    /**
     * Simpan transaksi ke Firebase.
     * Field 'timestamp' diisi System.currentTimeMillis() agar bisa diurutkan
     * di Dashboard "Transaksi Terbaru" dengan .orderByChild("timestamp").limitToLast(5)
     */
    private fun simpanTransaksi(metode: String, uangBayar: Long, total: Long, cabang: String, dialog: Dialog) {
        val idTrx = db.child("transaksi").push().key ?: return
        val now = System.currentTimeMillis()
        val tanggal = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(now))
        val kembalian = if (metode == "Cash") (uangBayar - total).coerceAtLeast(0L) else 0L
        val namaKasir = RoleHelper.getNama(this)

        val detailMap = mutableMapOf<String, ModelDetailTransaksi>()
        listKeranjang.forEach { item ->
            detailMap[item.idProduk] = ModelDetailTransaksi(
                item.idProduk, item.namaProduk, item.hargaJual, item.qty, item.subtotal
            )
        }

        val transaksi = ModelTransaksi(
            idTransaksi = idTrx,
            tanggal = tanggal,
            timestamp = now,          // ← kunci untuk sorting "terbaru" di dashboard
            total = total,
            metodePembayaran = metode,
            uangBayar = uangBayar,
            kembalian = kembalian,
            namaKasir = namaKasir,
            cabang = cabang,
            detailProduk = detailMap
        )

        db.child("transaksi").child(idTrx).setValue(transaksi)
            .addOnSuccessListener {
                kurangiStok()
                dialog.dismiss()
                Toast.makeText(this, "✅ Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                printStruk(transaksi)
                listKeranjang.clear()
                adapterKeranjang.updateData(listKeranjang)
                hitungTotal()
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun kurangiStok() {
        // Simpan salinan lokal untuk thread
        val salinan = listKeranjang.map { it.copy() }
        salinan.forEach { item ->
            db.child("produk").child(item.idProduk).child("stokProduk")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val stokLama = snapshot.getValue(Int::class.java) ?: 0
                        snapshot.ref.setValue((stokLama - item.qty).coerceAtLeast(0))
                    }
                    override fun onCancelled(e: DatabaseError) {}
                })
        }
    }

    @SuppressLint("MissingPermission")
    private fun printStruk(trx: ModelTransaksi) {
        try {
            val btAdapter = BluetoothAdapter.getDefaultAdapter() ?: return
            if (!btAdapter.isEnabled) return
            val prefs = getSharedPreferences("printer_prefs", MODE_PRIVATE)
            val mac = prefs.getString("mac_printer", "") ?: return
            if (mac.isEmpty()) return

            Thread {
                try {
                    val device = btAdapter.getRemoteDevice(mac)
                    val socket = device.createRfcommSocketToServiceRecord(
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                    )
                    socket.connect()
                    val os = socket.outputStream
                    os.write(buatStruk(trx).toByteArray(Charsets.UTF_8))
                    os.flush(); os.close(); socket.close()
                    runOnUiThread { Toast.makeText(this, "🖨️ Struk dicetak!", Toast.LENGTH_SHORT).show() }
                } catch (_: Exception) {}
            }.start()
        } catch (_: Exception) {}
    }

    private fun buatStruk(trx: ModelTransaksi): String {
        val sb = StringBuilder()
        sb.append("\n")
        sb.append("   *** SELARAS POS ***\n")
        sb.append("     Toko Serba Ada\n")
        sb.append("================================\n")
        sb.append("ID  : ${trx.idTransaksi.takeLast(8)}\n")
        sb.append("Tgl : ${trx.tanggal}\n")
        sb.append("Kas : ${trx.namaKasir}\n")
        sb.append("Cab : ${trx.cabang}\n")
        sb.append("--------------------------------\n")
        trx.detailProduk.values.forEach { item ->
            sb.append("${item.namaProduk}\n")
            sb.append("  ${item.qty} x ${formatRp(item.hargaJual)}\n")
            sb.append("  = ${formatRp(item.subtotal)}\n")
        }
        sb.append("--------------------------------\n")
        sb.append("TOTAL   : ${formatRp(trx.total)}\n")
        sb.append("BAYAR   : ${formatRp(trx.uangBayar)}\n")
        sb.append("KEMBALI : ${formatRp(trx.kembalian)}\n")
        sb.append("METODE  : ${trx.metodePembayaran}\n")
        sb.append("================================\n")
        sb.append("  Terima kasih sudah belanja!\n")
        sb.append("   Sampai jumpa kembali :)\n")
        sb.append("\n\n\n")
        return sb.toString()
    }

    private fun formatRp(h: Long) = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(h)
}