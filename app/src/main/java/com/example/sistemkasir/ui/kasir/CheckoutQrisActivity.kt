package com.example.sistemkasir.ui.kasir

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sistemkasir.R

class CheckoutQrisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout_qris)

        // Opsional: Jika Anda punya tombol Verifikasi di desain XML, Anda bisa aktifkan ini nanti
        /*
        val btnVerifikasi = findViewById<Button>(R.id.btnVerifikasiQris)
        btnVerifikasi.setOnClickListener {
            Toast.makeText(this, "Pembayaran QRIS Berhasil!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DashboardKasirActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        */
    }
}