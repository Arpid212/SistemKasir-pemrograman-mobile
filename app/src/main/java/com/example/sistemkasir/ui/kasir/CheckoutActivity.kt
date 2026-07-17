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
import com.example.sistemkasir.model.Produk
import com.google.android.material.appbar.MaterialToolbar
import java.text.NumberFormat
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var adapter: KeranjangAdapter
    private val PAJAK_PERSEN = 0.10
    private var totalAkhirBelanja = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarCheckout)
        val rvKeranjang = findViewById<RecyclerView>(R.id.rvKeranjangCheckout)
        val btnLanjut = findViewById<Button>(R.id.btnLanjutCheckout)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // 1. Tangkap array dasar dari Intent
        val listNama = intent.getStringArrayListExtra("LIST_NAMA") ?: arrayListOf()
        val listHarga = intent.getDoubleArrayExtra("LIST_HARGA") ?: doubleArrayOf()
        val listQty = intent.getIntegerArrayListExtra("LIST_QTY") ?: arrayListOf()

        // 2. Rakit kembali menjadi list pesanan
        val listPesanan = arrayListOf<ItemKeranjang>()
        for (i in listNama.indices) {
            // Buat objek produk buatan dari data yang diterima
            val produk = Produk(nama = listNama[i], harga = listHarga[i])
            listPesanan.add(ItemKeranjang(produk, listQty[i]))
        }

        // 3. Masukkan ke adapter
        adapter = KeranjangAdapter(listPesanan)
        rvKeranjang.layoutManager = LinearLayoutManager(this)
        rvKeranjang.adapter = adapter

        hitungRincianBiaya(listPesanan)

        btnLanjut.setOnClickListener {
            // Melempar HANYA total akhir (tipe data Double biasa) ke halaman Tunai
            val intentTunai = Intent(this, CheckoutTunaiActivity::class.java)
            intentTunai.putExtra("TOTAL_TAGIHAN", totalAkhirBelanja)
            startActivity(intentTunai)
        }
    }

    private fun hitungRincianBiaya(keranjang: List<ItemKeranjang>) {
        var subtotal = 0.0
        for (item in keranjang) {
            subtotal += (item.produk.harga * item.kuantitas)
        }

        val pajak = subtotal * PAJAK_PERSEN
        totalAkhirBelanja = subtotal + pajak

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        findViewById<TextView>(R.id.tvLabelSubtotal).text = "Subtotal (${keranjang.size})"
        findViewById<TextView>(R.id.tvNilaiSubtotal).text = formatRupiah.format(subtotal).replace("Rp", "Rp ")
        findViewById<TextView>(R.id.tvNilaiPajak).text = formatRupiah.format(pajak).replace("Rp", "Rp ")
        findViewById<TextView>(R.id.tvNilaiTotal).text = formatRupiah.format(totalAkhirBelanja).replace("Rp", "Rp ")
    }
}