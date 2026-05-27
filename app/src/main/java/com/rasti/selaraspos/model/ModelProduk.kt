package com.rasti.selaraspos.model

data class ModelProduk(
    var idProduk: String? = null,
    var namaProduk: String? = null,
    var skuProduk: String? = null,
    var barcodeProduk: String? = null,
    var kategoriProduk: String? = null,
    var cabangProduk: String? = null,
    var fotoProduk: String? = null,
    var hargaBeli: Int = 0,
    var nilaiProfit: Int = 0,
    var hargaJual: Int = 0,
    var stokProduk: Int = 0,
    var stokTakTerbatas: Boolean = false
)