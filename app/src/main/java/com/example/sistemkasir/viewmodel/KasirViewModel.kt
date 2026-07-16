package com.example.sistemkasir.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sistemkasir.model.ItemKeranjang
import com.example.sistemkasir.model.Produk

class KasirViewModel : ViewModel() {

    private val _keranjang = MutableLiveData<List<ItemKeranjang>>(emptyList())
    val keranjang: LiveData<List<ItemKeranjang>> get() = _keranjang

    private val _subtotal = MutableLiveData<Double>(0.0)
    val subtotal: LiveData<Double> get() = _subtotal

    fun tambahKeKeranjang(produk: Produk) {
        val daftarSekarang = _keranjang.value?.toMutableList() ?: mutableListOf()
        val indexProduk = daftarSekarang.indexOfFirst { it.produk.id == produk.id }

        if (indexProduk != -1) {
            val itemLama = daftarSekarang[indexProduk]
            daftarSekarang[indexProduk] = itemLama.copy(kuantitas = itemLama.kuantitas + 1)
        } else {
            daftarSekarang.add(ItemKeranjang(produk, 1))
        }

        _keranjang.value = daftarSekarang
        perbaruiSubtotal(daftarSekarang)
    }

    fun kurangiDariKeranjang(produk: Produk) {
        val daftarSekarang = _keranjang.value?.toMutableList() ?: return
        val indexProduk = daftarSekarang.indexOfFirst { it.produk.id == produk.id }

        if (indexProduk != -1) {
            val itemLama = daftarSekarang[indexProduk]
            if (itemLama.kuantitas > 1) {
                daftarSekarang[indexProduk] = itemLama.copy(kuantitas = itemLama.kuantitas - 1)
            } else {
                daftarSekarang.removeAt(indexProduk)
            }

            _keranjang.value = daftarSekarang
            perbaruiSubtotal(daftarSekarang)
        }
    }

    private fun perbaruiSubtotal(daftar: List<ItemKeranjang>) {
        var total = 0.0
        for (item in daftar) {
            total += (item.produk.harga * item.kuantitas)
        }
        _subtotal.value = total
    }

    fun bersihkanKeranjang() {
        _keranjang.value = emptyList()
        _subtotal.value = 0.0
    }
}