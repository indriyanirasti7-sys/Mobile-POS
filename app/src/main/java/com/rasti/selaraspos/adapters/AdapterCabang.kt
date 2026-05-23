package com.rasti.selaraspos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.model.ModelCabang
import com.rasti.selaraspos.R


    class AdapterCabang(
        private var listCabang: MutableList<ModelCabang>,
        private val onEditClick: (ModelCabang) -> Unit
    ) : RecyclerView.Adapter<AdapterCabang.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNama: TextView = itemView.findViewById(R.id.tvNamaCabangItem)
            val tvAlamat: TextView = itemView.findViewById(R.id.tvAlamatCabangItem)
            val tvTelepon: TextView = itemView.findViewById(R.id.tvTeleponCabangItem)
            val tvPJ: TextView = itemView.findViewById(R.id.tvPJCabangItem)
            val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditCabangItem)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cabang, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val cabang = listCabang[position]

            holder.tvNama.text    = cabang.namaCabang
            holder.tvAlamat.text  = cabang.alamatCabang.ifEmpty { "Alamat belum diisi" }
            holder.tvTelepon.text = cabang.teleponCabang.ifEmpty { "–" }
            holder.tvPJ.text      = cabang.penanggungJawab.ifEmpty { "–" }

            holder.btnEdit.setOnClickListener { onEditClick(cabang) }
            holder.itemView.setOnClickListener { onEditClick(cabang) }
        }

        override fun getItemCount(): Int = listCabang.size

        fun updateData(data: List<ModelCabang>) {
            listCabang.clear()
            listCabang.addAll(data)
            notifyDataSetChanged()
        }

        fun filter(keyword: String, dataOriginal: List<ModelCabang>) {
            listCabang.clear()
            if (keyword.isEmpty()) {
                listCabang.addAll(dataOriginal)
            } else {
                val lower = keyword.lowercase()
                listCabang.addAll(
                    dataOriginal.filter {
                        it.namaCabang.lowercase().contains(lower) ||
                                it.alamatCabang.lowercase().contains(lower) ||
                                it.penanggungJawab.lowercase().contains(lower)
                    }
                )
            }
            notifyDataSetChanged()
        }
}
