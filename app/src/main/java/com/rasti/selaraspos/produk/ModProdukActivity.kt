package com.rasti.selaraspos.produk

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.R
import com.rasti.selaraspos.RoleHelper
import com.rasti.selaraspos.databinding.ActivityModProdukBinding
import com.rasti.selaraspos.model.ModelProduk

/**
 * ModProdukActivity - Tambah / Edit Produk
 * Guard: hanya admin yang boleh akses, kasir diarahkan keluar
 */
class ModProdukActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModProdukBinding
    private val db = FirebaseDatabase.getInstance().reference
    private var mode = "TAMBAH"
    private var idProdukEdit = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Cek hak akses ──────────────────────────────────────────────
        if (!RoleHelper.isAdmin(this)) {
            Toast.makeText(this, "⛔ Hanya Admin yang bisa mengelola produk", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding = ActivityModProdukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mode = intent.getStringExtra("MODE") ?: "TAMBAH"
        binding.tvJudulMod.text = if (mode == "EDIT") "Edit Produk" else "Tambah Produk"

        muatKategori()
        aturPreviewGambar()
        if (mode == "EDIT") isiDataEdit()

        binding.btnSimpanProduk.setOnClickListener { simpanProduk() }
        binding.btnKembali.setOnClickListener { finish() }
    }

    private fun muatKategori() {
        db.child("kategori").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<String>()
                for (snap in snapshot.children) {
                    snap.child("namaKategori").getValue(String::class.java)?.let { list.add(it) }
                }
                val adp = ArrayAdapter(this@ModProdukActivity,
                    android.R.layout.simple_dropdown_item_1line, list)
                binding.actvKategori.setAdapter(adp)
            }
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    private fun aturPreviewGambar() {
        binding.etFotoProduk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {
                val url = s.toString().trim()
                if (url.isNotEmpty()) {
                    binding.imgPreviewProduk.visibility = View.VISIBLE
                    Glide.with(this@ModProdukActivity).load(url)
                        .apply(RequestOptions().transform(RoundedCorners(14))
                            .placeholder(R.drawable.produk).error(R.drawable.produk))
                        .into(binding.imgPreviewProduk)
                } else {
                    binding.imgPreviewProduk.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun isiDataEdit() {
        idProdukEdit = intent.getStringExtra("ID_PRODUK") ?: ""
        binding.etNamaProduk.setText(intent.getStringExtra("NAMA_PRODUK") ?: "")
        binding.etHargaJual.setText(intent.getLongExtra("HARGA_JUAL", 0L).toString())
        binding.etStokProduk.setText(intent.getIntExtra("STOK_PRODUK", 0).toString())
        binding.actvKategori.setText(intent.getStringExtra("KATEGORI_PRODUK") ?: "")
        val foto = intent.getStringExtra("FOTO_PRODUK") ?: ""
        binding.etFotoProduk.setText(foto)
        if (foto.isNotEmpty()) {
            binding.imgPreviewProduk.visibility = View.VISIBLE
            Glide.with(this).load(foto)
                .apply(RequestOptions().transform(RoundedCorners(14)).placeholder(R.drawable.produk))
                .into(binding.imgPreviewProduk)
        }
    }

    private fun simpanProduk() {
        val nama = binding.etNamaProduk.text.toString().trim()
        val hargaStr = binding.etHargaJual.text.toString().trim()
        val stokStr = binding.etStokProduk.text.toString().trim()
        val kategori = binding.actvKategori.text.toString().trim()
        val foto = binding.etFotoProduk.text.toString().trim()

        if (nama.isEmpty()) { binding.tilNamaProduk.error = "Wajib diisi"; return }
        if (hargaStr.isEmpty()) { binding.tilHargaJual.error = "Wajib diisi"; return }
        if (stokStr.isEmpty()) { binding.tilStokProduk.error = "Wajib diisi"; return }
        if (kategori.isEmpty()) { binding.tilKategori.error = "Wajib dipilih"; return }

        binding.tilNamaProduk.error = null
        binding.tilHargaJual.error = null
        binding.tilStokProduk.error = null
        binding.tilKategori.error = null

        binding.progressSimpan.visibility = View.VISIBLE
        binding.btnSimpanProduk.isEnabled = false

        val harga = hargaStr.toLongOrNull() ?: 0L
        val stok = stokStr.toIntOrNull() ?: 0

        if (mode == "TAMBAH") {
            val id = db.child("produk").push().key ?: return
            db.child("produk").child(id).setValue(ModelProduk(id, nama, harga, stok, kategori, foto))
                .addOnSuccessListener {
                    binding.progressSimpan.visibility = View.GONE
                    Toast.makeText(this, "✅ Produk ditambahkan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    binding.progressSimpan.visibility = View.GONE
                    binding.btnSimpanProduk.isEnabled = true
                    Toast.makeText(this, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.child("produk").child(idProdukEdit).updateChildren(
                mapOf("namaProduk" to nama, "hargaJual" to harga,
                    "stokProduk" to stok, "kategoriProduk" to kategori, "fotoProduk" to foto)
            ).addOnSuccessListener {
                binding.progressSimpan.visibility = View.GONE
                Toast.makeText(this, "✅ Produk diperbarui!", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                binding.progressSimpan.visibility = View.GONE
                binding.btnSimpanProduk.isEnabled = true
                Toast.makeText(this, "Gagal memperbarui", Toast.LENGTH_SHORT).show()
            }
        }
    }
}