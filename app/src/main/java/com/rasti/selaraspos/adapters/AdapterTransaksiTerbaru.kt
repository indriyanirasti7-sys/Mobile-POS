package com.rasti.selaraspos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelTransaksi
import java.text.NumberFormat
import java.util.Locale

class AdapterTransaksiTerbaru(
    private val listTransaksi: MutableList<ModelTransaksi>
) : RecyclerView.Adapter<AdapterTransaksiTerbaru.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvId: TextView = itemView.findViewById(R.id.tvIdTransaksi)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggalTransaksi)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotalTransaksi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi_terbaru, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trx = listTransaksi[position]
        holder.tvId.text = "#${trx.idTransaksi.takeLast(8)}"
        holder.tvTanggal.text = trx.tanggal
        holder.tvTotal.text = formatRupiah(trx.total)
    }

    override fun getItemCount(): Int = listTransaksi.size

    fun updateData(data: List<ModelTransaksi>) {
        listTransaksi.clear()
        listTransaksi.addAll(data)
        notifyDataSetChanged()
    }

    private fun formatRupiah(value: Long): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(value)
    }
}