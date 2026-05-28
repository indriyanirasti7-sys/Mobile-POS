package com.rasti.selaraspos.model

/**
 * Model data untuk Akun Pengguna
 */
data class ModelAkun(
    var uid: String = "",
    var namaUser: String = "",
    var email: String = "",
    var role: String = "kasir",   // "admin" atau "kasir"
    var fotoProfil: String = ""
)
