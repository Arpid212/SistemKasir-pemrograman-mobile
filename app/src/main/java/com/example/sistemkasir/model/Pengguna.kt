package com.example.sistemkasir.model

/**
 * FUNGSI DIREKTORI:
 * Folder ini khusus untuk menyimpan 'Data Class'.
 * Bertugas sebagai kerangka data (blueprint) yang akan dikirim ke/dari database.
 * Pantangan: Dilarang memasukkan kode yang berhubungan dengan UI (seperti TextView, Button) di folder ini.
 */
data class Pengguna(
    val id: Int,
    val nama: String,
    val email: String,
    val pin: String,
    val role: String // Berisi "Admin" atau "Kasir"
)