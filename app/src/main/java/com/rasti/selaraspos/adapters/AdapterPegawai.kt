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
class AdapterPegawai(
    private val list: MutableList<ModelPegawai>,
    private val onEdit: (ModelPegawai) -> Unit,
    private val onHapus: (ModelPegawai) -> Unit
) : RecyclerView.Adapter<AdapterPegawai.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvNama: TextView = v.findViewById(R.id.tvNamaPegawai)
        val tvRole: TextView = v.findViewById(R.id.tvRolePegawai)
        val tvHp: TextView = v.findViewById(R.id.tvHpPegawai)
        val tvAlamat: TextView = v.findViewById(R.id.tvAlamatPegawai)
        val btnEdit: TextView = v.findViewById(R.id.btnEditPegawai)
        val btnHapus: TextView = v.findViewById(R.id.btnHapusPegawai)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(
        LayoutInflater.from(p.context).inflate(R.layout.item_pegawai, p, false)
    )

    override fun onBindViewHolder(h: VH, pos: Int) {
        val d = list[pos]
        h.tvNama.text = d.namaPegawai
        h.tvRole.text = d.role.replaceFirstChar { it.uppercase() }
        h.tvHp.text = d.noHp
        h.tvAlamat.text = d.alamat
        h.btnEdit.setOnClickListener { onEdit(d) }
        h.btnHapus.setOnClickListener { onHapus(d) }
    }

    override fun getItemCount() = list.size

    fun updateData(data: List<ModelPegawai>) {
        list.clear(); list.addAll(data); notifyDataSetChanged()
    }
}
