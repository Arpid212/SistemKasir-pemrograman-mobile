package com.example.sistemkasir.model


data class Produk(
    val id: Int = 0,
    val nama: String,
    val harga: Double,
    val deskripsi: String,
    val stok: Int,
    val foto: String, // Bisa berupa URL atau path gambar lokal
    val kategori: String // Berisi "Kopi", "Non-Kopi", atau "Makanan"
)

