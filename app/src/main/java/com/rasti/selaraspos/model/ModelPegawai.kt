package com.rasti.selaraspos.model

/**
 * Model data untuk Pegawai
 */
data class ModelPegawai(
    var idPegawai: String = "",
    var namaPegawai: String = "",
    var role: String = "kasir",   // "admin" atau "kasir"
    var noHp: String = "",
    var alamat: String = "",
    var email: String = ""
)