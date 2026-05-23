package com.rasti.selaraspos.pegawai

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelPegawai


    class ModPegawaiActivity : AppCompatActivity() {

        private lateinit var tvTitleMod: TextView
        private lateinit var etNama: TextInputEditText
        private lateinit var etNoHp: TextInputEditText
        private lateinit var etAlamat: TextInputEditText
        private lateinit var rgRole: RadioGroup
        private lateinit var rbAdmin: RadioButton
        private lateinit var rbKasir: RadioButton
        private lateinit var btnSimpan: Button
        private lateinit var btnHapus: Button
        private lateinit var loadingOverlay: FrameLayout
        private lateinit var btnBack: ImageButton

        private val database = FirebaseDatabase.getInstance()

        // Data mode edit
        private var idPegawai  = ""
        private var isEditMode = false

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_mod_pegawai)

            initViews()
            cekModeEdit()

            btnBack.setOnClickListener { finish() }
            btnSimpan.setOnClickListener { validasiDanSimpan() }
            btnHapus.setOnClickListener { konfirmasiHapus() }
        }

        private fun initViews() {
            tvTitleMod   = findViewById(R.id.tvTitleMod)
            etNama       = findViewById(R.id.etNamaPegawai)
            etNoHp       = findViewById(R.id.etNoHpPegawai)
            etAlamat     = findViewById(R.id.etAlamatPegawai)
            rgRole       = findViewById(R.id.rgRole)
            rbAdmin      = findViewById(R.id.rbAdmin)
            rbKasir      = findViewById(R.id.rbKasir)
            btnSimpan    = findViewById(R.id.btnSimpanPegawai)
            btnHapus     = findViewById(R.id.btnHapusPegawai)
            loadingOverlay = findViewById(R.id.loadingOverlay)
            btnBack      = findViewById(R.id.btnBack)
        }

        // ===== CEK MODE EDIT DARI INTENT =====
        private fun cekModeEdit() {
            idPegawai = intent.getStringExtra("idPegawai") ?: ""
            isEditMode = idPegawai.isNotEmpty()

            if (isEditMode) {
                tvTitleMod.text    = "Edit Pegawai"
                btnSimpan.text     = "Perbarui Pegawai"
                btnHapus.visibility = View.VISIBLE

                // Isi form dengan data yang sudah ada
                etNama.setText(intent.getStringExtra("namaPegawai") ?: "")
                etNoHp.setText(intent.getStringExtra("noHp") ?: "")
                etAlamat.setText(intent.getStringExtra("alamat") ?: "")

                val role = intent.getStringExtra("role") ?: "kasir"
                if (role.lowercase() == "admin") rbAdmin.isChecked = true
                else rbKasir.isChecked = true
            }
        }

        // ===== VALIDASI INPUT =====
        private fun validasiDanSimpan() {
            val nama   = etNama.text.toString().trim()
            val noHp   = etNoHp.text.toString().trim()
            val alamat = etAlamat.text.toString().trim()

            when {
                nama.isEmpty()   -> { etNama.error = "Nama wajib diisi"; etNama.requestFocus(); return }
                noHp.isEmpty()   -> { etNoHp.error = "No HP wajib diisi"; etNoHp.requestFocus(); return }
                alamat.isEmpty() -> { etAlamat.error = "Alamat wajib diisi"; etAlamat.requestFocus(); return }
            }

            val role = if (rbAdmin.isChecked) "admin" else "kasir"

            if (isEditMode) updatePegawai(nama, noHp, alamat, role)
            else            tambahPegawai(nama, noHp, alamat, role)
        }

        // ===== TAMBAH PEGAWAI BARU =====
        private fun tambahPegawai(nama: String, noHp: String, alamat: String, role: String) {
            showLoading(true)
            val ref = database.getReference("pegawai")
            val id  = ref.push().key ?: return

            val pegawai = ModelPegawai(
                idPegawai = id,
                namaPegawai = nama,
                role = role,
                noHp = noHp,
                alamat = alamat
            )

            ref.child(id).setValue(pegawai)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, "✅ Pegawai berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, "❌ Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // ===== UPDATE DATA PEGAWAI =====
        private fun updatePegawai(nama: String, noHp: String, alamat: String, role: String) {
            showLoading(true)
            val updates = mapOf(
                "namaPegawai" to nama,
                "noHp"        to noHp,
                "alamat"      to alamat,
                "role"        to role
            )
            database.getReference("pegawai/$idPegawai")
                .updateChildren(updates)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, "✅ Data pegawai diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, "❌ Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // ===== HAPUS PEGAWAI =====
        private fun konfirmasiHapus() {
            AlertDialog.Builder(this)
                .setTitle("Hapus Pegawai")
                .setMessage("Yakin ingin menghapus pegawai ini? Data tidak dapat dikembalikan.")
                .setPositiveButton("Ya, Hapus") { _, _ -> hapusPegawai() }
                .setNegativeButton("Batal", null)
                .show()
        }

        private fun hapusPegawai() {
            showLoading(true)
            database.getReference("pegawai/$idPegawai")
                .removeValue()
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, "✅ Pegawai dihapus!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, "❌ Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        private fun showLoading(show: Boolean) {
            loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        }
}