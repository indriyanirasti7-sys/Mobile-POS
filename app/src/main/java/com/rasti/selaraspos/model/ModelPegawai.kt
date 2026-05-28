package com.rasti.selaraspos.model

data class ModelPegawai(
    var idPegawai: String = "",
    var namaPegawai: String = "",
    var role: String = "",
    var noHp: String = "",
    var alamat: String = "",
    var email: String = "",
    var fotoPegawai: String = "",
    var status: String = "aktif"  // 🔥 TAMBAHKAN INI (aktif / tidak_aktif)
)