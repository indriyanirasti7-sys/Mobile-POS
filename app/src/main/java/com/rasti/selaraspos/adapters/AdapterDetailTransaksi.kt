package com.rasti.selaraspos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelDetailTransaksi
import java.text.NumberFormat
import java.util.Locale

class AdapterDetailTransaksi(
    private val list: List<ModelDetailTransaksi>
) : RecyclerView.Adapter<AdapterDetailTransaksi.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvDetailNamaProduk)
        val tvQty: TextView = itemView.findViewById(R.id.tvDetailQty)
        val tvHarga: TextView = itemView.findViewById(R.id.tvDetailHarga)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detail_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNama.text = item.namaProduk
        holder.tvQty.text = "x${item.qty}"
        holder.tvHarga.text = formatRupiah(item.subtotal)
    }

    override fun getItemCount() = list.size

    private fun formatRupiah(value: Long): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(value)
    }
}