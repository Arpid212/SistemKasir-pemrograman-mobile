package com.example.sistemkasir.utils

object GeneratorUtil {

    fun generateEmail(namaLengkap: String): String{
        val ambilNama = namaLengkap.trim().lowercase()

        val kata = ambilNama.split(" ")

        val namaDepan = kata.first().take(3)
        val namaAkhir = kata.last().take(3)

        return "$namaDepan$namaAkhir@kafe.com"
    }

    fun generatePin(): String{
        val angkaRandom = (100000..999999).random()
        return angkaRandom.toString()
    }

}