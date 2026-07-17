package com.example.sistemkasir.ui.kasir

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sistemkasir.R
import com.example.sistemkasir.adapter.KatalogProdukAdapter
import com.example.sistemkasir.model.ItemKeranjang
import com.example.sistemkasir.model.Produk
import com.google.firebase.firestore.FirebaseFirestore // 1. Tambahan Import Firebase
import java.text.NumberFormat
import java.util.Locale

class DashboardKasirActivity : AppCompatActivity() {

    private val listKeranjang = arrayListOf<ItemKeranjang>()
    private var subtotalBelanja = 0.0

    private lateinit var adapter: KatalogProdukAdapter
    private lateinit var btnCheckout: Button
    private lateinit var tvTotalItem: TextView

    // 2. Deklarasi Pemanggil Database
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_kasir)

        val rvKatalog = findViewById<RecyclerView>(R.id.rvKatalogProduk)
        btnCheckout = findViewById(R.id.btnCheckout)
        tvTotalItem = findViewById(R.id.tvTotalItemDiKeranjang)

        adapter = KatalogProdukAdapter(emptyList()) { produkTerpilih ->
            tambahKeKeranjang(produkTerpilih)
        }
        rvKatalog.adapter = adapter

        // 3. Panggil fungsi untuk memantau data dari Admin
        pantauDataDariAdmin()

        btnCheckout.setOnClickListener {
            val listNama = ArrayList<String>()
            val listHarga = ArrayList<Double>()
            val listQty = ArrayList<Int>()

            for (item in listKeranjang) {
                listNama.add(item.produk.nama)
                listHarga.add(item.produk.harga)
                listQty.add(item.kuantitas)
            }

            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putStringArrayListExtra("LIST_NAMA", listNama)
            intent.putExtra("LIST_HARGA", listHarga.toDoubleArray())
            intent.putIntegerArrayListExtra("LIST_QTY", listQty)

            startActivity(intent)
        }
    }

    // 4. Fungsi baru untuk menyedot data dari Firebase secara Real-time
    private fun pantauDataDariAdmin() {
        db.collection("produk").addSnapshotListener { snapshots, error ->
            if (error != null) return@addSnapshotListener

            val listProdukDariDatabase = arrayListOf<Produk>()

            for (dokumen in snapshots!!) {
                val id = dokumen.getLong("id")?.toInt() ?: 0
                val nama = dokumen.getString("nama") ?: ""
                val harga = dokumen.getDouble("harga") ?: 0.0
                val deskripsi = dokumen.getString("deskripsi") ?: ""
                val stok = dokumen.getLong("stok")?.toInt() ?: 0
                val foto = dokumen.getString("foto") ?: ""
                val kategori = dokumen.getString("kategori") ?: ""

                val produk = Produk(id, nama, harga, deskripsi, stok, foto, kategori)
                listProdukDariDatabase.add(produk)
            }

            // Kirim data yang didapat ke Adapter agar tampil di layar
            adapter.updateData(listProdukDariDatabase)
        }
    }

    private fun tambahKeKeranjang(produk: Produk) {
        val indexProduk = listKeranjang.indexOfFirst { it.produk.id == produk.id }

        if (indexProduk != -1) {
            listKeranjang[indexProduk].kuantitas += 1
        } else {
            listKeranjang.add(ItemKeranjang(produk, 1))
        }
        hitungSubtotal()
    }

    private fun hitungSubtotal() {
        var total = 0.0
        var totalItem = 0
        for (item in listKeranjang) {
            total += (item.produk.harga * item.kuantitas)
            totalItem += item.kuantitas
        }
        subtotalBelanja = total
        perbaruiUI(totalItem)
    }

    private fun perbaruiUI(totalItem: Int) {
        if (subtotalBelanja > 0) {
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            val hargaStr = formatRupiah.format(subtotalBelanja).replace("Rp", "Rp ")

            btnCheckout.text = "Checkout ($hargaStr)"
            btnCheckout.isEnabled = true
            tvTotalItem.text = totalItem.toString()
        } else {
            btnCheckout.text = "Checkout"
            btnCheckout.isEnabled = false
            tvTotalItem.text = "0"
        }
    }
}