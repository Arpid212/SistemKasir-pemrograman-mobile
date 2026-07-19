package com.example.sistemkasir.ui.kasir

import android.content.Intent
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

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarQris)
        val tvTotalBayarQris = findViewById<TextView>(R.id.tvTotalBayarQris)
        val btnVerifikasi = findViewById<MaterialButton>(R.id.btnVerifikasiQris)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Tangkap data dari Dashboard/Keranjang
        val listNama = intent.getStringArrayListExtra("LIST_NAMA") ?: arrayListOf()
        val listHarga = intent.getDoubleArrayExtra("LIST_HARGA") ?: doubleArrayOf()
        val listQty = intent.getIntegerArrayListExtra("LIST_QTY") ?: arrayListOf()
        val totalAkhir = intent.getDoubleExtra("TOTAL_TAGIHAN", 0.0)

        // Tampilkan di halaman QRIS (Format Rp)
        val formatRupiah = NumberFormat.getNumberInstance(Locale("in", "ID"))
        tvTotalBayarQris.text = "Rp " + formatRupiah.format(totalAkhir).split(",")[0]

        btnVerifikasi.setOnClickListener {
            Toast.makeText(this, "Pembayaran Berhasil!", Toast.LENGTH_SHORT).show()

            val intentStruk = Intent(this, CetakStrukActivity::class.java)
            // Oper seluruh data ke kelas Struk
            intentStruk.putExtra("TOTAL_TAGIHAN", totalAkhir)
            intentStruk.putStringArrayListExtra("LIST_NAMA", listNama)
            intentStruk.putExtra("LIST_HARGA", listHarga)
            intentStruk.putIntegerArrayListExtra("LIST_QTY", listQty)

            startActivity(intentStruk)
            finish()
        }
    }
}