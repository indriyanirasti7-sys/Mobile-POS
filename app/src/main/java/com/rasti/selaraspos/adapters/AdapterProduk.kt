package com.selaraspos.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelProduk
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import kotlin.concurrent.thread

class AdapterProduk(
    private val listProduk: MutableList<ModelProduk>,
    private val onEditClick: (ModelProduk) -> Unit,
    private val onHapusClick: (ModelProduk) -> Unit
) : RecyclerView.Adapter<AdapterProduk.ViewHolder>() {

    private val listAsli: MutableList<ModelProduk> = mutableListOf()

    init {
        listAsli.addAll(listProduk)
    }

    // ViewHolder manual menggunakan findViewById
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduk: ImageView = view.findViewById(R.id.imgProduk)
        val tvNamaProduk: TextView = view.findViewById(R.id.tvNamaProduk)
        val tvKategoriProduk: TextView = view.findViewById(R.id.tvKategoriProduk)
        val tvStokProduk: TextView = view.findViewById(R.id.tvStokProduk)
        val tvHargaProduk: TextView = view.findViewById(R.id.tvHargaProduk)
        val btnEditProduk: ImageButton = view.findViewById(R.id.btnEditProduk)
        val btnHapusProduk: ImageButton = view.findViewById(R.id.btnHapusProduk)

        fun bind(produk: ModelProduk) {
            tvNamaProduk.text = produk.namaProduk
            tvKategoriProduk.text = produk.kategoriProduk
            tvStokProduk.text = "Stok: ${produk.stokProduk}"
            tvHargaProduk.text = formatRupiah(produk.hargaJual)

            // Download manual
            if (produk.fotoProduk.isNotEmpty()) {
                imgProduk.setImageResource(R.drawable.placeholder)
                thread {
                    try {
                        val url = URL(produk.fotoProduk)
                        val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                        imgProduk.post { imgProduk.setImageBitmap(bmp) }
                    } catch (e: Exception) {
                        imgProduk.post { imgProduk.setImageResource(R.drawable.placeholder) }
                    }
                }
            } else {
                imgProduk.setImageResource(R.drawable.placeholder)
            }

            btnEditProduk.setOnClickListener { onEditClick(produk) }
            btnHapusProduk.setOnClickListener { onHapusClick(produk) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Menggunakan inflator standar
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_produk, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listProduk[position])
    }

    override fun getItemCount(): Int = listProduk.size

    // ... (Fungsi filter, updateData, formatRupiah tetap sama) ...
    fun filter(query: String) {
        listProduk.clear()
        if (query.isEmpty()) listProduk.addAll(listAsli)
        else {
            val keyword = query.lowercase()
            listProduk.addAll(listAsli.filter {
                it.namaProduk.lowercase().contains(keyword) || it.kategoriProduk.lowercase().contains(keyword)
            })
        }
        notifyDataSetChanged()
    }

    fun updateData(data: List<ModelProduk>) {
        listProduk.clear()
        listAsli.clear()
        listProduk.addAll(data)
        listAsli.addAll(data)
        notifyDataSetChanged()
    }

    private fun formatRupiah(harga: Long): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(harga)
    }
}