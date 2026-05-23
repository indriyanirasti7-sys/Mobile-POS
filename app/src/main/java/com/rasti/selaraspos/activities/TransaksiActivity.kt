package com.rasti.selaraspos.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.ModelProduk
import com.rasti.selaraspos.R
import com.rasti.selaraspos.adapters.AdapterKeranjang
import com.rasti.selaraspos.adapters.AdapterProdukTransaksi
import com.rasti.selaraspos.model.ModelCabang
import com.rasti.selaraspos.model.ModelKeranjang
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransaksiActivity : AppCompatActivity() {

    // ===== VIEWS =====
    private lateinit var rvProduk: RecyclerView
    private lateinit var rvKeranjang: RecyclerView
    private lateinit var etCariProduk: EditText
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvJumlahItem: TextView
    private lateinit var tvNamaKasir: TextView
    private lateinit var btnBayar: Button
    private lateinit var spinnerCabang: Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmptyProduk: LinearLayout
    private lateinit var layoutEmptyKeranjang: LinearLayout

    // ===== DATA =====
    private val listProdukOriginal = mutableListOf<ModelProduk>()
    private val listProdukFilter = mutableListOf<ModelProduk>()
    private val listKeranjang = mutableListOf<ModelKeranjang>()
    private val listCabang = mutableListOf<ModelCabang>()
    private var namaKasir = "Kasir"
    private var idKasir = ""
    private var cabangTerpilih = ""
    private var idCabangTerpilih = ""

    // ===== ADAPTERS =====
    private lateinit var adapterProduk: AdapterProdukTransaksi
    private lateinit var adapterKeranjang: AdapterKeranjang

    // ===== FIREBASE =====
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaksi)

        initViews()
        setupAdapters()
        setupSearch()
        loadDataKasir()
        loadCabang()
        loadProduk()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        btnBayar.setOnClickListener { showDialogPembayaran() }
    }

    private fun initViews() {
        rvProduk = findViewById(R.id.rvProduk)
        rvKeranjang = findViewById(R.id.rvKeranjang)
        etCariProduk = findViewById(R.id.etCariProduk)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvTotal = findViewById(R.id.tvTotal)
        tvJumlahItem = findViewById(R.id.tvJumlahItem)
        tvNamaKasir = findViewById(R.id.tvNamaKasir)
        btnBayar = findViewById(R.id.btnBayar)
        spinnerCabang = findViewById(R.id.spinnerCabang)
        progressBar = findViewById(R.id.progressBar)
        layoutEmptyProduk = findViewById(R.id.layoutEmptyProduk)
        layoutEmptyKeranjang = findViewById(R.id.layoutEmptyKeranjang)
    }

    private fun setupAdapters() {
        adapterProduk = AdapterProdukTransaksi(listProdukFilter) { produk ->
            tambahKeKeranjang(produk)
        }
        rvProduk.layoutManager = GridLayoutManager(this, 2)
        rvProduk.adapter = adapterProduk

        adapterKeranjang = AdapterKeranjang(
            listKeranjang,
            onQtyChanged = { updateTotalUI() },
            onItemHapus = { pos -> hapusItemKeranjang(pos) }
        )
        rvKeranjang.layoutManager = LinearLayoutManager(this)
        rvKeranjang.adapter = adapterKeranjang
    }

    private fun setupSearch() {
        etCariProduk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapterProduk.filter(s.toString(), listProdukOriginal)
                updateEmptyStateProduk()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadDataKasir() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        namaKasir = prefs.getString("namaKasir", "Kasir") ?: "Kasir"
        idKasir = prefs.getString("idKasir", "") ?: ""
        tvNamaKasir.text = "Kasir: $namaKasir"
    }

    private fun loadCabang() {
        database.getReference("cabang").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                val namaCabangList = mutableListOf<String>()
                for (data in snapshot.children) {
                    val cabang = data.getValue(ModelCabang::class.java) ?: continue
                    listCabang.add(cabang)
                    namaCabangList.add(cabang.namaCabang)
                }
                val spinnerAdapter = ArrayAdapter(this@TransaksiActivity, android.R.layout.simple_spinner_item, namaCabangList)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCabang.adapter = spinnerAdapter
                spinnerCabang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                        if (listCabang.isNotEmpty()) {
                            cabangTerpilih = listCabang[pos].namaCabang
                            idCabangTerpilih = listCabang[pos].idCabang
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadProduk() {
        progressBar.visibility = View.VISIBLE
        database.getReference("produk").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProdukOriginal.clear()
                for (data in snapshot.children) {
                    val produk = data.getValue(ModelProduk::class.java) ?: continue
                    listProdukOriginal.add(produk)
                }
                adapterProduk.updateData(listProdukOriginal)
                progressBar.visibility = View.GONE
                updateEmptyStateProduk()
            }
            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@TransaksiActivity, "Gagal memuat produk", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun tambahKeKeranjang(produk: ModelProduk) {
        val existing = listKeranjang.find { it.idProduk == produk.idProduk }
        if (existing != null) {
            existing.qty++
            existing.hitungSubtotal()
            adapterKeranjang.notifyDataSetChanged()
        } else {
            val item = ModelKeranjang(produk.idProduk, produk.namaProduk, produk.hargaJual, 1, produk.hargaJual)
            listKeranjang.add(item)
            adapterKeranjang.notifyItemInserted(listKeranjang.size - 1)
        }
        updateTotalUI()
        updateEmptyStateKeranjang()
        Toast.makeText(this, "${produk.namaProduk} ditambahkan", Toast.LENGTH_SHORT).show()
    }

    private fun hapusItemKeranjang(position: Int) {
        listKeranjang.removeAt(position)
        adapterKeranjang.notifyItemRemoved(position)
        updateTotalUI()
        updateEmptyStateKeranjang()
    }

    private fun updateTotalUI() {
        val total = adapterKeranjang.hitungTotal()
        tvSubtotal.text = formatRupiah(total)
        tvTotal.text = formatRupiah(total)
        tvJumlahItem.text = "${listKeranjang.size} item"
    }

    private fun updateEmptyStateProduk() {
        layoutEmptyProduk.visibility = if (listProdukFilter.isEmpty()) View.VISIBLE else View.GONE
        rvProduk.visibility = if (listProdukFilter.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateEmptyStateKeranjang() {
        layoutEmptyKeranjang.visibility = if (listKeranjang.isEmpty()) View.VISIBLE else View.GONE
        rvKeranjang.visibility = if (listKeranjang.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showDialogPembayaran() {
        if (listKeranjang.isEmpty()) {
            Toast.makeText(this, "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_pembayaran)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.92).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvTotalDialog = dialog.findViewById<TextView>(R.id.tvTotalDialog)
        val rgMetode = dialog.findViewById<RadioGroup>(R.id.rgMetodePembayaran)
        val layoutUangBayar = dialog.findViewById<LinearLayout>(R.id.layoutUangBayar)
        val etUangBayar = dialog.findViewById<EditText>(R.id.etUangBayar)
        val layoutKembalian = dialog.findViewById<LinearLayout>(R.id.layoutKembalian)
        val tvKembalian = dialog.findViewById<TextView>(R.id.tvKembalian)
        val btnBatal = dialog.findViewById<Button>(R.id.btnBatalDialog)
        val btnKonfirmasi = dialog.findViewById<Button>(R.id.btnKonfirmasiPembayaran)

        val total = adapterKeranjang.hitungTotal()
        tvTotalDialog.text = formatRupiah(total)

        rgMetode.setOnCheckedChangeListener { _, checkedId ->
            layoutUangBayar.visibility = if (checkedId == R.id.rbCash) View.VISIBLE else View.GONE
            layoutKembalian.visibility = View.GONE
        }

        etUangBayar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val bayar = s.toString().toLongOrNull() ?: 0L
                if (bayar >= total) {
                    tvKembalian.text = formatRupiah(bayar - total)
                    layoutKembalian.visibility = View.VISIBLE
                } else {
                    layoutKembalian.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnBatal.setOnClickListener { dialog.dismiss() }
        btnKonfirmasi.setOnClickListener {
            val metode = when (rgMetode.checkedRadioButtonId) {
                R.id.rbCash -> "Cash"
                R.id.rbQris -> "QRIS"
                R.id.rbTransfer -> "Transfer"
                else -> "Cash"
            }
            dialog.dismiss()
            simpanTransaksi(metode)
        }
        dialog.show()
    }

    private fun simpanTransaksi(metode: String) {
        progressBar.visibility = View.VISIBLE
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val idTransaksi = "TRX-${sdf.format(Date())}"
        val tanggal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val total = adapterKeranjang.hitungTotal()

        val detailMap = mutableMapOf<String, Any>()
        listKeranjang.forEachIndexed { index, item ->
            detailMap["item_${index + 1}"] = mapOf("idProduk" to item.idProduk, "namaProduk" to item.namaProduk, "hargaSatuan" to item.hargaSatuan, "qty" to item.qty, "subtotal" to item.subtotal)
        }

        val transaksiMap = mapOf(
            "idTransaksi" to idTransaksi, "tanggal" to tanggal, "total" to total, "metodePembayaran" to metode,
            "namaKasir" to namaKasir, "idKasir" to idKasir, "cabang" to cabangTerpilih, "idCabang" to idCabangTerpilih, "detailProduk" to detailMap
        )

        database.getReference("transaksi/$idTransaksi").setValue(transaksiMap).addOnSuccessListener {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Transaksi berhasil!", Toast.LENGTH_SHORT).show()
            listKeranjang.clear()
            adapterKeranjang.notifyDataSetChanged()
            updateTotalUI()
            updateEmptyStateKeranjang()
        }
    }

    private fun formatRupiah(nominal: Long): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(nominal).replace("Rp", "Rp ").replace(",00", "")
    }
}