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

/**
 * Adapter untuk menampilkan 5 transaksi terbaru di Dashboard
 */
class AdapterTransaksiTerbaru(
    private val listTransaksi: MutableList<ModelTransaksi>
) : RecyclerView.Adapter<AdapterTransaksiTerbaru.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIdTrx: TextView = itemView.findViewById(R.id.tvIdTrxTerbaru)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggalTerbaru)
        val tvKasir: TextView = itemView.findViewById(R.id.tvKasirTerbaru)
        val tvMetode: TextView = itemView.findViewById(R.id.tvMetodeTerbaru)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotalTerbaru)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi_terbaru, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trx = listTransaksi[position]
        // Tampilkan 8 karakter terakhir ID agar tidak terlalu panjang
        holder.tvIdTrx.text = "#${trx.idTransaksi.takeLast(8).uppercase()}"
        holder.tvTanggal.text = trx.tanggal
        holder.tvKasir.text = trx.namaKasir
        holder.tvMetode.text = trx.metodePembayaran
        holder.tvTotal.text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(trx.total)
    }

    override fun getItemCount(): Int = listTransaksi.size

    fun updateData(data: List<ModelTransaksi>) {
        listTransaksi.clear()
        listTransaksi.addAll(data)
        notifyDataSetChanged()
    }
}