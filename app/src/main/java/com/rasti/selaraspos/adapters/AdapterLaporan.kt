package com.rasti.selaraspos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.model.ModelLaporan
import com.rasti.selaraspos.R
import java.text.NumberFormat
import java.util.Locale
class AdapterLaporan(
        private var listLaporan: MutableList<ModelLaporan>
    ) : RecyclerView.Adapter<AdapterLaporan.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvId: TextView = itemView.findViewById(R.id.tvIdTransaksiLaporan)
            val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggalLaporan)
            val tvCabang: TextView = itemView.findViewById(R.id.tvCabangLaporan)
            val tvMetode: TextView = itemView.findViewById(R.id.tvMetodeLaporan)
            val tvTotal: TextView = itemView.findViewById(R.id.tvTotalLaporan)
            val tvKasir: TextView = itemView.findViewById(R.id.tvKasirLaporan)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_laporan, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = listLaporan[position]

            holder.tvId.text      = item.idTransaksi
            holder.tvTanggal.text = item.tanggal
            holder.tvCabang.text  = item.cabang.ifEmpty { "–" }
            holder.tvMetode.text  = item.metodePembayaran
            holder.tvTotal.text   = formatRupiah(item.total)
            holder.tvKasir.text   = "Kasir: ${item.namaKasir}"
        }

        override fun getItemCount(): Int = listLaporan.size

        fun updateData(data: List<ModelLaporan>) {
            listLaporan.clear()
            listLaporan.addAll(data)
            notifyDataSetChanged()
        }

        private fun formatRupiah(nominal: Long): String {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            return format.format(nominal).replace("Rp", "Rp ").replace(",00", "")
        }
    }

