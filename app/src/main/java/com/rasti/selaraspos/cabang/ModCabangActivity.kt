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
class ModCabangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModCabangBinding
    private val db = FirebaseDatabase.getInstance().reference.child("cabang")
    private var mode = "TAMBAH"; private var idEdit = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModCabangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = intent.getStringExtra("MODE") ?: "TAMBAH"
        binding.tvJudulModCabang.text = if (mode == "EDIT") "Edit Cabang" else "Tambah Cabang"

        if (mode == "EDIT") {
            idEdit = intent.getStringExtra("ID_CABANG") ?: ""
            binding.etNamaCabang.setText(intent.getStringExtra("NAMA_CABANG") ?: "")
            binding.etAlamatCabang.setText(intent.getStringExtra("ALAMAT_CABANG") ?: "")
            binding.etTeleponCabang.setText(intent.getStringExtra("TELEPON_CABANG") ?: "")
            binding.etPjCabang.setText(intent.getStringExtra("PJ_CABANG") ?: "")
        }

        binding.btnSimpanCabang.setOnClickListener { simpan() }
        binding.btnKembali.setOnClickListener { finish() }
    }

    private fun simpan() {
        val nama = binding.etNamaCabang.text.toString().trim()
        val alamat = binding.etAlamatCabang.text.toString().trim()
        val telepon = binding.etTeleponCabang.text.toString().trim()
        val pj = binding.etPjCabang.text.toString().trim()
        if (nama.isEmpty()) { binding.tilNamaCabang.error = "Wajib diisi"; return }
        binding.tilNamaCabang.error = null
        binding.progressSimpanCabang.visibility = View.VISIBLE; binding.btnSimpanCabang.isEnabled = false

        if (mode == "TAMBAH") {
            val id = db.push().key ?: return
            db.child(id).setValue(ModelCabang(id, nama, alamat, telepon, pj))
                .addOnSuccessListener { binding.progressSimpanCabang.visibility = View.GONE; Toast.makeText(this, "✅ Cabang ditambahkan!", Toast.LENGTH_SHORT).show(); finish() }
                .addOnFailureListener { binding.progressSimpanCabang.visibility = View.GONE; binding.btnSimpanCabang.isEnabled = true }
        } else {
            db.child(idEdit).updateChildren(mapOf("namaCabang" to nama, "alamatCabang" to alamat, "teleponCabang" to telepon, "penanggungjawab" to pj))
                .addOnSuccessListener { binding.progressSimpanCabang.visibility = View.GONE; Toast.makeText(this, "✅ Cabang diperbarui!", Toast.LENGTH_SHORT).show(); finish() }
                .addOnFailureListener { binding.progressSimpanCabang.visibility = View.GONE; binding.btnSimpanCabang.isEnabled = true }
        }
    }
}