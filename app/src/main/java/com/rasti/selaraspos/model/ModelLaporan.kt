package com.rasti.selaraspos.model

// Gunakan kurung () untuk data class
data class ModelLaporan(
    var idTransaksi: String = "",
    var tanggal: String = "",
    var tanggalMilis: Long = 0L,
    var total: Long = 0L,
    var metodePembayaran: String = "",
    var namaKasir: String = "",
    var cabang: String = ""
)