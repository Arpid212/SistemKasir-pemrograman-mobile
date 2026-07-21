package com.example.sistemkasir.ui.kasir

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sistemkasir.R
import com.google.android.material.appbar.MaterialToolbar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CetakStrukActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cetak_struk)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarStruk)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val listNama = intent.getStringArrayListExtra("LIST_NAMA") ?: arrayListOf()
        val listHarga = intent.getDoubleArrayExtra("LIST_HARGA") ?: doubleArrayOf()
        val listQty = intent.getIntegerArrayListExtra("LIST_QTY") ?: arrayListOf()
        val totalAkhir = intent.getDoubleExtra("TOTAL_TAGIHAN", 0.0)

        val metodePembayaran = intent.getStringExtra("METODE_PEMBAYARAN") ?: "QRIS"
        val nominalBayar = intent.getDoubleExtra("NOMINAL_BAYAR", totalAkhir)
        val kembalian = intent.getDoubleExtra("KEMBALIAN", 0.0)

        val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))

        val sharedPref = getSharedPreferences("SesiSistemKasir", Context.MODE_PRIVATE)
        val namaKasir = sharedPref.getString("NAMA_USER", "Kasir")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val tanggalSekarang = sdf.format(Date())
        val kodeStruk = System.currentTimeMillis().toString()

        findViewById<TextView>(R.id.tvInfoStruk).text =
            "Kode Struk : $kodeStruk\nTanggal : $tanggalSekarang\nKasir : $namaKasir"

        findViewById<TextView>(R.id.tvTotalStruk).text = formatter.format(totalAkhir.toInt())
        findViewById<TextView>(R.id.tvLabelMetode).text = metodePembayaran

        if (metodePembayaran == "QRIS") {
            findViewById<TextView>(R.id.tvNominalBayarStruk).text = "LUNAS"
            findViewById<TextView>(R.id.tvLabelKembali).text = "-"
            findViewById<TextView>(R.id.tvKembaliStruk).text = "-"
        } else {
            findViewById<TextView>(R.id.tvNominalBayarStruk).text = formatter.format(nominalBayar.toInt())
            findViewById<TextView>(R.id.tvKembaliStruk).text = formatter.format(kembalian.toInt())
        }

        val wadahRincian = findViewById<LinearLayout>(R.id.llRincianPesanan)

        for (i in listNama.indices) {
            val barisItem = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 12) }
            }

            val tvNama = TextView(this).apply {
                text = listNama[i]
                setTextColor(resources.getColor(android.R.color.black, theme))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvQty = TextView(this).apply {
                text = "x${listQty[i]}"
                setTextColor(resources.getColor(android.R.color.black, theme))
                setPadding(0, 0, 32, 0)
            }

            val tvSubtotal = TextView(this).apply {
                val totalHargaPerItem = listHarga[i] * listQty[i]
                text = formatter.format(totalHargaPerItem.toInt())
                setTextColor(resources.getColor(android.R.color.black, theme))
                gravity = Gravity.END
            }

            barisItem.addView(tvNama)
            barisItem.addView(tvQty)
            barisItem.addView(tvSubtotal)
            wadahRincian.addView(barisItem)
        }

        findViewById<Button>(R.id.btnCetakPrinter).setOnClickListener {
            Toast.makeText(this, "Mengirim data ke Printer...", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnSelesai).setOnClickListener {
            val intentDashboard = Intent(this, DashboardKasirActivity::class.java)
            intentDashboard.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intentDashboard)
            finish()
        }
    }
}