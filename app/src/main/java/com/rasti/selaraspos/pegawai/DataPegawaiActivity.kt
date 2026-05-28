package com.rasti.selaraspos.pegawai

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
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
import com.rasti.selaraspos.adapters.AdapterCabang
import com.rasti.selaraspos.adapters.AdapterPegawai
import com.rasti.selaraspos.databinding.ActivityDataCabangBinding
import com.rasti.selaraspos.databinding.ActivityDataPegawaiBinding
import com.rasti.selaraspos.databinding.ActivityModCabangBinding
import com.rasti.selaraspos.databinding.ActivityModPegawaiBinding
import com.rasti.selaraspos.databinding.ActivityPrinterBinding
import com.rasti.selaraspos.model.ModelCabang
import com.rasti.selaraspos.model.ModelPegawai
import java.io.OutputStream
import java.util.UUID

class DataPegawaiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataPegawaiBinding
    private val db = FirebaseDatabase.getInstance().reference.child("pegawai")
    private val list = mutableListOf<ModelPegawai>()
    private lateinit var adapter: AdapterPegawai

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataPegawaiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AdapterPegawai(list,
            onEdit = { p ->
                // Perbaikan di sini: Hapus "com.rasti.selaraspos.activities."
                startActivity(Intent(this, ModPegawaiActivity::class.java).apply {
                    putExtra("MODE", "EDIT")
                    putExtra("ID_PEGAWAI", p.idPegawai)
                    putExtra("NAMA_PEGAWAI", p.namaPegawai)
                    putExtra("ROLE", p.role)
                    putExtra("NO_HP", p.noHp)
                    putExtra("ALAMAT", p.alamat)
                    putExtra("EMAIL", p.email)
                })
            },
            onHapus = { p ->
                AlertDialog.Builder(this).setTitle("Hapus Pegawai")
                    .setMessage("Yakin hapus \"${p.namaPegawai}\"?")
                    .setPositiveButton("Hapus") { _, _ -> db.child(p.idPegawai).removeValue() }
                    .setNegativeButton("Batal", null).show()
            }
        )
        binding.rvPegawai.layoutManager = LinearLayoutManager(this)
        binding.rvPegawai.adapter = adapter

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = mutableListOf<ModelPegawai>()
                for (snap in snapshot.children) {
                    snap.getValue(ModelPegawai::class.java)?.let { data.add(it) }
                }
                binding.progressPegawai.visibility = View.GONE
                binding.tvEmptyPegawai.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
                adapter.updateData(data)
            }
            override fun onCancelled(e: DatabaseError) {}
        })

        binding.fabTambahPegawai.setOnClickListener {
            startActivity(Intent(this, ModPegawaiActivity::class.java))
        }
        binding.btnKembali.setOnClickListener { finish() }
    }
}