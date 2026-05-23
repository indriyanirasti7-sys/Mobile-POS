package com.rasti.selaraspos.model

data class ModelAkun (
    var uid: String = "",
    var namaPengguna: String = "",
    var email: String = "",
    var role: String = "",     // admin / kasir
    var fotoProfil: String = "", // URL Firebase Storage
    var idCabang: String = ""


)