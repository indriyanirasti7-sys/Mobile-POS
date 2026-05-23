package com.rasti.selaraspos.pegawai

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
import com.rasti.selaraspos.pegawai.ModPegawaiActivity
import com.rasti.selaraspos.R
import com.rasti.selaraspos.adapters.AdapterPegawai
import com.rasti.selaraspos.model.ModelPegawai


    class DataPegawaiActivity : AppCompatActivity() {

        private lateinit var rvPegawai: RecyclerView
        private lateinit var progressBar: ProgressBar
        private lateinit var layoutEmptyPegawai: LinearLayout
        private lateinit var etCariPegawai: EditText
        private lateinit var tvJumlahPegawai: TextView
        private lateinit var fabTambah: ExtendedFloatingActionButton

        private val listPegawaiOriginal = mutableListOf<ModelPegawai>()
        private val listPegawaiFilter   = mutableListOf<ModelPegawai>()
        private lateinit var adapter: AdapterPegawai

        private val database = FirebaseDatabase.getInstance()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_data_pegawai)

            initViews()
            setupAdapter()
            setupSearch()
            loadPegawai()

            findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
            fabTambah.setOnClickListener {
                startActivity(Intent(this, ModPegawaiActivity::class.java))
            }
        }

        override fun onResume() {
            super.onResume()
            // Refresh data setiap kali kembali dari ModPegawaiActivity
            loadPegawai()
        }

        private fun initViews() {
            rvPegawai         = findViewById(R.id.rvPegawai)
            progressBar       = findViewById(R.id.progressBar)
            layoutEmptyPegawai = findViewById(R.id.layoutEmptyPegawai)
            etCariPegawai     = findViewById(R.id.etCariPegawai)
            tvJumlahPegawai   = findViewById(R.id.tvJumlahPegawai)
            fabTambah         = findViewById(R.id.fabTambahPegawai)
        }

        private fun setupAdapter() {
            adapter = AdapterPegawai(listPegawaiFilter) { pegawai ->
                // Buka edit dengan membawa data pegawai
                val intent = Intent(this, ModPegawaiActivity::class.java).apply {
                    putExtra("idPegawai", pegawai.idPegawai)
                    putExtra("namaPegawai", pegawai.namaPegawai)
                    putExtra("noHp", pegawai.noHp)
                    putExtra("alamat", pegawai.alamat)
                    putExtra("role", pegawai.role)
                }
                startActivity(intent)
            }
            rvPegawai.layoutManager = LinearLayoutManager(this)
            rvPegawai.adapter = adapter
        }

        private fun setupSearch() {
            etCariPegawai.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    adapter.filter(s.toString(), listPegawaiOriginal)
                    updateEmptyState()
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        private fun loadPegawai() {
            progressBar.visibility = View.VISIBLE
            database.getReference("pegawai")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        listPegawaiOriginal.clear()
                        for (data in snapshot.children) {
                            val pegawai = data.getValue(ModelPegawai::class.java) ?: continue
                            listPegawaiOriginal.add(pegawai)
                        }
                        adapter.updateData(listPegawaiOriginal)
                        tvJumlahPegawai.text = "${listPegawaiOriginal.size} pegawai"
                        progressBar.visibility = View.GONE
                        updateEmptyState()
                    }
                    override fun onCancelled(error: DatabaseError) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@DataPegawaiActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        private fun updateEmptyState() {
            val empty = adapter.itemCount == 0
            layoutEmptyPegawai.visibility = if (empty) View.VISIBLE else View.GONE
            rvPegawai.visibility          = if (empty) View.GONE else View.VISIBLE
        }
}