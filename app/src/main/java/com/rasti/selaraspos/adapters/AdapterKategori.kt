package com.selaraspos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.R // Pastikan ini mengarah ke file R yang benar
import com.rasti.selaraspos.model.ModelKategori

class AdapterKategori(
    private val listKategori: MutableList<ModelKategori>,
    private val onEditClick: (ModelKategori) -> Unit,
    private val onHapusClick: (ModelKategori) -> Unit
) : RecyclerView.Adapter<AdapterKategori.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Inisialisasi manual komponen UI
        val tvNamaKategori: TextView = view.findViewById(R.id.tvNamaKategori)
        val btnEditKategori: ImageButton = view.findViewById(R.id.btnEditKategori)
        val btnHapusKategori: ImageButton = view.findViewById(R.id.btnHapusKategori)

        fun bind(kategori: ModelKategori) {
            tvNamaKategori.text = kategori.namaKategori

            btnEditKategori.setOnClickListener {
                onEditClick(kategori)
            }

            btnHapusKategori.setOnClickListener {
                onHapusClick(kategori)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Menggunakan R.layout.item_kategori (pastikan nama file XML-nya benar)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_kategori, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listKategori[position])
    }

    override fun getItemCount(): Int = listKategori.size

    fun updateData(data: List<ModelKategori>) {
        listKategori.clear()
        listKategori.addAll(data)
        notifyDataSetChanged()
    }
}