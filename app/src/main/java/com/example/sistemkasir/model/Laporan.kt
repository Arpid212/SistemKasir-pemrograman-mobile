package com.example.sistemkasir.model

import java.util.Date

data class Laporan(
    val id: String = "",
    val detailPesanan: String = "",
    val tanggal: Date,
    val jenis: String,
    val totalPenjualan: Double
)