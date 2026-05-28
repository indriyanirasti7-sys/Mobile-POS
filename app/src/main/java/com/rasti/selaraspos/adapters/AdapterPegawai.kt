package com.rasti.selaraspos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelPegawai

class AdapterPegawai(
    private val list: MutableList<ModelPegawai>,
    private val isAdmin: Boolean,
    private val onEdit: (ModelPegawai) -> Unit,
    private val onHapus: (ModelPegawai) -> Unit
) : RecyclerView.Adapter<AdapterPegawai.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvNama: TextView = v.findViewById(R.id.tvNamaPegawai)
        val tvRole: TextView = v.findViewById(R.id.tvRolePegawai)
        val tvNoHp: TextView = v.findViewById(R.id.tvHpPegawai)
        val tvStatus: TextView = v.findViewById(R.id.tvStatusPegawai)  // ✅ PERBAIKI INI
        val btnEdit: TextView = v.findViewById(R.id.btnEditPegawai)
        val btnHapus: TextView = v.findViewById(R.id.btnHapusPegawai)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(
        LayoutInflater.from(p.context).inflate(R.layout.item_pegawai, p, false)
    )

    override fun onBindViewHolder(h: VH, pos: Int) {
        val pegawai = list[pos]
        h.tvNama.text = pegawai.namaPegawai
        h.tvRole.text = pegawai.role
        h.tvNoHp.text = pegawai.noHp

        // 🔥 SET STATUS DENGAN WARNA 🔥
        when (pegawai.status) {
            "aktif" -> {
                h.tvStatus.text = "Aktif"
                h.tvStatus.setBackgroundColor(ContextCompat.getColor(h.itemView.context, R.color.success_light))
                h.tvStatus.setTextColor(ContextCompat.getColor(h.itemView.context, R.color.success))
            }
            "tidak_aktif" -> {
                h.tvStatus.text = "Tidak Aktif"
                h.tvStatus.setBackgroundColor(ContextCompat.getColor(h.itemView.context, R.color.error_light))
                h.tvStatus.setTextColor(ContextCompat.getColor(h.itemView.context, R.color.error))
            }
            else -> {
                h.tvStatus.text = "Aktif"
                h.tvStatus.setBackgroundColor(ContextCompat.getColor(h.itemView.context, R.color.success_light))
                h.tvStatus.setTextColor(ContextCompat.getColor(h.itemView.context, R.color.success))
            }
        }

        // 🔥 HANYA ADMIN YANG BISA EDIT & HAPUS 🔥
        if (isAdmin) {
            h.btnEdit.visibility = View.VISIBLE
            h.btnHapus.visibility = View.VISIBLE
            h.btnEdit.setOnClickListener { onEdit(pegawai) }
            h.btnHapus.setOnClickListener { onHapus(pegawai) }
        } else {
            h.btnEdit.visibility = View.GONE
            h.btnHapus.visibility = View.GONE
        }
    }

    override fun getItemCount() = list.size

    fun updateData(data: List<ModelPegawai>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }
}