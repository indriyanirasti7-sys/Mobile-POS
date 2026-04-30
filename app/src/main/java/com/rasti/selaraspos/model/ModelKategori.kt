package com.rasti.selaraspos.model


import android.os.Parcel
import android.os.Parcelable
import java.sql.ClientInfoStatus

data class ModelKategori(
    var idkategori: String? = null,
    var namaKategori: String? = null,
    var status: String? = null
): Parcelable {
    constructor(parcel: Parcel): this(
        idkategori = parcel.readString(),
        namaKategori = parcel.readString(),
        status = parcel.readString()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idkategori)
        parcel.writeString(namaKategori)
        parcel.writeString(status)
    }
    companion object CREATOR : Parcelable.Creator<ModelKategori> {
        override fun createFromParcel(parcel: Parcel): ModelKategori {
            return ModelKategori(parcel)
        }

        override fun newArray(size: Int): Array<ModelKategori?> {
            return arrayOfNulls(size)
        }
    }
}



