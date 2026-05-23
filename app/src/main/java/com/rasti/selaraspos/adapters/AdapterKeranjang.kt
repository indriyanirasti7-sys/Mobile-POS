package com.rasti.selaraspos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.model.ModelKeranjang
import com.rasti.selaraspos.R
import java.text.NumberFormat
import java.util.Locale

    class AdapterKeranjang(
        private val listKeranjang: MutableList<ModelKeranjang>,
        private val onQtyChanged: () -> Unit,           // callback hitung ulang total
        private val onItemHapus: (Int) -> Unit          // callback hapus item by position
    ) : RecyclerView.Adapter<AdapterKeranjang.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNama: TextView = itemView.findViewById(R.id.tvNamaProdukKeranjang)
            val tvHarga: TextView = itemView.findViewById(R.id.tvHargaKeranjang)
            val tvQty: TextView = itemView.findViewById(R.id.tvQty)
            val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotalItem)
            val btnPlus: ImageButton = itemView.findViewById(R.id.btnPlus)
            val btnMinus: ImageButton = itemView.findViewById(R.id.btnMinus)
            val btnHapus: ImageButton = itemView.findViewById(R.id.btnHapusItem)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_keranjang, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = listKeranjang[position]

            holder.tvNama.text     = item.namaProduk
            holder.tvHarga.text    = formatRupiah(item.hargaSatuan)
            holder.tvQty.text      = item.qty.toString()
            holder.tvSubtotal.text = formatRupiah(item.subtotal)

            // Tombol PLUS: tambah qty
            holder.btnPlus.setOnClickListener {
                item.qty++
                item.hitungSubtotal()
                notifyItemChanged(position)
                onQtyChanged()
            }

            // Tombol MINUS: kurangi qty, minimal 1
            holder.btnMinus.setOnClickListener {
                if (item.qty > 1) {
                    item.qty--
                    item.hitungSubtotal()
                    notifyItemChanged(position)
                    onQtyChanged()
                }
            }

            // Tombol HAPUS: remove item dari keranjang
            holder.btnHapus.setOnClickListener {
                onItemHapus(position)
            }
        }

        override fun getItemCount(): Int = listKeranjang.size

        // ===== HELPER =====
        private fun  formatRupiah(nominal: Long): String {
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            return format.format(nominal).replace("Rp", "Rp ").replace(",00", "")
        }

        /**
         * Hitung total semua subtotal item di keranjang
         */
        fun hitungTotal(): Long = listKeranjang.sumOf { it.subtotal }
}