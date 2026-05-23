package com.rasti.selaraspos.model

import com.rasti.selaraspos.model.ModelDetailTransaksi

data class ModelTransaksi (
    var idTransaksi: String = "",
    var tanggal: String = "",
    var tanggalMilis: Long = 0L,       // untuk sorting & filter
    var total: Long = 0L,
    var metodePembayaran: String = "", // Cash / QRIS / Transfer
    var namaKasir: String = "",
    var idKasir: String = "",
    var cabang: String = "",
    var idCabang: String = "",
    var detailProduk: Map<String, ModelDetailTransaksi> = emptyMap()

)

