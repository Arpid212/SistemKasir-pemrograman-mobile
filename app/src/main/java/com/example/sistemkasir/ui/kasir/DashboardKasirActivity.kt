package com.example.sistemkasir.ui.kasir

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.TextView
import android.widget.Toast
import android.content.Context
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
import android.app.AlertDialog
import com.example.sistemkasir.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class DashboardKasirActivity : AppCompatActivity() {

    private val listKeranjang = arrayListOf<ItemKeranjang>()
    private var subtotalBelanja = 0.0

    private var masterListProduk = arrayListOf<Produk>()
    private var itemAktif: ItemKeranjang? = null

    private lateinit var adapter: KatalogProdukAdapter
    private lateinit var rvKatalog: RecyclerView
    private lateinit var btnCheckout: Button
    private lateinit var tvTotalItem: TextView
    private lateinit var btnMinusGlobal: TextView
    private lateinit var btnPlusGlobal: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_kasir)

        rvKatalog = findViewById<RecyclerView>(R.id.rvKatalogProduk)
        btnCheckout = findViewById(R.id.btnCheckout)
        tvTotalItem = findViewById(R.id.tvTotalItemDiKeranjang)
        btnMinusGlobal = findViewById(R.id.btnMinusGlobal)
        btnPlusGlobal = findViewById(R.id.btnPlusGlobal)

        val chipGroup = findViewById<HorizontalScrollView>(R.id.scrollCategories).getChildAt(0) as ChipGroup

        adapter = KatalogProdukAdapter(emptyList()) { produkTerpilih ->
            tambahKeKeranjang(produkTerpilih)
        }
        rvKatalog.adapter = adapter

        pantauDataDariAdmin()

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
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


        btnPlusGlobal.setOnClickListener {
            if (itemAktif == null && listKeranjang.isNotEmpty()) {
                itemAktif = listKeranjang.last()
            }
            itemAktif?.let {
                it.kuantitas += 1
                hitungSubtotal()
            } ?: Toast.makeText(this, "Silakan pilih produk dulu!", Toast.LENGTH_SHORT).show()
        }

        btnMinusGlobal.setOnClickListener {
            if (itemAktif == null && listKeranjang.isNotEmpty()) {
                itemAktif = listKeranjang.last()
            }
            itemAktif?.let { currentItem ->
                if (currentItem.kuantitas > 1) {
                    currentItem.kuantitas -= 1
                } else {
                    listKeranjang.remove(currentItem)
                    itemAktif = if (listKeranjang.isNotEmpty()) listKeranjang.last() else null
                }
                hitungSubtotal()
            } ?: Toast.makeText(this, "Keranjang kosong!", Toast.LENGTH_SHORT).show()
        }

        val macAddressPrinter = "00:11:22:33:44:55"
        Thread {
            val terhubung = com.example.sistemkasir.utils.PrinterUtil.connectBluetooth(macAddressPrinter)
            runOnUiThread {
                if (terhubung) {
                    Toast.makeText(this, "Printer Terhubung", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal menghubungkan Printer", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()

        btnCheckout.setOnClickListener {
            if (listKeranjang.isEmpty()) {
                Toast.makeText(this, "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val listId = ArrayList<Int>()
            val listNama = ArrayList<String>()
            val listHarga = ArrayList<Double>()
            val listQty = ArrayList<Int>()
            val listFoto = ArrayList<String>()
            val listKategori = ArrayList<String>()

            for (item in listKeranjang) {
                listId.add(item.produk.id)
                listNama.add(item.produk.nama)
                listHarga.add(item.produk.harga)
                listQty.add(item.kuantitas)
                listFoto.add(item.produk.foto)
                listKategori.add(item.produk.kategori)
            }

            val intent = Intent(this, CheckoutActivity::class.java).apply {
                putIntegerArrayListExtra("LIST_ID", listId)
                putStringArrayListExtra("LIST_NAMA", listNama)
                putExtra("LIST_HARGA", listHarga.toDoubleArray())
                putIntegerArrayListExtra("LIST_QTY", listQty)
                putStringArrayListExtra("LIST_FOTO", listFoto)
                putStringArrayListExtra("LIST_KATEGORI", listKategori)
                putExtra("TOTAL_TAGIHAN", subtotalBelanja)
            }
            startActivity(intent)
        }
        val btnKeluar = findViewById<Button>(R.id.btnKeluar)

        btnKeluar.setOnClickListener {
            tampilkanDialogKeluar()
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
        val indexProduk = listKeranjang.indexOfFirst { it.produk.nama.equals(produk.nama, ignoreCase = true) }

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

    private fun tampilkanDialogKeluar() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin mengakhiri sesi kasir ini?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                prosesLogout()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun prosesLogout() {
        FirebaseAuth.getInstance().signOut()
        val sharedPref = getSharedPreferences("SesiSistemKasir", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    private fun perbaruiUI() {
        var totalKuantitasSemua = 0
        for (item in listKeranjang) {
            totalKuantitasSemua += item.kuantitas
        }
        tvTotalItem.text = totalKuantitasSemua.toString()

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