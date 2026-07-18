package com.example.sistemkasir.ui.kasir

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.Context
import com.example.sistemkasir.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class CheckoutTunaiActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout_tunai)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarTunai)
        val etNominalBayar = findViewById<EditText>(R.id.etNominalBayar)
        val tvTotalBayar = findViewById<TextView>(R.id.tvTotalBayar)
        val tvKembalian = findViewById<TextView>(R.id.tvKembalian)
        val btnCetakStruk = findViewById<Button>(R.id.btnCetakStruk)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Tangkap tipe data dasar (Double)
        val totalTagihan = intent.getDoubleExtra("TOTAL_TAGIHAN", 0.0)

        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        tvTotalBayar.text = formatRupiah.format(totalTagihan).replace("Rp", "Rp ")

        etNominalBayar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val inputStr = s.toString()
                if (inputStr.isNotEmpty()) {
                    val bayar = inputStr.toDoubleOrNull() ?: 0.0
                    val kembalian = bayar - totalTagihan

                    if (kembalian >= 0) {
                        tvKembalian.text = formatRupiah.format(kembalian).replace("Rp", "Rp ")
                        btnCetakStruk.isEnabled = true
                    } else {
                        tvKembalian.text = "Uang Kurang"
                        btnCetakStruk.isEnabled = false
                    }
                } else {
                    tvKembalian.text = "Rp 0"
                    btnCetakStruk.isEnabled = false
                }
            }
        })
        btnCetakStruk.setOnClickListener {
            val bayar = etNominalBayar.text.toString().toDoubleOrNull() ?: 0.0
            simpanTransaksiKeFirestore(totalTagihan, bayar)
        }
    }
    private fun simpanTransaksiKeFirestore(totalTagihan: Double, bayar: Double) {
        val listNama = intent.getStringArrayListExtra("LIST_NAMA") ?: arrayListOf()
        val listHarga = intent.getDoubleArrayExtra("LIST_HARGA") ?: doubleArrayOf()
        val listQty = intent.getIntegerArrayListExtra("LIST_QTY") ?: arrayListOf()

        val rincianPesanan = arrayListOf<Map<String, Any>>()
        for (i in listNama.indices) {
            rincianPesanan.add(
                mapOf(
                    "nama" to listNama[i],
                    "harga" to listHarga[i],
                    "kuantitas" to listQty[i]
                )
            )
        }

        // Ambil nama kasir dari SharedPreferences
        val sharedPref = getSharedPreferences("SesiSistemKasir", Context.MODE_PRIVATE)
        val namaKasirAktif = sharedPref.getString("NAMA_USER", "Kasir Tidak Dikenal")

        val dataTransaksi = hashMapOf(
            "waktu_transaksi" to com.google.firebase.Timestamp.now(),
            "metode_pembayaran" to "Tunai",
            "total_tagihan" to totalTagihan,
            "nominal_bayar" to bayar,
            "kembalian" to (bayar - totalTagihan),
            "rincian" to rincianPesanan,
            "nama_kasir" to namaKasirAktif // Menggunakan variabel dinamis dari session
        )
        db.collection("Transaksi")
            .add(dataTransaksi)
            .addOnSuccessListener {

                // PERBAIKAN: Format teks struk dan kirim ke Printer Bluetooth
                val struk = StringBuilder()
                struk.append("==== NAMA TOKO ANDA ====\n")
                struk.append("Kasir: Kasir Aktif\n")
                struk.append("------------------------\n")

                for (i in listNama.indices) {
                    val namaProduk = listNama[i]
                    val qtyTerbeli = listQty[i]

                    db.collection("Produk")
                        .whereEqualTo("nama", namaProduk)
                        .get()
                        .addOnSuccessListener { hasilPencarian ->
                            for (dokumen in hasilPencarian) {
                                val stokSekarang = dokumen.getLong("stok") ?: 0
                                val stokBaru = stokSekarang - qtyTerbeli

                                // Memperbarui stok langsung ke referensi ID dokumennya
                                dokumen.reference.update("stok", stokBaru)
                            }
                        }
                }

                // Kembali ke Dashboard Kasir dan bersihkan tumpukan halaman (Clear Top)
                val intent = Intent(this, DashboardKasirActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
    }
}