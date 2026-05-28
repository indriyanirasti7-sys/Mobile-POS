package com.rasti.selaraspos.model

/**
 * Model data untuk Produk
 * Digunakan untuk menyimpan dan mengambil data produk dari Firebase
 */
data class ModelProduk(
    var idProduk: String = "",
    var namaProduk: String = "",
    var hargaJual: Long = 0,
    var stokProduk: Int = 0,
    var kategoriProduk: String = "",
    var fotoProduk: String = ""
)