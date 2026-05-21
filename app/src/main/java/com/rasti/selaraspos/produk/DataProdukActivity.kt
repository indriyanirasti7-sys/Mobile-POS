package com.rasti.selaraspos.produk

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rasti.selaraspos.R
import com.rasti.selaraspos.adapter.DetailProdukAdapter
import com.rasti.selaraspos.viewmodel.DataProdukViewModel

class DataProdukActivity : AppCompatActivity() {

    private val viewModel:
            DataProdukViewModel by viewModels()

    private lateinit var rvProduk:
            RecyclerView

    private lateinit var fabTambah:
            FloatingActionButton

    private lateinit var tvKosong:
            TextView

    private lateinit var adapter:
            DetailProdukAdapter

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_data_produk
        )

        init()

        setupRecycler()

        setupFab()

        observeData()
    }

    private fun init() {

        rvProduk =
            findViewById(R.id.rvProduk)

        fabTambah =
            findViewById(R.id.fabDataProdukTambah)

        tvKosong =
            findViewById(R.id.tvDataProdukKosong)
    }

    private fun setupRecycler() {

        adapter =
            DetailProdukAdapter(emptyList())

        rvProduk.layoutManager =
            LinearLayoutManager(this)

        rvProduk.adapter =
            adapter
    }

    private fun setupFab() {

        // DITAMBAHKAN
        fabTambah.bringToFront()

        fabTambah.setOnClickListener {

            val intent =
                Intent(
                    this,
                    ModProdukActivity::class.java
                )

            startActivity(intent)
        }
    }

    private fun observeData() {

        viewModel.produkList.observe(this) {

            adapter.updateData(it)

            if (it.isEmpty()) {

                tvKosong.visibility =
                    View.VISIBLE

            } else {

                tvKosong.visibility =
                    View.GONE
            }
        }
    }
}