package com.example.sistemkasir.ui.kasir

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sistemkasir.R
import com.example.sistemkasir.adapter.KeranjangAdapter
import com.example.sistemkasir.model.ItemKeranjang
import com.google.android.material.appbar.MaterialToolbar
import java.text.NumberFormat
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var adapter: KeranjangAdapter
    private val PAJAK_PERSEN = 0.10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarCheckout)
        val rvKeranjang = findViewById<RecyclerView>(R.id.rvKeranjangCheckout)
        val tvLabelSubtotal = findViewById<TextView>(R.id.tvLabelSubtotal)
        val tvNilaiSubtotal = findViewById<TextView>(R.id.tvNilaiSubtotal)
        val tvNilaiPajak = findViewById<TextView>(R.id.tvNilaiPajak)
        val tvNilaiTotal = findViewById<TextView>(R.id.tvNilaiTotal)
        val btnLanjut = findViewById<Button>(R.id.btnLanjutCheckout)
        val tvMetodePembayaran = findViewById<TextView>(R.id.tvMetodePembayaran)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val listPesanan = emptyList<ItemKeranjang>() // Dummy data sementara

        adapter = KeranjangAdapter(listPesanan)
        rvKeranjang.layoutManager = LinearLayoutManager(this)
        rvKeranjang.adapter = adapter

        val subtotal = hitungSubtotal(listPesanan)
        val pajak = subtotal * PAJAK_PERSEN
        val totalAkhir = subtotal + pajak

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvLabelSubtotal.text = "Subtotal (${listPesanan.size})"
        tvNilaiSubtotal.text = formatRupiah.format(subtotal).replace("Rp", "Rp ")
        tvNilaiPajak.text = formatRupiah.format(pajak).replace("Rp", "Rp ")
        tvNilaiTotal.text = formatRupiah.format(totalAkhir).replace("Rp", "Rp ")

        btnLanjut.setOnClickListener {
            val metode = tvMetodePembayaran.text.toString()
            if (metode == "Tunai") {
                startActivity(Intent(this, CheckoutTunaiActivity::class.java))
            } else if (metode == "QRIS") {
                // startActivity(Intent(this, CheckoutQrisActivity::class.java))
            }
        }
    }

    private fun hitungSubtotal(keranjang: List<ItemKeranjang>): Double {
        var sub = 0.0
        for (item in keranjang) {
            sub += (item.produk.harga * item.kuantitas)
        }
        return sub
    }
}