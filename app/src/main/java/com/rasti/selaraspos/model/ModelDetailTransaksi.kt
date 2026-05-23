package com.rasti.selaraspos.model

data class ModelDetailTransaksi (
    var idProduk: String = "",
    var namaProduk: String = "",
    var hargaSatuan: Long = 0L,
    var qty: Int = 1,
    var subtotal: Long = 0L
)