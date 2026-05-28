package com.rasti.selaraspos.pelanggan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.rasti.selaraspos.adapters.AdapterPelanggan
import com.rasti.selaraspos.databinding.ActivityDataPelangganBinding
import com.rasti.selaraspos.model.ModelPelanggan

class DataPelangganActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataPelangganBinding
    private val db = FirebaseDatabase.getInstance().reference.child("pelanggan")
    private val list = mutableListOf<ModelPelanggan>()
    private lateinit var adapter: AdapterPelanggan
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataPelangganBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val role = prefs.getString("role", "kasir") ?: "kasir"
        isAdmin = role == "admin"

        if (!isAdmin) {
            binding.fabTambahPelanggan.visibility = View.GONE
        }

        adapter = AdapterPelanggan(list, isAdmin,
            onEdit = { p ->
                if (isAdmin) {
                    startActivity(Intent(this, ModPelangganActivity::class.java).apply {
                        putExtra("MODE", "EDIT")
                        putExtra("ID_PELANGGAN", p.idPelanggan)
                        putExtra("NAMA_PELANGGAN", p.namaPelanggan)
                        putExtra("TELEPON_PELANGGAN", p.teleponPelanggan)
                        putExtra("ALAMAT_PELANGGAN", p.alamatPelanggan)
                        putExtra("EMAIL_PELANGGAN", p.emailPelanggan)
                        putExtra("POINT", p.point)
                    })
                } else {
                    Toast.makeText(this, "Hanya admin yang bisa mengedit", Toast.LENGTH_SHORT).show()
                }
            },
            onHapus = { p ->
                if (isAdmin) {
                    AlertDialog.Builder(this)
                        .setTitle("Hapus Pelanggan")
                        .setMessage("Yakin hapus \"${p.namaPelanggan}\"?")
                        .setPositiveButton("Hapus") { _, _ -> db.child(p.idPelanggan).removeValue() }
                        .setNegativeButton("Batal", null)
                        .show()
                } else {
                    Toast.makeText(this, "Hanya admin yang bisa menghapus", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.rvPelanggan.layoutManager = LinearLayoutManager(this)
        binding.rvPelanggan.adapter = adapter

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = mutableListOf<ModelPelanggan>()
                for (snap in snapshot.children) {
                    snap.getValue(ModelPelanggan::class.java)?.let { data.add(it) }
                }
                binding.progressPelanggan.visibility = View.GONE
                binding.tvEmptyPelanggan.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
                adapter.updateData(data)
            }
            override fun onCancelled(e: DatabaseError) {}
        })

        binding.fabTambahPelanggan.setOnClickListener {
            if (isAdmin) {
                startActivity(Intent(this, ModPelangganActivity::class.java))
            } else {
                Toast.makeText(this, "Hanya admin yang bisa menambah", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnKembali.setOnClickListener { finish() }
    }
}