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

class CetakStrukActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cetak_struk)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarStruk)
        val btnCetak = findViewById<MaterialButton>(R.id.btnCetakPrinter)

        val tvNamaItem = findViewById<TextView>(R.id.tvNamaItemStruk)
        val tvQtyItem = findViewById<TextView>(R.id.tvQtyItemStruk)
        val tvHargaItem = findViewById<TextView>(R.id.tvHargaItemStruk)
        val tvTotalStruk = findViewById<TextView>(R.id.tvTotalStruk)
        val tvTunai = findViewById<TextView>(R.id.tvTunaiStruk)
        val tvKembali = findViewById<TextView>(R.id.tvKembaliStruk)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val listNama = intent.getStringArrayListExtra("LIST_NAMA") ?: arrayListOf()
        val listHarga = intent.getDoubleArrayExtra("LIST_HARGA") ?: doubleArrayOf()
        val listQty = intent.getIntegerArrayListExtra("LIST_QTY") ?: arrayListOf()
        val totalAkhir = intent.getDoubleExtra("TOTAL_TAGIHAN", 0.0)

        // Tangkap data tambahan dari CheckoutTunaiActivity
        val metodePembayaran = intent.getStringExtra("METODE_PEMBAYARAN") ?: "QRIS"
        val nominalBayar = intent.getDoubleExtra("NOMINAL_BAYAR", totalAkhir)
        val kembalian = intent.getDoubleExtra("KEMBALIAN", 0.0)

        val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))

        // 1. Tampilkan barang
        if (listNama.isNotEmpty()) {
            tvNamaItem.text = listNama[0]
            tvQtyItem.text = "x${listQty[0]}"
            tvHargaItem.text = formatter.format(listHarga[0].toInt())
        }

        // 2. Set Total Belanja
        tvTotalStruk.text = formatter.format(totalAkhir.toInt())

        // 3. Cek logika metode pembayaran (Tunai vs QRIS)
        if (metodePembayaran == "Tunai") {
            tvTunai.text = formatter.format(nominalBayar.toInt())
            tvKembali.text = formatter.format(kembalian.toInt())
        } else {
            // Jika QRIS, nominal bayar = total belanjaan dan kembalian 0
            tvTunai.text = formatter.format(totalAkhir.toInt())
            tvKembali.text = "0"
        }

        btnCetak.setOnClickListener {
            Toast.makeText(this, "Mencetak Struk ke Printer...", Toast.LENGTH_SHORT).show()
        }
    }
}