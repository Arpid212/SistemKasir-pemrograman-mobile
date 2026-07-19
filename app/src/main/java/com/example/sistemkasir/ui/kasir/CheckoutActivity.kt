package com.example.sistemkasir.ui.kasir

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sistemkasir.R
import com.example.sistemkasir.adapter.KeranjangAdapter
import com.example.sistemkasir.model.ItemKeranjang
import com.example.sistemkasir.model.Produk
import com.google.android.material.appbar.MaterialToolbar
import java.text.NumberFormat
import java.util.Locale

// IMPORT EKSPLISIT AGAR TIDAK UNRESOLVED REFERENCE
import com.example.sistemkasir.ui.kasir.CheckoutQrisActivity
import com.example.sistemkasir.ui.kasir.CheckoutTunaiActivity

class CheckoutActivity : AppCompatActivity() {

    private lateinit var adapter: KeranjangAdapter
    private val PAJAK_PERSEN = 0.10
    private var totalAkhirBelanja = 0.0

    // Variabel untuk melacak metode pembayaran aktif (Bawaan awal: Tunai)
    private var metodeTerpilih = "Tunai"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarCheckout)
        val rvKeranjang = findViewById<RecyclerView>(R.id.rvKeranjangCheckout)
        val btnLanjut = findViewById<Button>(R.id.btnLanjutCheckout)

        // Menghubungkan ke TextView Pembayaran di pojok kanan atas XML kamu
        val tvMetode = findViewById<TextView>(R.id.tvMetodePembayaran)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // 1. ✨ SEKARANG MENANGKAP DATA LENGKAP DARI INTENT
        val listId = intent.getIntegerArrayListExtra("LIST_ID") ?: arrayListOf()
        val listNama = intent.getStringArrayListExtra("LIST_NAMA") ?: arrayListOf()
        val listHarga = intent.getDoubleArrayExtra("LIST_HARGA") ?: doubleArrayOf()
        val listQty = intent.getIntegerArrayListExtra("LIST_QTY") ?: arrayListOf()
        val listFoto = intent.getStringArrayListExtra("LIST_FOTO") ?: arrayListOf()
        val listKategori = intent.getStringArrayListExtra("LIST_KATEGORI") ?: arrayListOf()

        // 2. ✨ RAKIT KEMBALI DENGAN DATA PARAMETER YANG LENGKAP
        val listPesanan = arrayListOf<ItemKeranjang>()
        for (i in listNama.indices) {
            val produk = Produk(
                id = if (i < listId.size) listId[i] else i,
                nama = listNama[i],
                harga = listHarga[i],
                deskripsi = "",
                stok = 0,
                foto = if (i < listFoto.size) listFoto[i] else "",
                kategori = if (i < listKategori.size) listKategori[i] else ""
            )
            listPesanan.add(ItemKeranjang(produk, listQty[i]))
        }

        // 3. Masukkan ke RecyclerView Adapter
        adapter = KeranjangAdapter(listPesanan)
        rvKeranjang.layoutManager = LinearLayoutManager(this)
        rvKeranjang.adapter = adapter

        hitungRincianBiaya(listPesanan)

        // --- LOGIKA MENAMPILKAN POPUP PILIHAN MENU (TUNAI / QRIS) ---
        tvMetode.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("Tunai")
            popup.menu.add("QRIS")

            popup.setOnMenuItemClickListener { menuItem ->
                metodeTerpilih = menuItem.title.toString()
                tvMetode.text = metodeTerpilih // Mengubah teks di UI (kanan atas) sesuai pilihan
                true
            }
            popup.show()
        }

        // --- LOGIKA TOMBOL LANJUT DINAMIS ---
        btnLanjut.setOnClickListener {
            // Tentukan target halaman berdasarkan metode yang dipilih oleh kasir
            val intentTujuan = if (metodeTerpilih == "QRIS") {
                Intent(this, CheckoutQrisActivity::class.java)
            } else {
                Intent(this, CheckoutTunaiActivity::class.java)
            }

            // Kirimkan semua data rincian belanja ke halaman berikutnya
            intentTujuan.putExtra("TOTAL_TAGIHAN", totalAkhirBelanja)
            intentTujuan.putStringArrayListExtra("LIST_NAMA", listNama)
            intentTujuan.putExtra("LIST_HARGA", listHarga)
            intentTujuan.putIntegerArrayListExtra("LIST_QTY", listQty)

            startActivity(intentTujuan)
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

        // Menghitung total jumlah kuantitas barang, bukan jumlah jenis itemnya saja
        var totalKuantitasSemua = 0
        for (item in keranjang) {
            totalKuantitasSemua += item.kuantitas
        }

        findViewById<TextView>(R.id.tvLabelSubtotal).text = "Subtotal ($totalKuantitasSemua)"
        findViewById<TextView>(R.id.tvNilaiSubtotal).text = formatRupiah.format(subtotal).replace("Rp", "Rp ")
        findViewById<TextView>(R.id.tvNilaiPajak).text = formatRupiah.format(pajak).replace("Rp", "Rp ")
        findViewById<TextView>(R.id.tvNilaiTotal).text = formatRupiah.format(totalAkhirBelanja).replace("Rp", "Rp ")
    }
}