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
import com.rasti.selaraspos.model.ModelCabang
import com.rasti.selaraspos.model.ModelKeranjang
import com.rasti.selaraspos.model.ModelLaporan
import com.rasti.selaraspos.model.ModelPegawai
import com.rasti.selaraspos.model.ModelProduk
import java.text.NumberFormat
import java.util.Locale

class AdapterLaporan(
    private val list: MutableList<ModelLaporan>
) : RecyclerView.Adapter<AdapterLaporan.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvId: TextView = v.findViewById(R.id.tvIdTransaksiLaporan)
        val tvTgl: TextView = v.findViewById(R.id.tvTanggalLaporan)
        val tvKasir: TextView = v.findViewById(R.id.tvKasirLaporan)
        val tvCabang: TextView = v.findViewById(R.id.tvCabangLaporan)
        val tvMetode: TextView = v.findViewById(R.id.tvMetodeLaporan)
        val tvTotal: TextView = v.findViewById(R.id.tvTotalLaporan)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(
        LayoutInflater.from(p.context).inflate(R.layout.item_laporan, p, false)
    )

    override fun onBindViewHolder(h: VH, pos: Int) {
        val l = list[pos]
        h.tvId.text = "#${l.idTransaksi.takeLast(8).uppercase()}"
        h.tvTgl.text = l.tanggal
        h.tvKasir.text = "Kasir: ${l.namaKasir}"
        h.tvCabang.text = l.cabang
        h.tvMetode.text = l.metodePembayaran
        h.tvTotal.text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(l.total)
    }

    override fun getItemCount() = list.size

    fun updateData(data: List<ModelLaporan>) {
        list.clear(); list.addAll(data); notifyDataSetChanged()
    }
}