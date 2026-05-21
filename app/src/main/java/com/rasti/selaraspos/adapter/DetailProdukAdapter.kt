package com.rasti.selaraspos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelProduk
import java.util.Locale

class DetailProdukAdapter(
    private var produkList: List<ModelProduk>
) : RecyclerView.Adapter<DetailProdukAdapter.ProdukViewHolder>() {

    interface OnItemClickListener {

        fun onItemClick(produk: ModelProduk)
    }

    private var listener:
            OnItemClickListener? = null

    fun setOnItemClickListener(
        listener: OnItemClickListener
    ) {

        this.listener = listener
    }

    fun updateData(
        newList: List<ModelProduk>
    ) {

        produkList = newList

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProdukViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_data_produk,
                    parent,
                    false
                )

        return ProdukViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProdukViewHolder,
        position: Int
    ) {

        holder.bind(produkList[position])
    }

    override fun getItemCount(): Int {

        return produkList.size
    }

    inner class ProdukViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvNamaProduk:
                TextView =
            itemView.findViewById(R.id.tvNamaProduk)

        private val tvHargaProduk:
                TextView =
            itemView.findViewById(R.id.tvHargaProduk)

        private val chipStatus:
                Chip =
            itemView.findViewById(R.id.chipStatus)

        fun bind(produk: ModelProduk) {

            tvNamaProduk.text =
                produk.namaProduk

            tvHargaProduk.text =
                String.format(
                    Locale("id", "ID"),
                    "Rp %,d",
                    produk.hargaJual
                )

            if (produk.stokTakTerbatas) {

                chipStatus.text = "Unlimited"

            } else {

                if (produk.stokProduk > 0) {

                    chipStatus.text = "Tersedia"

                } else {

                    chipStatus.text = "Habis"
                }
            }

            itemView.setOnClickListener {

                listener?.onItemClick(produk)
            }
        }
    }
}