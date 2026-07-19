package com.example.sistemkasir.ui.kasir

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sistemkasir.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class CheckoutQrisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout_qris)

        // 1. Inisialisasi View dari XML kamu
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarQris)
        val tvTotalBayarQris = findViewById<TextView>(R.id.tvTotalBayarQris)
        val btnVerifikasi = findViewById<MaterialButton>(R.id.btnVerifikasiQris)

        // 2. Aksi tombol kembali di kiri atas toolbar
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // 3. Tangkap total tagihan dari CheckoutActivity
        val totalAkhir = intent.getDoubleExtra("TOTAL_TAGIHAN", 0.0)

        // 4. Format dan tampilkan nominal tagihan ke rupiah
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvTotalBayarQris.text = formatRupiah.format(totalAkhir).replace("Rp", "Rp ")

        // 5. Aksi saat tombol VERIFIKASI PEMBAYARAN ditekan
        btnVerifikasi.setOnClickListener {
            // Jalankan logika simpan transaksi atau cetak struk di sini
            Toast.makeText(this, "Pembayaran QRIS Berhasil Diverifikasi!", Toast.LENGTH_SHORT).show()

            // Contoh: Menutup halaman dan kembali ke menu utama kasir
            finish()
        }
    }
}