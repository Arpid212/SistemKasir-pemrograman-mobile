package com.example.sistemkasir.model

data class DetailTransaksi(
    val id: Int = 0,
    val idTransaksi: Int, // Foreign key ke tabel Transaksi
    val idProduk: Int, // Foreign key ke tabel Produk
    val quantity: Int,
    val hargaSatuan: Double,
    val subtotal: Double
)