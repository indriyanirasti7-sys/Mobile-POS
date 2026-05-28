package com.rasti.selaraspos.cabang

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

class DataCabangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataCabangBinding
    private val db = FirebaseDatabase.getInstance().reference.child("cabang")
    private val list = mutableListOf<ModelCabang>()
    private lateinit var adapter: AdapterCabang

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataCabangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AdapterCabang(list,
            onEdit = { c ->
                // Perbaikan di sini: Hapus "com.rasti.selaraspos.activities."
                startActivity(Intent(this, ModCabangActivity::class.java).apply {
                    putExtra("MODE", "EDIT")
                    putExtra("ID_CABANG", c.idCabang)
                    putExtra("NAMA_CABANG", c.namaCabang)
                    putExtra("ALAMAT_CABANG", c.alamatCabang)
                    putExtra("TELEPON_CABANG", c.teleponCabang)
                    putExtra("PJ_CABANG", c.penanggungjawab)
                })
            },
            onHapus = { c ->
                AlertDialog.Builder(this).setTitle("Hapus Cabang")
                    .setMessage("Yakin hapus \"${c.namaCabang}\"?")
                    .setPositiveButton("Hapus") { _, _ -> db.child(c.idCabang).removeValue() }
                    .setNegativeButton("Batal", null).show()
            }
        )
        binding.rvCabang.layoutManager = LinearLayoutManager(this)
        binding.rvCabang.adapter = adapter

        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = mutableListOf<ModelCabang>()
                for (snap in snapshot.children) {
                    snap.getValue(ModelCabang::class.java)?.let { data.add(it) }
                }
                binding.progressCabang.visibility = View.GONE
                binding.tvEmptyCabang.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
                adapter.updateData(data)
            }
            override fun onCancelled(e: DatabaseError) {}
        })

        binding.fabTambahCabang.setOnClickListener {
            startActivity(Intent(this, ModCabangActivity::class.java))
        }
        binding.btnKembali.setOnClickListener { finish() }
    }
}