package com.example.sistemkasir.model

data class Pengguna(
    val id: Int,
    val nama: String,
    val email: String,
    val pin: String,
    val role: String
)