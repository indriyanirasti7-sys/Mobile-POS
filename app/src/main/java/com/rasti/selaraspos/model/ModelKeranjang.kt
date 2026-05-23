package com.rasti.selaraspos.model

import kotlin.times

data class ModelKeranjang (
    var idProduk: String = "",
    var namaProduk: String = "",
    var hargaSatuan: Long = 0L,
    var qty: Int = 1,
    var subtotal: Long = 0L
) {
    fun hitungSubtotal() {
        subtotal = hargaSatuan * qty
    }
}