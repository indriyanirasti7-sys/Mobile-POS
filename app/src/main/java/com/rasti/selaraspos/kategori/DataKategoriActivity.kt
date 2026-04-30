package com.rasti.selaraspos.kategori

import android.os.Bundle
import android.widget.AdapterView
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
    //langkah pertama di bawah class
    private val viewModel: DataKategoriViewModel by viewModels()
    private lateinit var rvDataKategori: RecyclerView
    private lateinit var fabDATAKATEGORITambah: FloatingActionButton
    private lateinit var tvKosong: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_kategori)
        //langkah 3 di bawah set on create
        init()


            var layoutManager = LinearLayoutManager(this)
            layoutManager.reverseLayout = true
            layoutManager.stackFromEnd = true
            rvDataKategori.layoutManager = layoutManager
            rvDataKategori.setHasFixedSize(true)

            //langkah 4
            viewModel.kategoriList.observe(this) { list ->
                val adapter = DetailKategoriAdapter(list)
                rvDataKategori.adapter = adapter

                adapter.setOnClickListener(object : DetailKategoriAdapter.OnClickListener {
                    override fun OnItemClick(kategori: ModelKategori) {
                        if (!kategori.idkategori.isNullOrBlank()) {
                            showKategoriDataFragment(kategori)
                        } else {
                            Toast.makeText(
                                this@DataKategoriActivity,
                                "galat: {getString(R.string.data_kategori_tidak_valid}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
            }

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    private fun showKategoriDataFragment(kategori: ModelKategori) {

        Toast.makeText(this, "Membuka detail: ${kategori.namaKategori}", Toast.LENGTH_SHORT)
            .show()
    }
        //langkah 2 kurang tau karena tida tau fun init di taruh dimana
        fun init() {
            rvDataKategori = findViewById(R.id.rvKategori)
            tvKosong = findViewById(R.id.tvData_Kategori_Kosong)

        }
    }
