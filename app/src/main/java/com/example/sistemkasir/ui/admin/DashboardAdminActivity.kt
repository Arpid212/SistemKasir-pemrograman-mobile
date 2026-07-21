package com.example.sistemkasir.ui.admin

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
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
import com.example.sistemkasir.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var txtJudul: TextView
    private lateinit var btnDashboard: Button
    private lateinit var btnProduk: Button
    private lateinit var btnLaporan: Button
    private lateinit var btnKasir: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_admin)

        txtJudul = findViewById(R.id.txtJudul)
        val btnKeluar = findViewById<Button>(R.id.btnKeluar)

        // Inisialisasi tombol navigasi
        btnDashboard = findViewById(R.id.btnDashboard)
        btnProduk = findViewById(R.id.btnProduk)
        btnLaporan = findViewById(R.id.btnLaporan)
        btnKasir = findViewById(R.id.btnKasir)

        // Logika Tombol Navigasi
        btnDashboard.setOnClickListener {
            txtJudul.text = "Dashboard"
            replaceFragment(DashboardFragment())
            updateWarnaTombol(btnDashboard)
        }

        btnProduk.setOnClickListener {
            txtJudul.text = "Manajemen Menu"
            replaceFragment(ProdukFragment())
            updateWarnaTombol(btnProduk)
        }

        btnLaporan.setOnClickListener {
            txtJudul.text = "Laporan"
            replaceFragment(LaporanFragment())
            updateWarnaTombol(btnLaporan)
        }

        btnKasir.setOnClickListener {
            txtJudul.text = "Manajemen Kasir"
            replaceFragment(KasirFragment())
            updateWarnaTombol(btnKasir)
        }
        btnKeluar.setOnClickListener {
            tampilkanDialogKeluar()
        }

        if (savedInstanceState == null) {
            txtJudul.text = "Dashboard"
            replaceFragment(DashboardFragment())
            updateWarnaTombol(btnDashboard)
        }
    }

    private fun updateWarnaTombol(tombolAktif: Button) {
        val semuaTombol = arrayOf(btnDashboard, btnProduk, btnLaporan, btnKasir)

        for (tombol in semuaTombol) {
            if (tombol == tombolAktif) {
                tombol.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
                tombol.setTextColor(Color.parseColor("#FFFFFF"))
            } else {
                tombol.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F5F5F5"))
                tombol.setTextColor(Color.parseColor("#000000"))
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun tampilkanDialogKeluar() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari akun Admin?")
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
}