package com.rasti.selaraspos.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelProduk

class DetailProdukAdapter(private var produkList: List<ModelProduk>) :
    RecyclerView.Adapter<DetailProdukAdapter.ProdukViewHolder>() {

    lateinit var appContext: Context

    interface OnItemClickListener {
        fun onItemClick(produk: ModelProduk)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun updateData(newList: List<ModelProduk>) {
        produkList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_produk, parent, false)
        appContext = parent.context
        return ProdukViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        val produk = produkList[position]
        holder.bind(produk)
    }

    override fun getItemCount(): Int {
        return produkList.size
    }

    inner class ProdukViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvNamaProduk: TextView = itemView.findViewById(R.id.tvNamaProduk)
        val tvHargaProduk: TextView = itemView.findViewById(R.id.tvHargaProduk)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)

        fun bind(produk: ModelProduk) {
            tvNamaProduk.text = produk.namaProduk ?: "-"
            tvHargaProduk.text = produk.hargaProduk?.toString() ?: "-"
            chipStatus.text = produk.statusProduk ?: "-"

            itemView.setOnClickListener {
                listener?.onItemClick(produk)
            }
        }
    }
}