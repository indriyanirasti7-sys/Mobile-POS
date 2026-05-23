package com.rasti.selaraspos.cabang

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelCabang

    class ModCabangActivity : AppCompatActivity() {

        private lateinit var tvTitleMod: TextView
        private lateinit var etNamaCabang: TextInputEditText
        private lateinit var etAlamatCabang: TextInputEditText
        private lateinit var etTeleponCabang: TextInputEditText
        private lateinit var etPenanggungJawab: TextInputEditText
        private lateinit var btnSimpan: Button
        private lateinit var btnHapus: Button
        private lateinit var loadingOverlay: FrameLayout
        private lateinit var btnBack: ImageButton

        private val database = FirebaseDatabase.getInstance()
        private var idCabang  = ""
        private var isEditMode = false

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_mod_cabang)

            initViews()
            cekModeEdit()

            btnBack.setOnClickListener { finish() }
            btnSimpan.setOnClickListener { validasiDanSimpan() }
            btnHapus.setOnClickListener { konfirmasiHapus() }
        }

        private fun initViews() {
            tvTitleMod         = findViewById(R.id.tvTitleMod)
            etNamaCabang       = findViewById(R.id.etNamaCabang)
            etAlamatCabang     = findViewById(R.id.etAlamatCabang)
            etTeleponCabang    = findViewById(R.id.etTeleponCabang)
            etPenanggungJawab  = findViewById(R.id.etPenanggungJawab)
            btnSimpan          = findViewById(R.id.btnSimpanCabang)
            btnHapus           = findViewById(R.id.btnHapusCabang)
            loadingOverlay     = findViewById(R.id.loadingOverlay)
            btnBack            = findViewById(R.id.btnBack)
        }

        private fun cekModeEdit() {
            idCabang  = intent.getStringExtra("idCabang") ?: ""
            isEditMode = idCabang.isNotEmpty()

            if (isEditMode) {
                tvTitleMod.text     = "Edit Cabang"
                btnSimpan.text      = "Perbarui Cabang"
                btnHapus.visibility = View.VISIBLE

                etNamaCabang.setText(intent.getStringExtra("namaCabang") ?: "")
                etAlamatCabang.setText(intent.getStringExtra("alamatCabang") ?: "")
                etTeleponCabang.setText(intent.getStringExtra("teleponCabang") ?: "")
                etPenanggungJawab.setText(intent.getStringExtra("penanggungJawab") ?: "")
            }
        }

        private fun validasiDanSimpan() {
            val nama      = etNamaCabang.text.toString().trim()
            val alamat    = etAlamatCabang.text.toString().trim()
            val telepon   = etTeleponCabang.text.toString().trim()
            val pj        = etPenanggungJawab.text.toString().trim()

            when {
                nama.isEmpty()   -> { etNamaCabang.error = "Nama cabang wajib diisi"; etNamaCabang.requestFocus(); return }
                alamat.isEmpty() -> { etAlamatCabang.error = "Alamat wajib diisi"; etAlamatCabang.requestFocus(); return }
            }

            if (isEditMode) updateCabang(nama, alamat, telepon, pj)
            else            tambahCabang(nama, alamat, telepon, pj)
        }

        private fun tambahCabang(nama: String, alamat: String, telepon: String, pj: String) {
            showLoading(true)
            val ref = database.getReference("cabang")
            val id  = ref.push().key ?: return

            val cabang = ModelCabang(
                idCabang = id,
                namaCabang = nama,
                alamatCabang = alamat,
                teleponCabang = telepon,
                penanggungJawab = pj
            )

            ref.child(id).setValue(cabang)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, "✅ Cabang berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, "❌ Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        private fun updateCabang(nama: String, alamat: String, telepon: String, pj: String) {
            showLoading(true)
            val updates = mapOf(
                "namaCabang"      to nama,
                "alamatCabang"    to alamat,
                "teleponCabang"   to telepon,
                "penanggungJawab" to pj
            )
            database.getReference("cabang/$idCabang")
                .updateChildren(updates)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(this, "✅ Data cabang diperbarui!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(this, "❌ Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        private fun konfirmasiHapus() {
            AlertDialog.Builder(this)
                .setTitle("Hapus Cabang")
                .setMessage("Yakin ingin menghapus cabang ini?")
                .setPositiveButton("Ya, Hapus") { _, _ ->
                    showLoading(true)
                    database.getReference("cabang/$idCabang")
                        .removeValue()
                        .addOnSuccessListener {
                            showLoading(false)
                            Toast.makeText(this, "✅ Cabang dihapus!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            showLoading(false)
                            Toast.makeText(this, "❌ Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        private fun showLoading(show: Boolean) {
            loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        }
}