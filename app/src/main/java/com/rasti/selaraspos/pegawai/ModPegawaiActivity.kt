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

class ModPegawaiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModPegawaiBinding
    private val db = FirebaseDatabase.getInstance().reference.child("pegawai")
    private var mode = "TAMBAH"; private var idEdit = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModPegawaiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roles = listOf("admin", "kasir")
        binding.actvRole.setAdapter(android.widget.ArrayAdapter(this,
            android.R.layout.simple_dropdown_item_1line, roles))

        mode = intent.getStringExtra("MODE") ?: "TAMBAH"
        binding.tvJudulModPegawai.text = if (mode == "EDIT") "Edit Pegawai" else "Tambah Pegawai"

        if (mode == "EDIT") {
            idEdit = intent.getStringExtra("ID_PEGAWAI") ?: ""
            binding.etNamaPegawai.setText(intent.getStringExtra("NAMA_PEGAWAI") ?: "")
            binding.actvRole.setText(intent.getStringExtra("ROLE") ?: "kasir", false)
            binding.etNoHp.setText(intent.getStringExtra("NO_HP") ?: "")
            binding.etAlamatPegawai.setText(intent.getStringExtra("ALAMAT") ?: "")
            binding.etEmailPegawai.setText(intent.getStringExtra("EMAIL") ?: "")
            binding.tilEmailPegawai.isEnabled = false
        } else binding.actvRole.setText("kasir", false)

        binding.btnSimpanPegawai.setOnClickListener { simpan() }
        binding.btnKembali.setOnClickListener { finish() }
    }

    private fun simpan() {
        val nama = binding.etNamaPegawai.text.toString().trim()
        val role = binding.actvRole.text.toString().trim()
        val noHp = binding.etNoHp.text.toString().trim()
        val alamat = binding.etAlamatPegawai.text.toString().trim()
        val email = binding.etEmailPegawai.text.toString().trim()
        if (nama.isEmpty()) { binding.tilNamaPegawai.error = "Wajib diisi"; return }
        if (role.isEmpty()) { binding.tilRole.error = "Wajib dipilih"; return }
        binding.tilNamaPegawai.error = null; binding.tilRole.error = null
        binding.progressSimpanPegawai.visibility = View.VISIBLE; binding.btnSimpanPegawai.isEnabled = false

        if (mode == "TAMBAH") {
            val id = db.push().key ?: return
            db.child(id).setValue(ModelPegawai(id, nama, role, noHp, alamat, email))
                .addOnSuccessListener { binding.progressSimpanPegawai.visibility = View.GONE; Toast.makeText(this, "✅ Pegawai ditambahkan!", Toast.LENGTH_SHORT).show(); finish() }
                .addOnFailureListener { binding.progressSimpanPegawai.visibility = View.GONE; binding.btnSimpanPegawai.isEnabled = true }
        } else {
            db.child(idEdit).updateChildren(mapOf("namaPegawai" to nama, "role" to role, "noHp" to noHp, "alamat" to alamat))
                .addOnSuccessListener { binding.progressSimpanPegawai.visibility = View.GONE; Toast.makeText(this, "✅ Pegawai diperbarui!", Toast.LENGTH_SHORT).show(); finish() }
                .addOnFailureListener { binding.progressSimpanPegawai.visibility = View.GONE; binding.btnSimpanPegawai.isEnabled = true }
        }
    }
}
