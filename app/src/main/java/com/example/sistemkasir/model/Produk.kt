package com.example.sistemkasir.model

data class Produk(
    var id: Int = 0,
    var nama: String = "",
    var harga: Double = 0.0,
    var deskripsi: String = "",
    var stok: Int = 0,
    var foto: String = "",
    var kategori: String = ""
)