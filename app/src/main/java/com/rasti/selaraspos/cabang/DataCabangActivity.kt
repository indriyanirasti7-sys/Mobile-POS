package com.rasti.selaraspos.cabang

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.cabang.ModCabangActivity
import com.rasti.selaraspos.R
import com.rasti.selaraspos.adapters.AdapterCabang
import com.rasti.selaraspos.model.ModelCabang

    class DataCabangActivity : AppCompatActivity() {

        private lateinit var rvCabang: RecyclerView
        private lateinit var progressBar: ProgressBar
        private lateinit var layoutEmptyCabang: LinearLayout
        private lateinit var etCariCabang: EditText
        private lateinit var tvJumlahCabang: TextView
        private lateinit var fabTambah: ExtendedFloatingActionButton

        private val listCabangOriginal = mutableListOf<ModelCabang>()
        private val listCabangFilter   = mutableListOf<ModelCabang>()
        private lateinit var adapter: AdapterCabang

        private val database = FirebaseDatabase.getInstance()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.actiivity_data_cabang)

            initViews()
            setupAdapter()
            setupSearch()
            loadCabang()

            findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
            fabTambah.setOnClickListener {
                startActivity(Intent(this, ModCabangActivity::class.java))
            }
        }

        override fun onResume() {
            super.onResume()
            loadCabang()
        }

        private fun initViews() {
            rvCabang          = findViewById(R.id.rvCabang)
            progressBar       = findViewById(R.id.progressBar)
            layoutEmptyCabang = findViewById(R.id.layoutEmptyCabang)
            etCariCabang      = findViewById(R.id.etCariCabang)
            tvJumlahCabang    = findViewById(R.id.tvJumlahCabang)
            fabTambah         = findViewById(R.id.fabTambahCabang)
        }

        private fun setupAdapter() {
            adapter = AdapterCabang(listCabangFilter) { cabang ->
                val intent = Intent(this, ModCabangActivity::class.java).apply {
                    putExtra("idCabang", cabang.idCabang)
                    putExtra("namaCabang", cabang.namaCabang)
                    putExtra("alamatCabang", cabang.alamatCabang)
                    putExtra("teleponCabang", cabang.teleponCabang)
                    putExtra("penanggungJawab", cabang.penanggungJawab)
                }
                startActivity(intent)
            }
            rvCabang.layoutManager = LinearLayoutManager(this)
            rvCabang.adapter = adapter
        }

        private fun setupSearch() {
            etCariCabang.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    adapter.filter(s.toString(), listCabangOriginal)
                    updateEmptyState()
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        private fun loadCabang() {
            progressBar.visibility = View.VISIBLE
            database.getReference("cabang")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        listCabangOriginal.clear()
                        for (data in snapshot.children) {
                            val cabang = data.getValue(ModelCabang::class.java) ?: continue
                            listCabangOriginal.add(cabang)
                        }
                        adapter.updateData(listCabangOriginal)
                        tvJumlahCabang.text = "${listCabangOriginal.size} cabang"
                        progressBar.visibility = View.GONE
                        updateEmptyState()
                    }
                    override fun onCancelled(error: DatabaseError) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@DataCabangActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        private fun updateEmptyState() {
            val empty = adapter.itemCount == 0
            layoutEmptyCabang.visibility = if (empty) View.VISIBLE else View.GONE
            rvCabang.visibility          = if (empty) View.GONE else View.VISIBLE
        }
}