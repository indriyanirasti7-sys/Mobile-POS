package com.rasti.selaraspos.model

import android.os.Parcel
import android.os.Parcelable

data class ModelProduk(

    val idProduk: String = "",

    val namaProduk: String = "",

    val skuProduk: String = "",

    val barcodeProduk: String = "",

    val kategoriProduk: String = "",

    val cabangProduk: String = "",

    val hargaBeli: Int = 0,

    val nilaiProfit: Int = 0,

    val hargaJual: Int = 0,

    val stokProduk: Int = 0,

    val stokTakTerbatas: Boolean = false,

    // DITAMBAHKAN
    val fotoProduk: String = "",

    // DITAMBAHKAN
    val createdAt: String = ""

) : Parcelable {

    // tetap dipakai
    var jumlahTerjual: Int = 0

    // =========================
    // CONSTRUCTOR PARCEL
    // =========================

    constructor(parcel: Parcel) : this(

        idProduk =
            parcel.readString() ?: "",

        namaProduk =
            parcel.readString() ?: "",

        skuProduk =
            parcel.readString() ?: "",

        barcodeProduk =
            parcel.readString() ?: "",

        kategoriProduk =
            parcel.readString() ?: "",

        cabangProduk =
            parcel.readString() ?: "",

        hargaBeli =
            parcel.readInt(),

        nilaiProfit =
            parcel.readInt(),

        hargaJual =
            parcel.readInt(),

        stokProduk =
            parcel.readInt(),

        stokTakTerbatas =
            parcel.readByte() != 0.toByte(),

        // DITAMBAHKAN
        fotoProduk =
            parcel.readString() ?: "",

        // DITAMBAHKAN
        createdAt =
            parcel.readString() ?: ""
    ) {

        jumlahTerjual =
            parcel.readInt()
    }

    // =========================
    // WRITE PARCEL
    // =========================

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int
    ) {

        parcel.writeString(idProduk)

        parcel.writeString(namaProduk)

        parcel.writeString(skuProduk)

        parcel.writeString(barcodeProduk)

        parcel.writeString(kategoriProduk)

        parcel.writeString(cabangProduk)

        parcel.writeInt(hargaBeli)

        parcel.writeInt(nilaiProfit)

        parcel.writeInt(hargaJual)

        parcel.writeInt(stokProduk)

        parcel.writeByte(
            if (stokTakTerbatas) 1 else 0
        )

        // DITAMBAHKAN
        parcel.writeString(fotoProduk)

        // DITAMBAHKAN
        parcel.writeString(createdAt)

        parcel.writeInt(jumlahTerjual)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR :
        Parcelable.Creator<ModelProduk> {

        override fun createFromParcel(
            parcel: Parcel
        ): ModelProduk {

            return ModelProduk(parcel)
        }

        override fun newArray(
            size: Int
        ): Array<ModelProduk?> {

            return arrayOfNulls(size)
        }
    }
}