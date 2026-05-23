package com.rasti.selaraspos.adapters

import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.ModelProduk
import com.rasti.selaraspos.R
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import kotlin.concurrent.thread

class AdapterProdukTransaksi(
    private var listProduk: MutableList<ModelProduk>,
    private val onTambahClick: (ModelProduk) -> Unit
) : RecyclerView.Adapter<AdapterProdukTransaksi.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivFoto: ImageView = itemView.findViewById(R.id.ivFotoProduk)
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaProduk)
        val tvHarga: TextView = itemView.findViewById(R.id.tvHargaProduk)
        val tvStok: TextView = itemView.findViewById(R.id.tvStokProduk)
        val btnTambah: Button = itemView.findViewById(R.id.btnTambahProduk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val produk = listProduk[position]

        holder.tvNama.text  = produk.namaProduk
        holder.tvHarga.text = formatRupiah(produk.hargaJual)
        holder.tvStok.text  = "Stok: ${produk.stokProduk}"

        // Load foto manual tanpa Glide
        if (!produk.fotoProduk.isNullOrEmpty()) {
            loadFotoManual(produk.fotoProduk, holder.ivFoto)
        } else {
            holder.ivFoto.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        if (produk.stokProduk <= 0) {
            holder.btnTambah.isEnabled = false
            holder.btnTambah.alpha     = 0.4f
            holder.btnTambah.text      = "Habis"
        } else {
            holder.btnTambah.isEnabled = true
            holder.btnTambah.alpha     = 1f
            holder.btnTambah.text      = "+ Tambah"
        }

        holder.btnTambah.setOnClickListener { onTambahClick(produk) }
    }

    // Fungsi untuk memuat gambar manual tanpa library
    private fun loadFotoManual(url: String, imageView: ImageView) {
        imageView.setImageResource(android.R.drawable.ic_menu_gallery) // Gambar saat loading
        thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()
                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Update UI di Main Thread
                Handler(Looper.getMainLooper()).post {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int = listProduk.size

    private fun formatRupiah(nominal: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(nominal).replace("Rp", "Rp ").replace(",00", "")
    }

    fun updateData(dataBaru: List<ModelProduk>) {
        listProduk.clear()
        listProduk.addAll(dataBaru)
        notifyDataSetChanged()
    }

    fun filter(keyword: String, dataOriginal: List<ModelProduk>) {
        listProduk.clear()
        if (keyword.isEmpty()) {
            listProduk.addAll(dataOriginal)
        } else {
            val lower = keyword.lowercase()
            listProduk.addAll(
                dataOriginal.filter {
                    it.namaProduk.lowercase().contains(lower) ||
                            (it.kategoriProduk?.lowercase()?.contains(lower) == true)
                }
            )
        }
        notifyDataSetChanged()
    }
}