package com.rasti.selaraspos.produk

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rasti.selaraspos.R
import com.rasti.selaraspos.adapter.DetailProdukAdapter
import com.rasti.selaraspos.model.ModelProduk
import com.rasti.selaraspos.viewmodel.DataProdukViewModel

class DataProdukActivity : AppCompatActivity() {

    private lateinit var rvProduk: RecyclerView

    private lateinit var fabTambah: FloatingActionButton

    private lateinit var tvKosong: TextView

    private lateinit var etSearch: EditText

    private lateinit var ivClearSearch: ImageView

    private lateinit var btnBack: ImageView

    private lateinit var adapter: DetailProdukAdapter

    private val viewModel: DataProdukViewModel
            by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_data_produk)

        initViews()

        setupRecycler()

        setupClickListeners()

        observeData()
    }

    private fun initViews() {

        rvProduk =
            findViewById(R.id.rvProduk)

        fabTambah =
            findViewById(R.id.fabDataProdukTambah)

        tvKosong =
            findViewById(R.id.tvDataProdukKosong)

        etSearch =
            findViewById(R.id.etSearchProduk)

        ivClearSearch =
            findViewById(R.id.ivClearSearch)

        btnBack =
            findViewById(R.id.btnBack)

        fabTambah.bringToFront()
    }

    private fun setupRecycler() {

        adapter = DetailProdukAdapter(emptyList())

        rvProduk.layoutManager =
            LinearLayoutManager(this)

        rvProduk.adapter = adapter

        adapter.setOnItemClickListener(
            object : DetailProdukAdapter
            .OnItemClickListener {

                override fun onItemClick(
                    produk: ModelProduk
                ) {

                    // TODO: buka detail produk
                }
            }
        )
    }

    private fun setupClickListeners() {

        btnBack.setOnClickListener {

            finish()
        }

        fabTambah.setOnClickListener {

            try {

                val intent = Intent(
                    this,
                    ModProdukActivity::class.java
                )

                startActivity(intent)

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        etSearch.addTextChangedListener(

            object : android.text.TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    viewModel.filterList(
                        s.toString()
                    )

                    ivClearSearch.visibility =
                        if (s.isNullOrEmpty())
                            View.GONE
                        else
                            View.VISIBLE
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {}
            }
        )

        ivClearSearch.setOnClickListener {

            etSearch.text.clear()

            viewModel.filterList("")
        }
    }

    private fun observeData() {

        viewModel.isLoading.observe(this) {
                isLoading ->

            // bisa tambah ProgressBar di sini
        }

        viewModel.produkList.observe(this) {
                produkList ->

            produkList?.let {

                adapter.updateData(it)

                if (it.isEmpty()) {

                    tvKosong.visibility =
                        View.VISIBLE

                    rvProduk.visibility =
                        View.GONE

                } else {

                    tvKosong.visibility =
                        View.GONE

                    rvProduk.visibility =
                        View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {

        super.onResume()

        viewModel.getData()
    }
}