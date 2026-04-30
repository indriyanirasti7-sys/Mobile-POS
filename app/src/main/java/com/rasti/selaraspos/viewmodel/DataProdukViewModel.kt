package com.rasti.selaraspos.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasti.selaraspos.model.ModelProduk

class DataProdukViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()

    // Referensi ke tabel "produk" di Firebase
    private val myRef = database.getReference("produk")

    val produkList = MutableLiveData<ArrayList<ModelProduk>>()
    private var originalProdukList = ArrayList<ModelProduk>()

    private val searchQuery = MutableLiveData<String?>()
    val isLoading = MutableLiveData<Boolean>()
    val isSearchEmpty = MutableLiveData<Boolean>()

    init {
        getData()
    }

    fun getData() {
        isLoading.value = true
        // Pastikan di Firebase kamu menggunakan "idProduk" sebagai child-nya
        val query = myRef.orderByChild("idProduk").limitToLast(100)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading.value = false
                if (snapshot.exists()) {
                    val list = ArrayList<ModelProduk>()
                    for (dataSnapshot in snapshot.children) {
                        val produk = dataSnapshot.getValue(ModelProduk::class.java)
                        if (produk == null) {
                            Log.e("DataProdukViewModel", "Failed to parse produk")
                        } else {
                            list.add(produk)
                        }
                    }
                    originalProdukList.clear()
                    originalProdukList.addAll(list)
                    produkList.value = list
                    isSearchEmpty.value = false
                    Log.d("DataProdukViewModel", "Loaded ${list.size} produk items.")
                } else {
                    originalProdukList.clear()
                    produkList.value = ArrayList()
                    isSearchEmpty.value = true
                    Log.d("DataProdukViewModel", "No produk data found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
                Log.e("DataProdukViewModel", "Database error: ${error.message}")
            }
        })
    }

    fun filterList(query: String?) {
        searchQuery.value = query
        if (query.isNullOrEmpty()) {
            produkList.value = originalProdukList
            isSearchEmpty.value = false
        } else {
            val filteredList = originalProdukList.filter {
                // Pastikan nama variabel di ModelProduk adalah namaProduk
                it.namaProduk?.lowercase()?.contains(query.lowercase()) == true
            }
            produkList.value = ArrayList(filteredList)
            isSearchEmpty.value = filteredList.isEmpty()
        }
    }
}