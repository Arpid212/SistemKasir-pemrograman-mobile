package com.example.sistemkasir.ui.kasir

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sistemkasir.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class CheckoutQrisActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout_qris)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarQris)
        val tvTotalBayarQris = findViewById<TextView>(R.id.tvTotalBayarQris)
        val btnVerifikasi = findViewById<MaterialButton>(R.id.btnVerifikasiQris)

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val listNama = intent.getStringArrayListExtra("LIST_NAMA") ?: arrayListOf()
        val listHarga = intent.getDoubleArrayExtra("LIST_HARGA") ?: doubleArrayOf()
        val listQty = intent.getIntegerArrayListExtra("LIST_QTY") ?: arrayListOf()
        val listFoto = intent.getStringArrayListExtra("LIST_FOTO") ?: arrayListOf()
        val totalAkhir = intent.getDoubleExtra("TOTAL_TAGIHAN", 0.0)

        val formatRupiah = NumberFormat.getNumberInstance(Locale("in", "ID"))
        tvTotalBayarQris.text = "Rp " + formatRupiah.format(totalAkhir).split(",")[0]

        btnVerifikasi.setOnClickListener {
            simpanTransaksiQris(totalAkhir, listNama, listHarga, listQty, listFoto)
        }
    }

    private fun simpanTransaksiQris(
        totalTagihan: Double,
        listNama: ArrayList<String>,
        listHarga: DoubleArray,
        listQty: ArrayList<Int>,
        listFoto: ArrayList<String>
    ) {
        val rincianPesanan = arrayListOf<Map<String, Any>>()
        for (i in listNama.indices) {
            val item = hashMapOf<String, Any>(
                "nama" to listNama[i],
                "harga" to listHarga[i],
                "kuantitas" to listQty[i],
                "foto" to if (i < listFoto.size) listFoto[i] else ""
            )
            rincianPesanan.add(item)
        }

        val sharedPref = getSharedPreferences("SesiSistemKasir", Context.MODE_PRIVATE)
        val namaKasirAktif = sharedPref.getString("NAMA_USER", "Kasir Tidak Dikenal")

        val dataTransaksi = hashMapOf(
            "waktu_transaksi" to com.google.firebase.Timestamp.now(),
            "metode_pembayaran" to "QRIS",
            "total_tagihan" to totalTagihan,
            "nominal_bayar" to totalTagihan,
            "kembalian" to 0.0,
            "rincian" to rincianPesanan,
            "nama_kasir" to namaKasirAktif
        )

        db.collection("Transaksi").add(dataTransaksi)
            .addOnSuccessListener {
                for (i in listNama.indices) {
                    db.collection("Produk").whereEqualTo("nama", listNama[i]).get()
                        .addOnSuccessListener { hasil ->
                            for (dokumen in hasil) {
                                val stokBaru = (dokumen.getLong("stok") ?: 0) - listQty[i]
                                dokumen.reference.update("stok", stokBaru)
                            }
                        }
                }

                Toast.makeText(this, "Transaksi QRIS Berhasil Disimpan!", Toast.LENGTH_SHORT).show()

                val intentStruk = Intent(this, CetakStrukActivity::class.java).apply {
                    putExtra("TOTAL_TAGIHAN", totalTagihan)
                    putExtra("NOMINAL_BAYAR", totalTagihan)
                    putExtra("KEMBALIAN", 0.0)
                    putExtra("METODE_PEMBAYARAN", "QRIS")
                    putStringArrayListExtra("LIST_NAMA", listNama)
                    putExtra("LIST_HARGA", listHarga)
                    putIntegerArrayListExtra("LIST_QTY", listQty)
                }
                startActivity(intentStruk)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan transaksi QRIS!", Toast.LENGTH_SHORT).show()
            }
    }
}