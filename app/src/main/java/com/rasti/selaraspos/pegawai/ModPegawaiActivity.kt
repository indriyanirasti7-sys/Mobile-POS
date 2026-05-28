package com.rasti.selaraspos.pegawai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.rasti.selaraspos.databinding.ActivityModPegawaiBinding
import com.rasti.selaraspos.model.ModelPegawai

class ModPegawaiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModPegawaiBinding
    private val db = FirebaseDatabase.getInstance().reference.child("pegawai")
    private var mode = "TAMBAH"
    private var idEdit = ""

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

            // 🔥 SET STATUS 🔥
            val status = intent.getStringExtra("STATUS") ?: "aktif"
            if (status == "aktif") {
                binding.rbAktif.isChecked = true
            } else {
                binding.rbTidakAktif.isChecked = true
            }
        } else {
            binding.actvRole.setText("kasir", false)
            binding.rbAktif.isChecked = true  // Default aktif
        }

        binding.btnSimpanPegawai.setOnClickListener { simpan() }
        binding.btnKembali.setOnClickListener { finish() }
    }

    private fun simpan() {
        val nama = binding.etNamaPegawai.text.toString().trim()
        val role = binding.actvRole.text.toString().trim()
        val noHp = binding.etNoHp.text.toString().trim()
        val alamat = binding.etAlamatPegawai.text.toString().trim()
        val email = binding.etEmailPegawai.text.toString().trim()
        val status = if (binding.rbAktif.isChecked) "aktif" else "tidak_aktif"

        if (nama.isEmpty()) {
            binding.tilNamaPegawai.error = "Nama wajib diisi"
            return
        }
        if (role.isEmpty()) {
            binding.tilRole.error = "Role wajib dipilih"
            return
        }

        binding.tilNamaPegawai.error = null
        binding.tilRole.error = null
        binding.progressSimpanPegawai.visibility = View.VISIBLE
        binding.btnSimpanPegawai.isEnabled = false

        if (mode == "TAMBAH") {
            val id = db.push().key ?: return
            val pegawai = ModelPegawai(
                idPegawai = id,
                namaPegawai = nama,
                role = role,
                noHp = noHp,
                alamat = alamat,
                email = email,
                fotoPegawai = "",
                status = status
            )
            db.child(id).setValue(pegawai)
                .addOnSuccessListener {
                    binding.progressSimpanPegawai.visibility = View.GONE
                    Toast.makeText(this, "✅ Pegawai ditambahkan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    binding.progressSimpanPegawai.visibility = View.GONE
                    binding.btnSimpanPegawai.isEnabled = true
                    Toast.makeText(this, "❌ Gagal menambahkan pegawai", Toast.LENGTH_SHORT).show()
                }
        } else {
            val updates = mapOf(
                "namaPegawai" to nama,
                "role" to role,
                "noHp" to noHp,
                "alamat" to alamat,
                "status" to status
            )
            db.child(idEdit).updateChildren(updates)
                .addOnSuccessListener {
                    binding.progressSimpanPegawai.visibility = View.GONE
                    Toast.makeText(this, "✅ Pegawai diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    binding.progressSimpanPegawai.visibility = View.GONE
                    binding.btnSimpanPegawai.isEnabled = true
                    Toast.makeText(this, "❌ Gagal memperbarui pegawai", Toast.LENGTH_SHORT).show()
                }
        }
    }
}