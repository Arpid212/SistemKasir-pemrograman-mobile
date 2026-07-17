package com.example.sistemkasir.ui.kasir

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sistemkasir.R
import com.google.android.material.appbar.MaterialToolbar
import java.text.NumberFormat
import java.util.Locale

class CheckoutTunaiActivity : AppCompatActivity() {

    private var totalTagihan: Double = 48000.0 // Simulasi total yang dilempar dari halaman sebelumnya

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout_tunai)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarTunai)
        val etNominalBayar = findViewById<EditText>(R.id.etNominalBayar)
        val tvTotalBayar = findViewById<TextView>(R.id.tvTotalBayar)
        val tvKembalian = findViewById<TextView>(R.id.tvKembalian)
        val btnCetakStruk = findViewById<Button>(R.id.btnCetakStruk)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvTotalBayar.text = formatRupiah.format(totalTagihan).replace("Rp", "Rp ")

        // Logika Hitung Otomatis saat kasir mengetik
        etNominalBayar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val inputStr = s.toString()
                if (inputStr.isNotEmpty()) {
                    val bayar = inputStr.toDoubleOrNull() ?: 0.0
                    val kembalian = bayar - totalTagihan

                    if (kembalian >= 0) {
                        tvKembalian.text = formatRupiah.format(kembalian).replace("Rp", "Rp ")
                        btnCetakStruk.isEnabled = true
                    } else {
                        tvKembalian.text = "Uang Kurang"
                        btnCetakStruk.isEnabled = false
                    }
                } else {
                    tvKembalian.text = "Rp 0"
                    btnCetakStruk.isEnabled = false
                }
            }
        })

        btnCetakStruk.setOnClickListener {
            // TODO: Pindah ke Halaman Cetak Struk
        }
    }
}