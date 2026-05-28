package com.rasti.selaraspos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelPelanggan

class AdapterPelanggan(
    private val list: MutableList<ModelPelanggan>,
    private val isAdmin: Boolean,
    private val onEdit: (ModelPelanggan) -> Unit,
    private val onHapus: (ModelPelanggan) -> Unit
) : RecyclerView.Adapter<AdapterPelanggan.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvNama: TextView = v.findViewById(R.id.tvNamaPelanggan)
        val tvTelepon: TextView = v.findViewById(R.id.tvTeleponPelanggan)
        val tvAlamat: TextView = v.findViewById(R.id.tvAlamatPelanggan)
        val tvPoint: TextView = v.findViewById(R.id.tvPointPelanggan)
        val btnEdit: TextView = v.findViewById(R.id.btnEditPelanggan)
        val btnHapus: TextView = v.findViewById(R.id.btnHapusPelanggan)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(
        LayoutInflater.from(p.context).inflate(R.layout.item_pelanggan, p, false)
    )

    override fun onBindViewHolder(h: VH, pos: Int) {
        val pelanggan = list[pos]
        h.tvNama.text = pelanggan.namaPelanggan
        h.tvTelepon.text = pelanggan.teleponPelanggan
        h.tvAlamat.text = pelanggan.alamatPelanggan
        h.tvPoint.text = "Point: ${pelanggan.point}"

        if (isAdmin) {
            h.btnEdit.visibility = View.VISIBLE
            h.btnHapus.visibility = View.VISIBLE
            h.btnEdit.setOnClickListener { onEdit(pelanggan) }
            h.btnHapus.setOnClickListener { onHapus(pelanggan) }
        } else {
            h.btnEdit.visibility = View.GONE
            h.btnHapus.visibility = View.GONE
        }
    }

    override fun getItemCount() = list.size

    fun updateData(data: List<ModelPelanggan>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }
}