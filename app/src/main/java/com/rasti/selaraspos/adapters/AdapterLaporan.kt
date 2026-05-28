package com.selaraspos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.R // Pastikan import R sesuai package Anda
import com.rasti.selaraspos.model.ModelLaporan
import java.text.NumberFormat
import java.util.Locale

class AdapterLaporan(
    private val listLaporan: MutableList<ModelLaporan>
) : RecyclerView.Adapter<AdapterLaporan.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Inisialisasi manual
        val tvId: TextView = view.findViewById(R.id.tvIdTransaksiLaporan)
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggalLaporan)
        val tvKasir: TextView = view.findViewById(R.id.tvKasirLaporan)
        val tvCabang: TextView = view.findViewById(R.id.tvCabangLaporan)
        val tvMetode: TextView = view.findViewById(R.id.tvMetodeLaporan)
        val tvTotal: TextView = view.findViewById(R.id.tvTotalLaporan)

        fun bind(laporan: ModelLaporan) {
            tvId.text = laporan.idTransaksi
            tvTanggal.text = laporan.tanggal
            tvKasir.text = laporan.namaKasir
            tvCabang.text = laporan.cabang
            tvMetode.text = laporan.metodePembayaran
            tvTotal.text = formatRupiah(laporan.total)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_laporan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(listLaporan[position])

    override fun getItemCount(): Int = listLaporan.size

    fun updateData(data: List<ModelLaporan>) {
        listLaporan.clear()
        listLaporan.addAll(data)
        notifyDataSetChanged()
    }

    private fun formatRupiah(harga: Long) =
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(harga)
}