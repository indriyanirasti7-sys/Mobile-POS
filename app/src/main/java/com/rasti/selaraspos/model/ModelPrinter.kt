package com.rasti.selaraspos.model

/**
 * Model data untuk Printer Bluetooth
 */
data class ModelPrinter(
    var namaPrinter: String = "",
    var alamatMac: String = "",
    var statusKoneksi: Boolean = false
)

