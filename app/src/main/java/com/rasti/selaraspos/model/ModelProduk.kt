package com.rasti.selaraspos.model

import android.os.Parcelable
import android.os.Parcel
// Hapus import ModelKategori di sini jika sudah satu folder

data class ModelProduk (
    val idProduk: String? = null,
    val namaProduk: String? = null,
    val hargaProduk: Int? = 0,
    val idKategori: String? = null,
    val idCabang: String? = null,
    val fotoProduk: String? = null,
    val stokProduk: Int? = 0,
    val tanpaBatas: Boolean? = false,
    val statusProduk: String? = null,
    val createdAt: String? = null,
    val updateAt: String? = null
): Parcelable {

    // Jika ingin menambah variabel di luar constructor
    var jumlahTerjual: Int = 0

    // Constructor untuk Parcelable
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(), // Cara baca boolean yang lebih aman
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idProduk)
        parcel.writeString(namaProduk)
        parcel.writeInt(hargaProduk ?: 0)
        parcel.writeString(idKategori)
        parcel.writeString(idCabang)
        parcel.writeString(fotoProduk)
        parcel.writeInt(stokProduk ?: 0)
        parcel.writeByte(if (tanpaBatas == true) 1 else 0)
        parcel.writeString(statusProduk)
        parcel.writeString(createdAt)
        parcel.writeString(updateAt)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ModelProduk> {
        override fun createFromParcel(parcel: Parcel): ModelProduk = ModelProduk(parcel)
        override fun newArray(size: Int): Array<ModelProduk?> = arrayOfNulls(size)
    }
}