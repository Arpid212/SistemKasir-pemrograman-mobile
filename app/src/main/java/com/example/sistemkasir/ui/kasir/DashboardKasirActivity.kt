package com.example.sistemkasir.ui.kasir

import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sistemkasir.R
import com.example.sistemkasir.adapter.KatalogProdukAdapter
import com.example.sistemkasir.viewmodel.KasirViewModel
import java.text.NumberFormat
import java.util.Locale

class DashboardKasirActivity : AppCompatActivity() {

    // Inisialisasi ViewModel
    private val viewModel: KasirViewModel by viewModels()
    private lateinit var adapter: KatalogProdukAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_kasir)

        val rvKatalog = findViewById<RecyclerView>(R.id.rvKatalogProduk)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)

        // Setup Adapter
        adapter = KatalogProdukAdapter(emptyList()) { produkTerpilih ->
            viewModel.tambahKeKeranjang(produkTerpilih)
        }
        rvKatalog.adapter = adapter

        // Memantau perubahan nilai subtotal dari ViewModel secara real-time
        viewModel.subtotal.observe(this) { totalHarga ->
            if (totalHarga > 0) {
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                val hargaStr = formatRupiah.format(totalHarga).replace("Rp", "Rp ")

                btnCheckout.text = "Checkout ($hargaStr)"
                btnCheckout.isEnabled = true
            } else {
                btnCheckout.text = "Checkout"
                btnCheckout.isEnabled = false
            }
        }
    }
}