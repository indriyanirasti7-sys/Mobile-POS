package com.rasti.selaraspos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.model.ModelPegawai
import com.rasti.selaraspos.R


    class AdapterPegawai(
        private var listPegawai: MutableList<ModelPegawai>,
        private val onEditClick: (ModelPegawai) -> Unit
    ) : RecyclerView.Adapter<AdapterPegawai.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvInisial: TextView = itemView.findViewById(R.id.tvInisialPegawai)
            val tvNama: TextView = itemView.findViewById(R.id.tvNamaPegawaiItem)
            val tvNoHp: TextView = itemView.findViewById(R.id.tvNoHpItem)
            val tvAlamat: TextView = itemView.findViewById(R.id.tvAlamatItem)
            val tvRole: TextView = itemView.findViewById(R.id.tvRoleItem)
            val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditPegawaiItem)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pegawai, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val pegawai = listPegawai[position]

            // Ambil inisial dari nama (1-2 huruf pertama)
            val inisial = if (pegawai.namaPegawai.isNotEmpty()) {
                pegawai.namaPegawai.split(" ")
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
            } else "?"

            holder.tvInisial.text = inisial
            holder.tvNama.text    = pegawai.namaPegawai
            holder.tvNoHp.text    = pegawai.noHp.ifEmpty { "–" }
            holder.tvAlamat.text  = pegawai.alamat.ifEmpty { "Alamat belum diisi" }
            holder.tvRole.text    = pegawai.role.replaceFirstChar { it.uppercase() }

            // Warna badge role
            if (pegawai.role.lowercase() == "admin") {
                holder.tvRole.setBackgroundResource(R.drawable.bg_tab_selected)
            } else {
                holder.tvRole.setBackgroundResource(R.drawable.bg_icon_circle)
            }

            holder.btnEdit.setOnClickListener { onEditClick(pegawai) }
            holder.itemView.setOnClickListener { onEditClick(pegawai) }
        }

        override fun getItemCount(): Int = listPegawai.size

        fun updateData(data: List<ModelPegawai>) {
            listPegawai.clear()
            listPegawai.addAll(data)
            notifyDataSetChanged()
        }

        fun filter(keyword: String, dataOriginal: List<ModelPegawai>) {
            listPegawai.clear()
            if (keyword.isEmpty()) {
                listPegawai.addAll(dataOriginal)
            } else {
                val lower = keyword.lowercase()
                listPegawai.addAll(
                    dataOriginal.filter {
                        it.namaPegawai.lowercase().contains(lower) ||
                                it.role.lowercase().contains(lower) ||
                                it.noHp.contains(lower)
                    }
                )
            }
            notifyDataSetChanged()
        }
}
