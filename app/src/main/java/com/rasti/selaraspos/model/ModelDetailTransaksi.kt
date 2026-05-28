package com.rasti.selaraspos.model

/**
 * Model data untuk Detail Transaksi (per item produk)
 */
data class ModelDetailTransaksi(
    var idProduk: String = "",
    var namaProduk: String = "",
    var hargaJual: Long = 0L,
    var qty: Int = 0,
    var subtotal: Long = 0L
)