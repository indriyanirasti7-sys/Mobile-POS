package com.rasti.selaraspos.produk

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.R
import com.rasti.selaraspos.RoleHelper
import com.rasti.selaraspos.adapters.AdapterProduk
import com.rasti.selaraspos.databinding.ActivityDataProdukBinding
import com.rasti.selaraspos.model.ModelProduk

/**
 * DataProdukActivity
 * - Semua user bisa LIHAT produk
 * - Hanya ADMIN yang bisa Tambah / Edit / Hapus
 */
class DataProdukActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataProdukBinding
    private val db = FirebaseDatabase.getInstance().reference.child("produk")
    private val listProduk = mutableListOf<ModelProduk>()
    private lateinit var adapter: AdapterProduk
    private val isAdmin by lazy { RoleHelper.isAdmin(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataProdukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        muatData()
        aturSearch()

        // FAB tambah hanya tampil untuk admin
        binding.fabTambahProduk.visibility = if (isAdmin) View.VISIBLE else View.GONE
        binding.fabTambahProduk.setOnClickListener {
            startActivity(Intent(this, ModProdukActivity::class.java))
        }

        binding.btnKembali.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = AdapterProduk(
            listProduk,
            isAdmin = isAdmin,
            onEditClick = { produk ->
                val intent = Intent(this, ModProdukActivity::class.java).apply {
                    putExtra("MODE", "EDIT")
                    putExtra("ID_PRODUK", produk.idProduk)
                    putExtra("NAMA_PRODUK", produk.namaProduk)
                    putExtra("HARGA_JUAL", produk.hargaJual)
                    putExtra("STOK_PRODUK", produk.stokProduk)
                    putExtra("KATEGORI_PRODUK", produk.kategoriProduk)
                    putExtra("FOTO_PRODUK", produk.fotoProduk)
                }
                startActivity(intent)
            },
            onHapusClick = { produk ->
                AlertDialog.Builder(this)
                    .setTitle("Hapus Produk")
                    .setMessage("Yakin hapus \"${produk.namaProduk}\"?")
                    .setPositiveButton("Hapus") { _, _ ->
                        db.child(produk.idProduk).removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Produk dihapus", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        )
        binding.rvProduk.layoutManager = LinearLayoutManager(this)
        binding.rvProduk.adapter = adapter
    }

    private fun muatData() {
        binding.progressProduk.visibility = View.VISIBLE
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = mutableListOf<ModelProduk>()
                for (snap in snapshot.children) {
                    snap.getValue(ModelProduk::class.java)?.let { data.add(it) }
                }
                binding.progressProduk.visibility = View.GONE
                binding.tvEmptyProduk.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
                adapter.updateData(data)
            }
            override fun onCancelled(e: DatabaseError) {
                binding.progressProduk.visibility = View.GONE
                Toast.makeText(this@DataProdukActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun aturSearch() {
        binding.etSearchProduk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { adapter.filter(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}