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


class AdapterCabang(
    private val list: MutableList<ModelCabang>,
    private val onEdit: (ModelCabang) -> Unit,
    private val onHapus: (ModelCabang) -> Unit
) : RecyclerView.Adapter<AdapterCabang.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvNama: TextView = v.findViewById(R.id.tvNamaCabangItem)
        val tvAlamat: TextView = v.findViewById(R.id.tvAlamatCabangItem)
        val tvTelepon: TextView = v.findViewById(R.id.tvTeleponCabangItem)
        val tvPj: TextView = v.findViewById(R.id.tvPJCabangItem)
        val btnEdit: TextView = v.findViewById(R.id.btnEditCabangItem)
        val btnHapus: TextView = v.findViewById(R.id.btnHapusCabangItem)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(
        LayoutInflater.from(p.context).inflate(R.layout.item_cabang, p, false)
    )

    override fun onBindViewHolder(h: VH, pos: Int) {
        val d = list[pos]
        h.tvNama.text = d.namaCabang
        h.tvAlamat.text = d.alamatCabang
        h.tvTelepon.text = d.teleponCabang
        h.tvPj.text = "PJ: ${d.penanggungjawab}"
        h.btnEdit.setOnClickListener { onEdit(d) }
        h.btnHapus.setOnClickListener { onHapus(d) }
    }

    override fun getItemCount() = list.size

    fun updateData(data: List<ModelCabang>) {
        list.clear(); list.addAll(data); notifyDataSetChanged()
    }
}