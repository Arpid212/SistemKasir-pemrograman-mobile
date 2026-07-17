package com.example.sistemkasir.ui.kasir

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog // Tambahan untuk memunculkan kotak pilihan
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

    // 1. Variabel memori untuk mengingat pilihan kasir
    private var metodeTerpilih = "Tunai"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarCheckout)
        val rvKeranjang = findViewById<RecyclerView>(R.id.rvKeranjangCheckout)
        val btnLanjut = findViewById<Button>(R.id.btnLanjutCheckout)
        val layoutPaymentMethod = findViewById<LinearLayout>(R.id.layoutPaymentMethod)

        // 2. Mengambil teks "Tunai" di ujung kanan baris pembayaran
        val tvMetodePembayaran = findViewById<TextView>(R.id.tvMetodePembayaran)

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

        // 3. Logika saat baris PEMBAYARAN diklik (Memunculkan Kotak Pilihan)
        layoutPaymentMethod.setOnClickListener {
            val daftarPilihan = arrayOf("Tunai", "QRIS")

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pilih Metode Pembayaran")
            builder.setItems(daftarPilihan) { _, urutanPilihan ->
                // Mengubah teks di layar sesuai yang diklik
                metodeTerpilih = daftarPilihan[urutanPilihan]
                tvMetodePembayaran.text = metodeTerpilih
            }
            builder.show()
        }

        // 4. Logika tombol LANJUT (Akan mengecek kasir pilih apa)
        btnLanjut.setOnClickListener {
            if (metodeTerpilih == "Tunai") {
                val intent = Intent(this, CheckoutTunaiActivity::class.java)
                intent.putExtra("TOTAL_TAGIHAN", totalAkhirBelanja)
                startActivity(intent)
            } else {
                val intent = Intent(this, CheckoutQrisActivity::class.java)
                intent.putExtra("TOTAL_TAGIHAN", totalAkhirBelanja)
                startActivity(intent)
            }
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