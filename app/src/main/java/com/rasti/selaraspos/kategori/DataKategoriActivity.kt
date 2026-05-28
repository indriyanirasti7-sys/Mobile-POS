package com.rasti.selaraspos.kategori

import android.content.Intent
import android.os.Bundle
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
import com.rasti.selaraspos.adapters.AdapterKategori
import com.rasti.selaraspos.databinding.ActivityDataKategoriBinding
import com.rasti.selaraspos.model.ModelKategori

/**
 * DataKategoriActivity
 * Semua user bisa lihat, hanya admin yang bisa CRUD
 */
class DataKategoriActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataKategoriBinding
    private val db = FirebaseDatabase.getInstance().reference.child("kategori")
    private val listKategori = mutableListOf<ModelKategori>()
    private lateinit var adapter: AdapterKategori
    private val isAdmin by lazy { RoleHelper.isAdmin(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataKategoriBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        muatData()

        // FAB hanya untuk admin
        binding.fabTambahKategori.visibility = if (isAdmin) View.VISIBLE else View.GONE
        binding.fabTambahKategori.setOnClickListener {
            startActivity(Intent(this, ModKategoriActivity::class.java))
        }

        binding.btnKembali.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = AdapterKategori(
            listKategori,
            isAdmin = isAdmin,
            onEditClick = { kat ->
                startActivity(Intent(this, ModKategoriActivity::class.java).apply {
                    putExtra("MODE", "EDIT")
                    putExtra("ID_KATEGORI", kat.idKategori)
                    putExtra("NAMA_KATEGORI", kat.namaKategori)
                })
            },
            onHapusClick = { kat ->
                AlertDialog.Builder(this)
                    .setTitle("Hapus Kategori")
                    .setMessage("Yakin hapus \"${kat.namaKategori}\"?")
                    .setPositiveButton("Hapus") { _, _ ->
                        db.child(kat.idKategori).removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Kategori dihapus", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Batal", null).show()
            }
        )
        binding.rvKategori.layoutManager = LinearLayoutManager(this)
        binding.rvKategori.adapter = adapter
    }

    private fun muatData() {
        binding.progressKategori.visibility = View.VISIBLE
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = mutableListOf<ModelKategori>()
                for (snap in snapshot.children) {
                    snap.getValue(ModelKategori::class.java)?.let { data.add(it) }
                }
                binding.progressKategori.visibility = View.GONE
                binding.tvEmptyKategori.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
                adapter.updateData(data)
            }
            override fun onCancelled(e: DatabaseError) {
                binding.progressKategori.visibility = View.GONE
            }
        })
    }
}