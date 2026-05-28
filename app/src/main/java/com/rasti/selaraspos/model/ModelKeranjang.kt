package com.rasti.selaraspos.model

import kotlin.times

/**
 * Model data untuk Keranjang Belanja (sementara, tidak disimpan ke Firebase)
 */
data class ModelKeranjang(
    var idProduk: String = "",
    var namaProduk: String = "",
    var hargaJual: Long = 0,
    var qty: Int = 1,
    var subtotal: Long = 0,
    var fotoProduk: String = ""
)