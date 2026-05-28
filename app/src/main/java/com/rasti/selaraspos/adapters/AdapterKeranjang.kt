package com.selaraspos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelKeranjang
import java.text.NumberFormat
import java.util.Locale

class AdapterKeranjang(
    private val listKeranjang: MutableList<ModelKeranjang>,
    private val onQtyChange: (ModelKeranjang, Int) -> Unit,
    private val onHapusItem: (ModelKeranjang) -> Unit
) : RecyclerView.Adapter<AdapterKeranjang.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNamaProdukKeranjang)
        val tvHarga: TextView = view.findViewById(R.id.tvHargaKeranjang)
        val tvQty: TextView = view.findViewById(R.id.tvQtyKeranjang)
        val tvSubtotal: TextView = view.findViewById(R.id.tvSubtotalKeranjang)
        val btnTambah: ImageButton = view.findViewById(R.id.btnTambahQty)
        val btnKurang: ImageButton = view.findViewById(R.id.btnKurangQty)
        val btnHapus: ImageButton = view.findViewById(R.id.btnHapusItem)

        fun bind(item: ModelKeranjang) {
            tvNama.text = item.namaProduk
            tvHarga.text = formatRupiah(item.hargaJual)
            tvQty.text = item.qty.toString()
            tvSubtotal.text = formatRupiah(item.subtotal)

            btnTambah.setOnClickListener { onQtyChange(item, 1) }
            btnKurang.setOnClickListener { onQtyChange(item, -1) }
            btnHapus.setOnClickListener { onHapusItem(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_keranjang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(listKeranjang[position])
    override fun getItemCount(): Int = listKeranjang.size

    fun updateData(data: List<ModelKeranjang>) {
        listKeranjang.clear()
        listKeranjang.addAll(data)
        notifyDataSetChanged()
    }

    private fun formatRupiah(harga: Long) = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(harga)
}