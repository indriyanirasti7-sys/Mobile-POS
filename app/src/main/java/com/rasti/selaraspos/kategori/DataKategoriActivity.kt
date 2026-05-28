package com.rasti.selaraspos.kategori

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.database.*
import com.selaraspos.adapter.AdapterKategori
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelKategori

class DataKategoriActivity : AppCompatActivity() {

    // Ganti binding dengan variabel View manual
    private lateinit var rvKategori: RecyclerView
    private lateinit var progressKategori: ProgressBar
    private lateinit var tvEmptyKategori: TextView
    private lateinit var fabTambahKategori: ExtendedFloatingActionButton
    private lateinit var btnKembali: ImageButton

    private val db = FirebaseDatabase.getInstance().reference.child("kategori")
    private val listKategori = mutableListOf<ModelKategori>()
    private lateinit var adapter: AdapterKategori

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_kategori) // Pastikan nama layout benar

        // Inisialisasi manual dengan findViewById
        rvKategori = findViewById(R.id.rvKategori)
        progressKategori = findViewById(R.id.progressKategori)
        tvEmptyKategori = findViewById(R.id.tvEmptyKategori)
        fabTambahKategori = findViewById(R.id.fabTambahKategori)
        btnKembali = findViewById(R.id.btnKembali)

        setupRecyclerView()
        muatDataKategori()

        fabTambahKategori.setOnClickListener {
            startActivity(Intent(this, ModKategoriActivity::class.java))
        }
        btnKembali.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = AdapterKategori(
            listKategori,
            onEditClick = { kategori ->
                val intent = Intent(this, ModKategoriActivity::class.java).apply {
                    putExtra("MODE", "EDIT")
                    putExtra("ID_KATEGORI", kategori.idKategori)
                    putExtra("NAMA_KATEGORI", kategori.namaKategori)
                }
                startActivity(intent)
            },
            onHapusClick = { kategori ->
                AlertDialog.Builder(this)
                    .setTitle("Hapus Kategori")
                    .setMessage("Yakin ingin menghapus kategori \"${kategori.namaKategori}\"?")
                    .setPositiveButton("Hapus") { _, _ ->
                        db.child(kategori.idKategori).removeValue()
                            .addOnSuccessListener { Toast.makeText(this, "Kategori dihapus", Toast.LENGTH_SHORT).show() }
                            .addOnFailureListener { Toast.makeText(this, "Gagal menghapus", Toast.LENGTH_SHORT).show() }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        )
        rvKategori.layoutManager = LinearLayoutManager(this)
        rvKategori.adapter = adapter
    }

    private fun muatDataKategori() {
        progressKategori.visibility = View.VISIBLE
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = mutableListOf<ModelKategori>()
                for (snap in snapshot.children) {
                    snap.getValue(ModelKategori::class.java)?.let { data.add(it) }
                }
                progressKategori.visibility = View.GONE
                tvEmptyKategori.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
                adapter.updateData(data)
            }
            override fun onCancelled(error: DatabaseError) {
                progressKategori.visibility = View.GONE
                Toast.makeText(this@DataKategoriActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}