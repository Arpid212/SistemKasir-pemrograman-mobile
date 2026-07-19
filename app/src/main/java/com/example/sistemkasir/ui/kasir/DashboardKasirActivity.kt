package com.example.sistemkasir.ui.kasir

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sistemkasir.R
import com.example.sistemkasir.adapter.KatalogProdukAdapter
import com.example.sistemkasir.model.ItemKeranjang
import com.example.sistemkasir.model.Produk
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class DashboardKasirActivity : AppCompatActivity() {

    private val listKeranjang = arrayListOf<ItemKeranjang>()
    private var subtotalBelanja = 0.0

    // Variabel untuk menampung semua data produk asli & item aktif untuk tombol +/-
    private var masterListProduk = arrayListOf<Produk>()
    private var itemAktif: ItemKeranjang? = null

    private lateinit var adapter: KatalogProdukAdapter
    private lateinit var btnCheckout: Button
    private lateinit var tvTotalItem: TextView
    private lateinit var btnMinusGlobal: TextView
    private lateinit var btnPlusGlobal: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_kasir)

        // Inisialisasi View berdasarkan ID di XML kamu
        val rvKatalog = findViewById<RecyclerView>(R.id.rvKatalogProduk)
        btnCheckout = findViewById(R.id.btnCheckout)
        tvTotalItem = findViewById(R.id.tvTotalItemDiKeranjang)
        btnMinusGlobal = findViewById(R.id.btnMinusGlobal)
        btnPlusGlobal = findViewById(R.id.btnPlusGlobal)

        // Mengambil ChipGroup di dalam HorizontalScrollView karena ChipGroup tidak diberi ID di XML
        val chipGroup = findViewById<HorizontalScrollView>(R.id.scrollCategories).getChildAt(0) as ChipGroup

        adapter = KatalogProdukAdapter(emptyList()) { produkTerpilih ->
            tambahKeKeranjang(produkTerpilih)
        }
        rvKatalog.adapter = adapter

        pantauDataDariAdmin()

        // --- FILTER KATEGORI ---
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedChipId = checkedIds.firstOrNull()
            if (selectedChipId == null) {
                adapter.updateData(masterListProduk)
                return@setOnCheckedStateChangeListener
            }

            val chip = findViewById<Chip>(selectedChipId)
            val kategoriDipilih = chip?.text.toString()

            if (kategoriDipilih.equals("All", ignoreCase = true)) {
                adapter.updateData(masterListProduk)
            } else {
                val filteredList = masterListProduk.filter { it.kategori.equals(kategoriDipilih, ignoreCase = true) }
                adapter.updateData(filteredList)
            }
        }

        // --- LOGIKA TOMBOL + GLOBAL ---
        btnPlusGlobal.setOnClickListener {
            itemAktif?.let {
                it.kuantitas += 1
                hitungSubtotal()
            }
        }

        // --- LOGIKA TOMBOL - GLOBAL ---
        btnMinusGlobal.setOnClickListener {
            itemAktif?.let {
                if (it.kuantitas > 1) {
                    it.kuantitas -= 1
                } else {
                    listKeranjang.remove(it)
                    // Jika item habis, alihkan fokus aktif ke item terakhir di keranjang
                    itemAktif = if (listKeranjang.isNotEmpty()) listKeranjang.last() else null
                }
                hitungSubtotal()
            }
        }

        // Bluetooth Printer Thread
        val macAddressPrinter = "00:11:22:33:44:55"
        Thread {
            val terhubung = com.example.sistemkasir.utils.PrinterUtil.connectBluetooth(macAddressPrinter)
            runOnUiThread {
                if (terhubung) {
                    android.widget.Toast.makeText(this, "Printer Terhubung", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(this, "Gagal menghubungkan Printer", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }.start()

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

    private fun pantauDataDariAdmin() {
        db.collection("Produk").addSnapshotListener { snapshots, error ->
            if (error != null) return@addSnapshotListener

            masterListProduk.clear()

            for (dokumen in snapshots!!) {
                val id = dokumen.getLong("id")?.toInt() ?: 0
                val nama = dokumen.getString("nama") ?: ""
                val harga = dokumen.getDouble("harga") ?: 0.0
                val deskripsi = dokumen.getString("deskripsi") ?: ""
                val stok = dokumen.getLong("stok")?.toInt() ?: 0
                val foto = dokumen.getString("foto") ?: ""
                val kategori = dokumen.getString("kategori") ?: ""

                val produk = Produk(id, nama, harga, deskripsi, stok, foto, kategori)
                masterListProduk.add(produk)
            }

            adapter.updateData(masterListProduk)
        }
    }

    private fun tambahKeKeranjang(produk: Produk) {
        val indexProduk = listKeranjang.indexOfFirst { it.produk.id == produk.id }

        if (indexProduk != -1) {
            listKeranjang[indexProduk].kuantitas += 1
            itemAktif = listKeranjang[indexProduk]
        } else {
            val newItem = ItemKeranjang(produk, 1)
            listKeranjang.add(newItem)
            itemAktif = newItem
        }
        hitungSubtotal()
    }

    private fun hitungSubtotal() {
        var total = 0.0
        for (item in listKeranjang) {
            total += (item.produk.harga * item.kuantitas)
        }
        subtotalBelanja = total
        perbaruiUI()
    }

    private fun perbaruiUI() {
        // Update angka kuantitas aktif di dalam lingkaran putih (tvTotalItemDiKeranjang)
        tvTotalItem.text = (itemAktif?.kuantitas ?: 0).toString()

        if (subtotalBelanja > 0) {
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            val hargaStr = formatRupiah.format(subtotalBelanja).replace("Rp", "Rp ")

            btnCheckout.text = "Checkout ($hargaStr)"
            btnCheckout.isEnabled = true
        } else {
            btnCheckout.text = "Checkout"
            btnCheckout.isEnabled = false
        }
    }
}