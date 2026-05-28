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

class AdapterKeranjang(
    private val list: MutableList<ModelKeranjang>,
    private val onQtyChange: (ModelKeranjang, Int) -> Unit,
    private val onHapus: (ModelKeranjang) -> Unit
) : RecyclerView.Adapter<AdapterKeranjang.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvNama: TextView = v.findViewById(R.id.tvNamaProdukKeranjang)
        val tvHarga: TextView = v.findViewById(R.id.tvHargaKeranjang)
        val tvQty: TextView = v.findViewById(R.id.tvQtyKeranjang)
        val tvSubtotal: TextView = v.findViewById(R.id.tvSubtotalKeranjang)
        val btnPlus: TextView = v.findViewById(R.id.btnTambahQty)
        val btnMinus: TextView = v.findViewById(R.id.btnKurangQty)
        val btnHapus: ImageButton = v.findViewById(R.id.btnHapusItem)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(
        LayoutInflater.from(p.context).inflate(R.layout.item_keranjang, p, false)
    )

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = list[pos]
        h.tvNama.text = item.namaProduk
        h.tvHarga.text = formatRp(item.hargaJual)
        h.tvQty.text = item.qty.toString()
        h.tvSubtotal.text = formatRp(item.subtotal)
        h.btnPlus.setOnClickListener { onQtyChange(item, 1) }
        h.btnMinus.setOnClickListener { onQtyChange(item, -1) }
        h.btnHapus.setOnClickListener { onHapus(item) }
    }

    override fun getItemCount() = list.size

    fun updateData(data: List<ModelKeranjang>) {
        list.clear(); list.addAll(data); notifyDataSetChanged()
    }

    private fun formatRp(h: Long) = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(h)
}
