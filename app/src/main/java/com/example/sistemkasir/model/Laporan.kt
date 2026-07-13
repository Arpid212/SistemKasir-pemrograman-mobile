package com.example.sistemkasir.model

import java.util.Date

data class Laporan(
    val id: Int = 0,
    val tanggal: Date,
    val jenis: String,
    val totalPenjualan: Double
)