package com.rasti.selaraspos.model

data class ModelTransaksi(
    var idTransaksi: String = "",
    var tanggal: String = "",
    var total: Long = 0,
    var metodePembayaran: String = "",
    var uangBayar: Long = 0,
    var kembalian: Long = 0,
    var namaKasir: String = "",
    var cabang: String = "",
    // Ini sekarang akan terbaca karena file ModelDetailTransaksi sudah ada
    var detailProduk: Map<String, ModelDetailTransaksi> = emptyMap()
)