package com.rasti.selaraspos.kategori

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rasti.selaraspos.R
import com.rasti.selaraspos.adapter.DetailKategoriAdapter
import com.rasti.selaraspos.model.ModelKategori
import com.rasti.selaraspos.viewmodel.DataKategoriViewModel

class DataKategoriActivity : AppCompatActivity() {

    private val viewModel: DataKategoriViewModel by viewModels()
    private lateinit var rvDataKategori: RecyclerView
    private lateinit var fabTambahKategori: FloatingActionButton  // ← FAB
    private lateinit var tvKosong: TextView
    private lateinit var btnBack: ImageView
    private lateinit var etSearchKategori: EditText
    private lateinit var ivClearSearch: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_kategori)

        init()
        setupRecyclerView()
        setupSearchView()
        setupClickListeners()  // ← PENTING: panggil ini
        observeData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        rvDataKategori = findViewById(R.id.rvKategori)
        fabTambahKategori = findViewById(R.id.fabTambahKategori)  // ← inisialisasi FAB
        tvKosong = findViewById(R.id.tvDataKategoriKosong)
        btnBack = findViewById(R.id.btnBack)
        etSearchKategori = findViewById(R.id.etSearchKategori)
        ivClearSearch = findViewById(R.id.ivClearSearch)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDataKategori.layoutManager = layoutManager
        rvDataKategori.setHasFixedSize(true)
    }

    private fun setupSearchView() {
        etSearchKategori.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                ivClearSearch.visibility = if (s.isNullOrEmpty()) android.view.View.GONE else android.view.View.VISIBLE
                viewModel.filterList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        ivClearSearch.setOnClickListener {
            etSearchKategori.text.clear()
        }
    }

    private fun setupClickListeners() {
        // Tombol back
        btnBack.setOnClickListener {
            onBackPressed()
        }

        // ========== INI YANG PALING PENTING ==========
        // FAB untuk tambah kategori
        fabTambahKategori.setOnClickListener {
            // Pindah ke halaman ModKategoriActivity (tambah kategori)
            val intent = Intent(this, ModKategoriActivity::class.java)
            startActivity(intent)
        }
        // ============================================
    }

    private fun observeData() {
        viewModel.kategoriList.observe(this) { list ->
            if (list.isNullOrEmpty()) {
                tvKosong.visibility = android.view.View.VISIBLE
                rvDataKategori.visibility = android.view.View.GONE
            } else {
                tvKosong.visibility = android.view.View.GONE
                rvDataKategori.visibility = android.view.View.VISIBLE

                val adapter = DetailKategoriAdapter(list)
                rvDataKategori.adapter = adapter

                adapter.setOnClickListener(object : DetailKategoriAdapter.OnClickListener {
                    override fun OnItemClick(kategori: ModelKategori) {
                        if (!kategori.idkategori.isNullOrBlank()) {
                            showKategoriDataFragment(kategori)
                        } else {
                            Toast.makeText(
                                this@DataKategoriActivity,
                                "Data kategori tidak valid",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
            }
        }
    }

    private fun showKategoriDataFragment(kategori: ModelKategori) {
        // Pindah ke halaman ModKategoriActivity dengan data untuk edit
        val intent = Intent(this, ModKategoriActivity::class.java)
        intent.putExtra("idkategori", kategori.idkategori)
        intent.putExtra("namaKategori", kategori.namaKategori)
        intent.putExtra("status", kategori.status)
        startActivity(intent)
    }
}