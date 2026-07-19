package com.example.sistemkasir.model

import java.util.Date

data class Laporan(
    val id: String = "", // Diubah menjadi String agar bisa menampung ID Dokumen Firestore
    val tanggal: Date,
    val jenis: String,
    val totalPenjualan: Double
)