package com.example.sistemkasir.ui.kasir

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout // Tambahan import
import android.widget.TextView
import android.widget.Toast
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

        // KODE BARU: Menggunakan Layout Pembayaran (bukan tombol QRIS yang error)
        val layoutPaymentMethod = findViewById<LinearLayout>(R.id.layoutPaymentMethod)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val listNama = intent.getStringArrayListExtra("LIST_NAMA") ?: arrayListOf()
        val listHarga = intent.getDoubleArrayExtra("LIST_HARGA") ?: doubleArrayOf()
        val listQty = intent.getIntegerArrayListExtra("LIST_QTY") ?: arrayListOf()

        val listPesanan = arrayListOf<ItemKeranjang>()
        for (i in listNama.indices) {
            val produk = Produk(nama = listNama[i], harga = listHarga[i])
            listPesanan.add(ItemKeranjang(produk, listQty[i]))
        }

        adapter = KeranjangAdapter(listPesanan)
        rvKeranjang.layoutManager = LinearLayoutManager(this)
        rvKeranjang.adapter = adapter

        hitungRincianBiaya(listPesanan)

        // Klik tombol Lanjut (Otomatis ke halaman Tunai)
        btnLanjut.setOnClickListener {
            val intentTunai = Intent(this, CheckoutTunaiActivity::class.java)
            intentTunai.putExtra("TOTAL_TAGIHAN", totalAkhirBelanja)
            startActivity(intentTunai)
        }

        // KODE BARU: Klik baris Metode Pembayaran
        layoutPaymentMethod.setOnClickListener {
            // Karena belum ada halaman QRIS, kita munculkan pop-up ini saja dulu
            Toast.makeText(this, "Pilihan pembayaran QRIS akan segera dikembangkan", Toast.LENGTH_SHORT).show()

            // Nanti jika ingin dibuat halamannya, Anda bisa menambahkan pop-up dialog di sini
            // untuk memilih "Tunai" atau "QRIS".
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