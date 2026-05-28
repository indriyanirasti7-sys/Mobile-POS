package com.rasti.selaraspos.produk

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.selaraspos.adapter.AdapterProduk
import com.rasti.selaraspos.model.ModelProduk
import com.rasti.selaraspos.R // Ganti sesuai package R aplikasi Anda jika perlu

class DataProdukActivity : AppCompatActivity() {

    private lateinit var rvProduk: RecyclerView
    private lateinit var etSearchProduk: EditText
    private lateinit var fabTambahProduk: ExtendedFloatingActionButton
    private lateinit var btnKembali: ImageButton
    private lateinit var progressProduk: ProgressBar
    private lateinit var tvEmptyProduk: TextView

    private val db = FirebaseDatabase.getInstance().reference.child("produk")
    private val listProduk = mutableListOf<ModelProduk>()
    private lateinit var adapter: AdapterProduk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_produk)

        rvProduk = findViewById(R.id.rvProduk)
        etSearchProduk = findViewById(R.id.etSearchProduk)
        fabTambahProduk = findViewById(R.id.fabTambahProduk)
        btnKembali = findViewById(R.id.btnKembali)
        progressProduk = findViewById(R.id.progressProduk)
        tvEmptyProduk = findViewById(R.id.tvEmptyProduk)

        setupRecyclerView()
        muatDataProduk()
        aturSearch()

        fabTambahProduk.setOnClickListener {
            startActivity(Intent(this, ModProdukActivity::class.java))
        }

        btnKembali.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = AdapterProduk(
            listProduk,
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
                tampilkanDialogHapus(produk)
            }
        )

        rvProduk.layoutManager = LinearLayoutManager(this)
        rvProduk.adapter = adapter
    }

    private fun muatDataProduk() {
        progressProduk.visibility = View.VISIBLE
        tvEmptyProduk.visibility = View.GONE

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = mutableListOf<ModelProduk>()
                for (snap in snapshot.children) {
                    snap.getValue(ModelProduk::class.java)?.let { data.add(it) }
                }

                progressProduk.visibility = View.GONE
                if (data.isEmpty()) {
                    tvEmptyProduk.visibility = View.VISIBLE
                } else {
                    tvEmptyProduk.visibility = View.GONE
                    adapter.updateData(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressProduk.visibility = View.GONE
                Toast.makeText(this@DataProdukActivity, "Gagal: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun aturSearch() {
        etSearchProduk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun tampilkanDialogHapus(produk: ModelProduk) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Produk")
            .setMessage("Yakin ingin menghapus \"${produk.namaProduk}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                db.child(produk.idProduk).removeValue()
                    .addOnSuccessListener { Toast.makeText(this, "Berhasil dihapus", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { Toast.makeText(this, "Gagal dihapus", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}