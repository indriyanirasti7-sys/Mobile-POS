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


class AdapterProdukTransaksi(
    private val listProduk: MutableList<ModelProduk>,
    private val onKlik: (ModelProduk) -> Unit
) : RecyclerView.Adapter<AdapterProdukTransaksi.VH>() {

    private val listAsli = mutableListOf<ModelProduk>()
    init { listAsli.addAll(listProduk) }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgProdukTrx)
        val tvNama: TextView = v.findViewById(R.id.tvNamaProdukTrx)
        val tvHarga: TextView = v.findViewById(R.id.tvHargaProdukTrx)
        val tvStok: TextView = v.findViewById(R.id.tvStokProdukTrx)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(
        LayoutInflater.from(p.context).inflate(R.layout.item_produk_transaksi, p, false)
    )

    override fun onBindViewHolder(h: VH, pos: Int) {
        val p = listProduk[pos]
        h.tvNama.text = p.namaProduk
        h.tvHarga.text = formatRp(p.hargaJual)
        h.tvStok.text = "Stok: ${p.stokProduk}"
        h.itemView.alpha = if (p.stokProduk > 0) 1f else 0.4f
        h.itemView.isEnabled = p.stokProduk > 0

        Glide.with(h.img.context).load(p.fotoProduk)
            .apply(RequestOptions().transform(RoundedCorners(12))
                .placeholder(R.drawable.produk).error(R.drawable.produk))
            .into(h.img)

        h.itemView.setOnClickListener { if (p.stokProduk > 0) onKlik(p) }
    }

    override fun getItemCount() = listProduk.size

    fun filter(q: String) {
        listProduk.clear()
        if (q.isEmpty()) listProduk.addAll(listAsli)
        else listAsli.filter { it.namaProduk.lowercase().contains(q.lowercase()) }
            .also { listProduk.addAll(it) }
        notifyDataSetChanged()
    }

    fun filterKategori(kat: String) {
        listProduk.clear()
        if (kat == "Semua") listProduk.addAll(listAsli)
        else listAsli.filter { it.kategoriProduk == kat }.also { listProduk.addAll(it) }
        notifyDataSetChanged()
    }

    fun updateData(data: List<ModelProduk>) {
        listProduk.clear(); listAsli.clear()
        listProduk.addAll(data); listAsli.addAll(data)
        notifyDataSetChanged()
    }

    private fun formatRp(h: Long) = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(h)
}

