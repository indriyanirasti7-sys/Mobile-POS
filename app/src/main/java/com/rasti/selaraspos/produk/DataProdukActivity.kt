package com.rasti.selaraspos.produk

import android.os.Bundle
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
import com.rasti.selaraspos.adapter.DetailProdukAdapter
import com.rasti.selaraspos.model.ModelProduk
import com.rasti.selaraspos.viewmodel.DataProdukViewModel

class DataProdukActivity : AppCompatActivity() {

    private val viewModel: DataProdukViewModel by viewModels()
    private lateinit var rvDataProduk: RecyclerView
    private lateinit var fabTambahProduk: FloatingActionButton
    private lateinit var tvKosong: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_produk)

        // Memanggil fungsi inisialisasi view
        init()

        // Setting LayoutManager untuk RecyclerView
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDataProduk.layoutManager = layoutManager
        rvDataProduk.setHasFixedSize(true)

        // Observasi data dari ViewModel
        viewModel.produkList.observe(this) { list ->
            // Pastikan DetailProdukAdapter menerima List<ModelProduk>
            val adapter = DetailProdukAdapter(list)
            rvDataProduk.adapter = adapter


            adapter.setOnItemClickListener(object : DetailProdukAdapter.OnItemClickListener {
                override fun onItemClick(produk: ModelProduk) {
                    if (!produk.idProduk.isNullOrBlank()) {
                        showProdukDataFragment(produk)
                    } else {
                        Toast.makeText(
                            this@DataProdukActivity,
                            "Galat: Data produk tidak valid",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })

            // Menampilkan/Sembunyikan teks "Kosong" jika data tidak ada
            if (list.isEmpty()) {
                tvKosong.visibility = android.view.View.VISIBLE
            } else {
                tvKosong.visibility = android.view.View.GONE
            }
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showProdukDataFragment(produk: ModelProduk) {
        // Logika untuk menampilkan detail atau pindah fragment
        Toast.makeText(this, "Membuka detail: ${produk.namaProduk}", Toast.LENGTH_SHORT).show()
    }

    private fun init() {
          // Pastikan ID ini sama persis dengan yang ada di activity_data_produk.xml
        rvDataProduk = findViewById(R.id.rvProduk)
        tvKosong = findViewById(R.id.tvData_Produk_Kosong)
        fabTambahProduk = findViewById(R.id.fabDataProdukTambah)
    }
}