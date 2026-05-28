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
import com.rasti.selaraspos.model.ModelKategori
import com.rasti.selaraspos.model.ModelProduk
import java.text.NumberFormat
import java.util.Locale

// ─── Adapter Produk ───────────────────────────────────────────────────────────

class AdapterProduk(
    private val listProduk: MutableList<ModelProduk>,
    private val isAdmin: Boolean,                        // ← kontrol tampil tombol edit/hapus
    private val onEditClick: (ModelProduk) -> Unit,
    private val onHapusClick: (ModelProduk) -> Unit
) : RecyclerView.Adapter<AdapterProduk.ViewHolder>() {

    private val listAsli = mutableListOf<ModelProduk>()
    init { listAsli.addAll(listProduk) }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduk: ImageView = itemView.findViewById(R.id.imgProduk)
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaProduk)
        val tvKategori: TextView = itemView.findViewById(R.id.tvKategoriProduk)
        val tvHarga: TextView = itemView.findViewById(R.id.tvHargaProduk)
        val tvStok: TextView = itemView.findViewById(R.id.tvStokProduk)
        val btnEdit: TextView = itemView.findViewById(R.id.btnEditProduk)
        val btnHapus: TextView = itemView.findViewById(R.id.btnHapusProduk)
        val layoutAksi: View = itemView.findViewById(R.id.layoutAksiProduk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_produk, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val produk = listProduk[position]
        holder.tvNama.text = produk.namaProduk
        holder.tvKategori.text = produk.kategoriProduk
        holder.tvHarga.text = formatRupiah(produk.hargaJual)
        holder.tvStok.text = "Stok: ${produk.stokProduk}"

        // Load gambar dari URL
        if (produk.fotoProduk.isNotEmpty()) {
            Glide.with(holder.imgProduk.context)
                .load(produk.fotoProduk)
                .apply(RequestOptions().transform(RoundedCorners(14))
                    .placeholder(R.drawable.produk).error(R.drawable.produk))
                .into(holder.imgProduk)
        } else {
            holder.imgProduk.setImageResource(R.drawable.produk)
        }

        // Tampilkan tombol edit/hapus HANYA untuk admin
        holder.layoutAksi.visibility = if (isAdmin) View.VISIBLE else View.GONE

        holder.btnEdit.setOnClickListener { onEditClick(produk) }
        holder.btnHapus.setOnClickListener { onHapusClick(produk) }
    }

    override fun getItemCount() = listProduk.size

    fun filter(query: String) {
        listProduk.clear()
        if (query.isEmpty()) listProduk.addAll(listAsli)
        else listAsli.filter {
            it.namaProduk.lowercase().contains(query.lowercase())
        }.also { listProduk.addAll(it) }
        notifyDataSetChanged()
    }

    fun updateData(data: List<ModelProduk>) {
        listProduk.clear(); listAsli.clear()
        listProduk.addAll(data); listAsli.addAll(data)
        notifyDataSetChanged()
    }

    private fun formatRupiah(h: Long) =
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(h)
}

// ─── Adapter Kategori ─────────────────────────────────────────────────────────

