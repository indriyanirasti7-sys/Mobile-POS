package com.rasti.selaraspos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.rasti.selaraspos.model.ModelProduk

class DataProdukViewModel : ViewModel() {

    private val database =
        FirebaseDatabase.getInstance(
            "https://selaraspos-6ce12-default-rtdb.firebaseio.com/"
        )

    private val myRef =
        database.getReference("produk")

    private val _produkList =
        MutableLiveData<List<ModelProduk>>()

    val produkList: LiveData<List<ModelProduk>> =
        _produkList

    private val _isLoading =
        MutableLiveData<Boolean>()

    val isLoading: LiveData<Boolean> =
        _isLoading

    // Simpan semua data asli untuk filter
    private val allProdukList =
        mutableListOf<ModelProduk>()

    private var listener: ValueEventListener? = null

    fun getData() {

        _isLoading.value = true

        listener = object : ValueEventListener {

            override fun onDataChange(
                snapshot: DataSnapshot
            ) {

                allProdukList.clear()

                for (data in snapshot.children) {

                    val produk =
                        data.getValue(
                            ModelProduk::class.java
                        )

                    if (produk != null) {

                        allProdukList.add(produk)
                    }
                }

                _produkList.value =
                    allProdukList.toList()

                _isLoading.value = false
            }

            override fun onCancelled(
                error: DatabaseError
            ) {

                _isLoading.value = false
            }
        }

        myRef.addValueEventListener(listener!!)
    }

    fun filterList(query: String) {

        if (query.isEmpty()) {

            _produkList.value =
                allProdukList.toList()

            return
        }

        val filtered = allProdukList.filter { produk ->
            val namaMatch = produk.namaProduk?.contains(query, ignoreCase = true) == true
            val kategoriMatch = produk.kategoriProduk?.contains(query, ignoreCase = true) == true
            val skuMatch = produk.skuProduk?.contains(query, ignoreCase = true) == true

            namaMatch || kategoriMatch || skuMatch
        }

        _produkList.value = filtered
    }

    override fun onCleared() {

        super.onCleared()

        // Hapus listener saat ViewModel destroyed
        listener?.let {
            myRef.removeEventListener(it)
        }
    }
}