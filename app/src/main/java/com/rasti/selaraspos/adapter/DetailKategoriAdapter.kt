package com.rasti.selaraspos.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
// Hapus ViewParent karena tidak dipakai
import com.rasti.selaraspos.R
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.rasti.selaraspos.model.ModelKategori

class DetailKategoriAdapter (private val kategoriList: List<ModelKategori>):
    RecyclerView.Adapter<DetailKategoriAdapter.KategoriViewHolder>() {

    lateinit var appContext: Context

    interface OnClickListener {
        fun OnItemClick (kategori: ModelKategori)
    }

    private var listener: OnClickListener? = null

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int): KategoriViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_data_kategori, parent, false
        )
        appContext = parent.context
        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: KategoriViewHolder,
        position: Int
    ) {
        val kategori = kategoriList[position]
        holder.bind(kategori)
    }

    override fun getItemCount(): Int {
        return kategoriList.size
    }

    inner class KategoriViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvNamaKategori: TextView = itemView.findViewById(R.id.tv_kategori)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)

        fun bind(kategori: ModelKategori) {
            tvNamaKategori.text = kategori.namaKategori ?: "-"
            chipStatus.text = kategori.status ?: "-"

            itemView.setOnClickListener {
                listener?.OnItemClick(kategori)
            }
        }
    }
}