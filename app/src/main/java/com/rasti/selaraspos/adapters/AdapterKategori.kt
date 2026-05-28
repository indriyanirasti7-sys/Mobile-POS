package com.rasti.selaraspos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelKategori
import com.rasti.selaraspos.model.ModelProduk
import java.text.NumberFormat
import java.util.Locale
class AdapterKategori(
    private val listKategori: MutableList<ModelKategori>,
    private val isAdmin: Boolean,
    private val onEditClick: (ModelKategori) -> Unit,
    private val onHapusClick: (ModelKategori) -> Unit
) : RecyclerView.Adapter<AdapterKategori.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaKategori)
        val btnEdit: TextView = itemView.findViewById(R.id.btnEditKategori)
        val btnHapus: TextView = itemView.findViewById(R.id.btnHapusKategori)
        val layoutAksi: View = itemView.findViewById(R.id.layoutAksiKategori)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_kategori, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kat = listKategori[position]
        holder.tvNama.text = kat.namaKategori
        holder.layoutAksi.visibility = if (isAdmin) View.VISIBLE else View.GONE
        holder.btnEdit.setOnClickListener { onEditClick(kat) }
        holder.btnHapus.setOnClickListener { onHapusClick(kat) }
    }

    override fun getItemCount() = listKategori.size

    fun updateData(data: List<ModelKategori>) {
        listKategori.clear(); listKategori.addAll(data)
        notifyDataSetChanged()
    }
}