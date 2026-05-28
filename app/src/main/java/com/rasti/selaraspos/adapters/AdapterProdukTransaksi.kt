package com.selaraspos.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelProduk
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import kotlin.concurrent.thread

class AdapterProdukTransaksi(
    private val listProduk: MutableList<ModelProduk>,
    private val onProdukClick: (ModelProduk) -> Unit
) : RecyclerView.Adapter<AdapterProdukTransaksi.ViewHolder>() {

    private val listAsli: MutableList<ModelProduk> = mutableListOf()

    init { listAsli.addAll(listProduk) }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProdukTrx: ImageView = view.findViewById(R.id.imgProdukTrx)
        val tvNamaProdukTrx: TextView = view.findViewById(R.id.tvNamaProdukTrx)
        val tvHargaProdukTrx: TextView = view.findViewById(R.id.tvHargaProdukTrx)
        val tvStokProdukTrx: TextView = view.findViewById(R.id.tvStokProdukTrx)

        fun bind(produk: ModelProduk) {
            tvNamaProdukTrx.text = produk.namaProduk
            tvHargaProdukTrx.text = formatRupiah(produk.hargaJual)
            tvStokProdukTrx.text = "Stok: ${produk.stokProduk}"

            // Download Gambar Manual
            imgProdukTrx.setImageResource(R.drawable.placeholder)
            if (produk.fotoProduk.isNotEmpty()) {
                thread {
                    try {
                        val bmp = BitmapFactory.decodeStream(URL(produk.fotoProduk).openStream())
                        imgProdukTrx.post { imgProdukTrx.setImageBitmap(bmp) }
                    } catch (e: Exception) { /* ignore */ }
                }
            }

            itemView.alpha = if (produk.stokProduk > 0) 1f else 0.4f
            itemView.isEnabled = produk.stokProduk > 0
            itemView.setOnClickListener { if (produk.stokProduk > 0) onProdukClick(produk) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_produk_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(listProduk[position])
    override fun getItemCount(): Int = listProduk.size

    fun filter(query: String) {
        listProduk.clear()
        if (query.isEmpty()) listProduk.addAll(listAsli)
        else listProduk.addAll(listAsli.filter { it.namaProduk.contains(query, true) })
        notifyDataSetChanged()
    }

    fun updateData(data: List<ModelProduk>) {
        listProduk.clear(); listAsli.clear()
        listProduk.addAll(data); listAsli.addAll(data)
        notifyDataSetChanged()
    }

    private fun formatRupiah(harga: Long) = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(harga)
}