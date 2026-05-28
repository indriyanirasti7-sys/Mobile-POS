package com.rasti.selaraspos.model

// Gunakan kurung () untuk data class
/**
 * Model data untuk Laporan Penjualan
 */
data class ModelLaporan(
    var idTransaksi: String = "",
    var tanggal: String = "",
    var namaKasir: String = "",
    var cabang: String = "",
    var metodePembayaran: String = "",
    var total: Long = 0
)