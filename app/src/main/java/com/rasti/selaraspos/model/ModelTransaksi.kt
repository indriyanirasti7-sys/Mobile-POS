package com.rasti.selaraspos.model

/**
 * ModelTransaksi - ditambah field timestamp (Long) untuk sorting terbaru
 */
data class ModelTransaksi(
    var idTransaksi: String = "",
    var tanggal: String = "",
    var timestamp: Long = 0L,           // ← untuk sorting di Firebase
    var total: Long = 0L,
    var metodePembayaran: String = "",
    var uangBayar: Long = 0L,
    var kembalian: Long = 0L,
    var namaKasir: String = "",
    var cabang: String = "",
    var detailProduk: Map<String, ModelDetailTransaksi> = emptyMap()
)



