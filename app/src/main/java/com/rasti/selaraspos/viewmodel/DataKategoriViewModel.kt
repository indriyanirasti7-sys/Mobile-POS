package com.rasti.selaraspos.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import androidx.lifecycle.ViewModel
import android.util.Log
import com.rasti.selaraspos.model.ModelKategori

class DataKategoriViewModel: ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef= database.getReference("kategori")
    val kategoriList = MutableLiveData<ArrayList<ModelKategori>>()
    private  var originalkategoriList = ArrayList<ModelKategori>()
    private val searchQuery = MutableLiveData<String?>()
    val isLoading = MutableLiveData<Boolean>()
    val isSearchEmty = MutableLiveData<Boolean>()
    init{
        getData()
    }
    fun getData(){
        isLoading.value = true
        val query = myRef.orderByChild("idkategori").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading.value = false
                if (snapshot.exists()) {
                    val list = ArrayList<ModelKategori>()
                    for (dataSnapshot in snapshot.children) {
                        val kategori = dataSnapshot.getValue(ModelKategori::class.java)
                        if (kategori == null) {
                            Log.e("DataKategoriViewModel", "Failed to purpose kategori")
                        } else {
                            list.add(kategori)
                        }
                    }
                    originalkategoriList.clear()
                    originalkategoriList.addAll(list)
                    kategoriList.value = list
                    isSearchEmty.value = false
                    Log.d("DataKategoriViewModel", "Loaded ${list.size} kategori items.")
                } else {
                    originalkategoriList.clear()
                    kategoriList.value = ArrayList()
                    isSearchEmty.value = true
                    Log.d("DataKategoriViewModel", "No kategori data found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
            }
        })
    }

    fun filterList(query: String?) {
        searchQuery.value = query
        if (query.isNullOrEmpty()) {
            kategoriList.value = originalkategoriList
            isSearchEmty.value = false
        } else {
            val filteredList = originalkategoriList.filter {
                it.namaKategori?.lowercase()?.contains(query.lowercase()) == true
            }
            kategoriList.value = ArrayList(filteredList)
            isSearchEmty.value = filteredList.isEmpty()
        }
                }

    }
