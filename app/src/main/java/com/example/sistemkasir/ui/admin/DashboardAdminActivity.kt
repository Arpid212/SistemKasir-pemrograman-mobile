package com.example.sistemkasir.ui.admin

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sistemkasir.R
import com.example.sistemkasir.ui.admin.fragment.DashboardFragment
import com.example.sistemkasir.ui.admin.fragment.KasirFragment
import com.example.sistemkasir.ui.admin.fragment.LaporanFragment
import com.example.sistemkasir.ui.admin.fragment.ProdukFragment

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var txtJudul: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_admin)

        txtJudul = findViewById(R.id.txtJudul)

        val btnDashboard = findViewById<Button>(R.id.btnDashboard)
        val btnProduk = findViewById<Button>(R.id.btnProduk)
        val btnLaporan = findViewById<Button>(R.id.btnLaporan)
        val btnKasir = findViewById<Button>(R.id.btnKasir)

        btnDashboard.setOnClickListener {
            txtJudul.text = "Dashboard"
            replaceFragment(DashboardFragment())
        }

        btnProduk.setOnClickListener {
            txtJudul.text = "Manajemen Menu"
            replaceFragment(ProdukFragment())
        }

        btnLaporan.setOnClickListener {
            txtJudul.text = "Laporan"
            replaceFragment(LaporanFragment())
        }

        btnKasir.setOnClickListener {
            txtJudul.text = "Manajemen Kasir"
            replaceFragment(KasirFragment())
        }

        if (savedInstanceState == null) {
            txtJudul.text = "Dashboard"
            replaceFragment(DashboardFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}