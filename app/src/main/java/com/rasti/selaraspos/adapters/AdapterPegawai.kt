package com.rasti.selaraspos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelPegawai

class AdapterPegawai(
    private val listPegawai: MutableList<ModelPegawai>,
    private val onEditClick: (ModelPegawai) -> Unit,
    private val onHapusClick: (ModelPegawai) -> Unit
) : RecyclerView.Adapter<AdapterPegawai.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Inisialisasi komponen UI manual
        val tvNama: TextView = view.findViewById(R.id.tvNamaPegawai)
        val tvRole: TextView = view.findViewById(R.id.tvRolePegawai)
        val tvHp: TextView = view.findViewById(R.id.tvHpPegawai)
        val tvAlamat: TextView = view.findViewById(R.id.tvAlamatPegawai)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditPegawai)
        val btnHapus: ImageButton = view.findViewById(R.id.btnHapusPegawai)

        fun bind(pegawai: ModelPegawai) {
            tvNama.text = pegawai.namaPegawai
            tvRole.text = pegawai.role.replaceFirstChar { it.uppercase() }
            tvHp.text = pegawai.noHp
            tvAlamat.text = pegawai.alamat

            btnEdit.setOnClickListener { onEditClick(pegawai) }
            btnHapus.setOnClickListener { onHapusClick(pegawai) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout manual menggunakan R.layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listPegawai[position])
    }

    override fun getItemCount(): Int = listPegawai.size

    fun updateData(data: List<ModelPegawai>) {
        listPegawai.clear()
        listPegawai.addAll(data)
        notifyDataSetChanged()
    }

    fun filter(query: String, originalList: List<ModelPegawai>) {
        listPegawai.clear()
        if (query.isEmpty()) {
            listPegawai.addAll(originalList)
        } else {
            val lowerCaseQuery = query.lowercase()
            val filtered = originalList.filter {
                it.namaPegawai.lowercase().contains(lowerCaseQuery) ||
                        it.noHp.contains(lowerCaseQuery)
            }
            listPegawai.addAll(filtered)
        }
        notifyDataSetChanged()
    }
}