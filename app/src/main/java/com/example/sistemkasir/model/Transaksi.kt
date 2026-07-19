package com.example.sistemkasir.model

import java.util.Date

data class Transaksi(
    val id: Int = 0,
    val nomorStruk: String,
    val metodePembayaran: String,
    val tanggal: String,
    val totalHarga: Double,
    val nominalBayar: Double,
    val kembalian: Double,
    val status: String
)