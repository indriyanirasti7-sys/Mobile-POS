package com.rasti.selaraspos.activities

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.rasti.selaraspos.R
import com.rasti.selaraspos.RoleHelper
import com.rasti.selaraspos.adapters.AdapterDetailTransaksi
import com.rasti.selaraspos.databinding.ActivityTransaksiBinding
import com.rasti.selaraspos.model.ModelKeranjang
import com.rasti.selaraspos.model.ModelProduk
import com.rasti.selaraspos.model.ModelTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransaksiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransaksiBinding
    private val db = FirebaseDatabase.getInstance().reference

    private val listProduk = mutableListOf<ModelProduk>()
    private val listKeranjang = mutableListOf<ModelKeranjang>()
    private val listCabang = mutableListOf<String>()
    private lateinit var produkAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvNamaKasir.text = RoleHelper.getNama(this)

        setupRecyclerViews()
        loadProduk()
        loadCabang()
        loadKategori()

        binding.btnKembali.setOnClickListener { finish() }

        binding.btnBayar.setOnClickListener {
            if (listKeranjang.isEmpty()) {
                Toast.makeText(this, "Keranjang kosong!", Toast.LENGTH_SHORT).show()
            } else {
                showPaymentDialog()
            }
        }
    }

    private fun setupRecyclerViews() {
        // Produk adapter dengan gambar
        produkAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_produk_transaksi, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val produk = listProduk[position]
                holder.itemView.findViewById<TextView>(R.id.tvNamaProdukTrx).text = produk.namaProduk
                holder.itemView.findViewById<TextView>(R.id.tvHargaProdukTrx).text = formatRupiah(produk.hargaJual)
                holder.itemView.findViewById<TextView>(R.id.tvStokProdukTrx).text = "Stok: ${produk.stokProduk}"

                val imgProduk = holder.itemView.findViewById<ImageView>(R.id.imgProdukTrx)
                if (produk.fotoProduk.isNotEmpty()) {
                    Glide.with(this@TransaksiActivity)
                        .load(produk.fotoProduk)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .centerCrop()
                        .into(imgProduk)
                } else {
                    imgProduk.setImageResource(R.drawable.placeholder)
                }

                holder.itemView.setOnClickListener {
                    if (produk.stokProduk > 0) {
                        addToCart(produk)
                    } else {
                        Toast.makeText(this@TransaksiActivity, "Stok habis!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun getItemCount() = listProduk.size
        }

        binding.rvProdukTrx.layoutManager = GridLayoutManager(this, 2)
        binding.rvProdukTrx.adapter = produkAdapter

        // Keranjang adapter dengan tombol +, -, hapus
        binding.rvKeranjang.layoutManager = LinearLayoutManager(this)
        binding.rvKeranjang.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_keranjang, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val item = listKeranjang[position]
                holder.itemView.findViewById<TextView>(R.id.tvNamaProdukKeranjang).text = item.namaProduk
                holder.itemView.findViewById<TextView>(R.id.tvHargaKeranjang).text = formatRupiah(item.hargaJual)
                holder.itemView.findViewById<TextView>(R.id.tvQtyKeranjang).text = item.qty.toString()
                holder.itemView.findViewById<TextView>(R.id.tvSubtotalKeranjang).text = "Subtotal: ${formatRupiah(item.subtotal)}"

                holder.itemView.findViewById<View>(R.id.btnTambahQty).setOnClickListener {
                    updateKeranjangQty(item, 1)
                }
                holder.itemView.findViewById<View>(R.id.btnKurangQty).setOnClickListener {
                    updateKeranjangQty(item, -1)
                }
                holder.itemView.findViewById<View>(R.id.btnHapusItem).setOnClickListener {
                    listKeranjang.removeAt(position)
                    notifyDataSetChanged()
                    updateTotal()
                    Toast.makeText(this@TransaksiActivity, "${item.namaProduk} dihapus", Toast.LENGTH_SHORT).show()
                }
            }

            override fun getItemCount() = listKeranjang.size
        }
    }

    private fun addToCart(produk: ModelProduk) {
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
        binding.rvKeranjang.adapter?.notifyDataSetChanged()
        updateTotal()
        Toast.makeText(this, "${produk.namaProduk} ditambahkan", Toast.LENGTH_SHORT).show()
    }

    private fun updateKeranjangQty(item: ModelKeranjang, delta: Int) {
        val index = listKeranjang.indexOfFirst { it.idProduk == item.idProduk }
        if (index != -1) {
            val newQty = listKeranjang[index].qty + delta
            if (newQty <= 0) {
                listKeranjang.removeAt(index)
            } else {
                listKeranjang[index].qty = newQty
                listKeranjang[index].subtotal = newQty * listKeranjang[index].hargaJual
            }
            binding.rvKeranjang.adapter?.notifyDataSetChanged()
            updateTotal()
        }
    }

    private fun updateTotal() {
        val total = listKeranjang.sumOf { it.subtotal }
        binding.tvTotalKeranjang.text = formatRupiah(total)
        binding.tvJumlahItem.text = "${listKeranjang.size} item"
    }

    private fun loadProduk() {
        db.child("produk").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProduk.clear()
                for (snap in snapshot.children) {
                    snap.getValue(ModelProduk::class.java)?.let { listProduk.add(it) }
                }
                binding.rvProdukTrx.adapter?.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadCabang() {
        db.child("cabang").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                for (snap in snapshot.children) {
                    snap.child("namaCabang").getValue(String::class.java)?.let { listCabang.add(it) }
                }
                if (listCabang.isNotEmpty()) {
                    binding.tvCabangTrx.text = listCabang[0]
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadKategori() {
        db.child("kategori").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.chipGroupKategori.removeAllViews()

                val chipSemua = com.google.android.material.chip.Chip(this@TransaksiActivity).apply {
                    text = "Semua"
                    isCheckable = true
                    isChecked = true
                    setOnClickListener {
                        loadProduk()
                    }
                }
                binding.chipGroupKategori.addView(chipSemua)

                for (snap in snapshot.children) {
                    val kategori = snap.child("namaKategori").getValue(String::class.java) ?: continue
                    val chip = com.google.android.material.chip.Chip(this@TransaksiActivity).apply {
                        text = kategori
                        isCheckable = true
                        setOnClickListener {
                            filterProdukByKategori(kategori)
                        }
                    }
                    binding.chipGroupKategori.addView(chip)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun filterProdukByKategori(kategori: String) {
        db.child("produk").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProduk.clear()
                for (snap in snapshot.children) {
                    val produk = snap.getValue(ModelProduk::class.java)
                    if (produk != null && produk.kategoriProduk == kategori) {
                        listProduk.add(produk)
                    }
                }
                binding.rvProdukTrx.adapter?.notifyDataSetChanged()

                if (listProduk.isEmpty()) {
                    Toast.makeText(this@TransaksiActivity, "Tidak ada produk di kategori $kategori", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showPaymentDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_pembayaran)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val total = listKeranjang.sumOf { it.subtotal }

        val tvTotal = dialog.findViewById<TextView>(R.id.tvTotalBayar)
        val actvCabang = dialog.findViewById<AutoCompleteTextView>(R.id.actvCabangBayar)
        val etUangBayar = dialog.findViewById<EditText>(R.id.etUangBayar)
        val tvKembalian = dialog.findViewById<TextView>(R.id.tvKembalian)
        val rbCash = dialog.findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.rbCash)
        val rbQris = dialog.findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.rbQris)
        val rbTransfer = dialog.findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.rbTransfer)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnKonfirmasiBayar)
        val btnCancel = dialog.findViewById<Button>(R.id.btnBatalBayar)

        tvTotal.text = formatRupiah(total)

        val cabangAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listCabang)
        actvCabang.setAdapter(cabangAdapter)
        if (listCabang.isNotEmpty()) {
            actvCabang.setText(listCabang[0], false)
        }

        etUangBayar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val bayar = s.toString().toLongOrNull() ?: 0
                val change = bayar - total
                tvKembalian.text = formatRupiah(if (change >= 0) change else 0)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnConfirm.setOnClickListener {
            val uangBayar = etUangBayar.text.toString().toLongOrNull() ?: 0

            if (rbCash.isChecked && uangBayar < total) {
                Toast.makeText(this, "Uang bayar kurang!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val metode = when {
                rbCash.isChecked -> "Cash"
                rbQris.isChecked -> "QRIS"
                rbTransfer.isChecked -> "Transfer"
                else -> "Cash"
            }

            val cabang = actvCabang.text.toString().trim()
            if (cabang.isEmpty()) {
                Toast.makeText(this, "Pilih cabang!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveTransaction(total, uangBayar, metode, cabang, dialog)
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun saveTransaction(total: Long, uangBayar: Long, metode: String, cabang: String, dialog: Dialog) {
        val idTrx = db.child("transaksi").push().key ?: return
        val now = System.currentTimeMillis()
        val tanggal = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(now))
        val kembalian = if (metode == "Cash") (uangBayar - total).coerceAtLeast(0) else 0
        val namaKasir = RoleHelper.getNama(this)

        val detailMap = mutableMapOf<String, com.rasti.selaraspos.model.ModelDetailTransaksi>()
        listKeranjang.forEach { item ->
            detailMap[item.idProduk] = com.rasti.selaraspos.model.ModelDetailTransaksi(
                item.idProduk, item.namaProduk, item.hargaJual, item.qty, item.subtotal
            )
        }

        val transaksi = ModelTransaksi(
            idTransaksi = idTrx,
            tanggal = tanggal,
            timestamp = now,
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

                tampilkanDialogSukses(transaksi)

                listKeranjang.clear()
                binding.rvKeranjang.adapter?.notifyDataSetChanged()
                updateTotal()
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun kurangiStok() {
        val salinan = listKeranjang.map { it.copy() }
        salinan.forEach { item ->
            db.child("produk").child(item.idProduk).child("stokProduk")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val stokLama = snapshot.getValue(Int::class.java) ?: 0
                        snapshot.ref.setValue((stokLama - item.qty).coerceAtLeast(0))
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun tampilkanDialogSukses(transaksi: ModelTransaksi) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_transaksi_sukses)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)

        dialog.findViewById<TextView>(R.id.tvDetailId).text = "#${transaksi.idTransaksi.takeLast(8)}"
        dialog.findViewById<TextView>(R.id.tvDetailTanggal).text = transaksi.tanggal
        dialog.findViewById<TextView>(R.id.tvDetailKasir).text = transaksi.namaKasir
        dialog.findViewById<TextView>(R.id.tvDetailCabang).text = transaksi.cabang
        dialog.findViewById<TextView>(R.id.tvDetailMetode).text = transaksi.metodePembayaran
        dialog.findViewById<TextView>(R.id.tvDetailTotal).text = formatRupiah(transaksi.total)
        dialog.findViewById<TextView>(R.id.tvDetailBayar).text = formatRupiah(transaksi.uangBayar)
        dialog.findViewById<TextView>(R.id.tvDetailKembali).text = formatRupiah(transaksi.kembalian)

        val rvDetailProduk = dialog.findViewById<RecyclerView>(R.id.rvDetailProduk)
        val detailList = transaksi.detailProduk.values.toList()
        rvDetailProduk.layoutManager = LinearLayoutManager(this)
        rvDetailProduk.adapter = AdapterDetailTransaksi(detailList)

        dialog.findViewById<Button>(R.id.btnShareTransaksi).setOnClickListener {
            shareTransaksi(transaksi)
        }

        // 🔥 PRINT LANGSUNG (TANPA BUKA PrinterActivity) 🔥
        dialog.findViewById<Button>(R.id.btnPrintTransaksi).setOnClickListener {
            cetakStrukLangsung(transaksi)
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnCloseDialog).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // 🔥 FUNGSI CETAK STRUK LANGSUNG 🔥
    private fun cetakStrukLangsung(transaksi: ModelTransaksi) {
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

        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        val device = btAdapter?.getRemoteDevice(macAddress)

        if (device == null) {
            Toast.makeText(this, "Printer tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "🖨️ Mencetak struk...", Toast.LENGTH_SHORT).show()

        Thread {
            try {
                val socket = device.createRfcommSocketToServiceRecord(
                    java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                )
                socket.connect()
                val os = socket.outputStream

                val struk = buatStrukPrint(transaksi)
                os.write(struk.toByteArray(Charsets.UTF_8))
                os.flush()
                os.close()
                socket.close()

                runOnUiThread {
                    Toast.makeText(this, "✅ Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "❌ Gagal mencetak: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun buatStrukPrint(transaksi: ModelTransaksi): String {
        val sb = StringBuilder()
        sb.append("\n\n")
        sb.append("============================\n")
        sb.append("      *** SELARAS POS ***\n")
        sb.append("============================\n")
        sb.append("ID     : #${transaksi.idTransaksi.takeLast(8)}\n")
        sb.append("Tanggal: ${transaksi.tanggal}\n")
        sb.append("Kasir  : ${transaksi.namaKasir}\n")
        sb.append("Cabang : ${transaksi.cabang}\n")
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
        sb.append("Metode: ${transaksi.metodePembayaran}\n")
        sb.append("\n   Terima kasih sudah\n")
        sb.append("        berbelanja!\n")
        sb.append("============================\n\n\n")
        return sb.toString()
    }

    private fun shareTransaksi(transaksi: ModelTransaksi) {
        val sb = StringBuilder()
        sb.append("═══════════════════════════════════\n")
        sb.append("        🧾 STRUK BELANJA\n")
        sb.append("           SELARAS POS\n")
        sb.append("═══════════════════════════════════\n\n")
        sb.append("ID: #${transaksi.idTransaksi.takeLast(8)}\n")
        sb.append("Tgl: ${transaksi.tanggal}\n")
        sb.append("Kasir: ${transaksi.namaKasir}\n")
        sb.append("Cabang: ${transaksi.cabang}\n")
        sb.append("───────────────────────────────────\n")
        sb.append("ITEM                    QTY   HARGA\n")
        sb.append("───────────────────────────────────\n")

        transaksi.detailProduk.values.forEach { item ->
            val namaSingkat = if (item.namaProduk.length > 18)
                item.namaProduk.substring(0, 15) + "..."
            else item.namaProduk
            sb.append(String.format("%-20s %3d   %s\n", namaSingkat, item.qty, formatRupiah(item.subtotal)))
        }

        sb.append("───────────────────────────────────\n")
        sb.append(String.format("%-23s %s\n", "TOTAL:", formatRupiah(transaksi.total)))
        sb.append(String.format("%-23s %s\n", "BAYAR:", formatRupiah(transaksi.uangBayar)))
        sb.append(String.format("%-23s %s\n", "KEMBALI:", formatRupiah(transaksi.kembalian)))
        sb.append("───────────────────────────────────\n")
        sb.append("Metode: ${transaksi.metodePembayaran}\n")
        sb.append("\n     Terima kasih sudah berbelanja!\n")
        sb.append("         🤍 Selaras POS 🤍\n")
        sb.append("═══════════════════════════════════\n")

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Bagikan Struk via"))
    }

    private fun formatRupiah(value: Long): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(value)
    }
}