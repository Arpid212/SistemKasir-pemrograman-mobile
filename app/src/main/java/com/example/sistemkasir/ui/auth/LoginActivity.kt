package com.example.sistemkasir.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sistemkasir.R
import com.example.sistemkasir.ui.admin.DashboardAdminActivity
import com.example.sistemkasir.ui.kasir.DashboardKasirActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    // Deklarasi variabel
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Hubungkan variabel dengan ID di XML
        val inputEmail = findViewById<EditText>(R.id.inputEmail)
        val inputPin = findViewById<EditText>(R.id.inputPin)
        val tombolMasuk = findViewById<Button>(R.id.tombolMasuk)

        // Aksi ketika tombol ditekan (Logika Asli Diaktifkan)
        tombolMasuk.setOnClickListener {
            val email = inputEmail.text.toString().trim()
            val pin = inputPin.text.toString().trim()

            if (email.isNotEmpty() && pin.isNotEmpty()) {
                prosesLogin(email, pin)
            } else {
                Toast.makeText(this, "Email dan PIN tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }
            //UNTUK KEPERLUAN TESTNG TANPA LOGIN
        //tombolMasuk.setOnClickListener {
            // Simulasi penyimpanan sesi agar fitur transaksi tidak error/kosong
            //val sharedPref = getSharedPreferences("SesiSistemKasir", Context.MODE_PRIVATE)
            //with(sharedPref.edit()) {
             //   putString("NAMA_USER", "Admin Tester")
             //   putString("ROLE_USER", "Admin")
                //apply()
         //   }
         //   startActivity(Intent(this, DashboardKasirActivity::class.java))
         //   finish()
      //  }
    }

    private fun prosesLogin(email: String, pin: String) {
        // Firebase Auth untuk mengecek email dan password (PIN)
        auth.signInWithEmailAndPassword(email, pin)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Jika login berhasil, cek Role di Firestore
                    cekRolePengguna(email)
                } else {
                    Toast.makeText(this, "Gagal Login: Cek kembali Email dan PIN", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun cekRolePengguna(email: String) {
        // Mencari data user di tabel 'Pengguna' berdasarkan email
        db.collection("Pengguna")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val role = document.getString("role") ?: ""
                        // PERBAIKAN: Menyesuaikan dengan nama field di database ("name", bukan "nama")
                        val name = document.getString("name") ?: "Pengguna"

                        // IMPLEMENTASI SESSION: Simpan nama dan role ke SharedPreferences
                        val sharedPref = getSharedPreferences("SesiSistemKasir", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("NAMA_USER", name)
                            putString("ROLE_USER", role)
                            apply() // Simpan secara asinkron
                        }

                        Toast.makeText(this, "Selamat datang, $name ($role)", Toast.LENGTH_LONG).show()

                        // Arahkan ke halaman Dashboard sesuai role (Admin/Kasir)
                        arahkanBerdasarkanRole(role)
                    }
                } else {
                    Toast.makeText(this, "Data role tidak ditemukan di sistem.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        // Cek apakah ada user yang sedang login di Firebase
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Jika ada, ambil role dari SharedPreferences
            val sharedPref = getSharedPreferences("SesiSistemKasir", Context.MODE_PRIVATE)
            val role = sharedPref.getString("ROLE_USER", "")

            // Langsung arahkan ke dashboard tanpa harus login ulang
            when (role) {
                "Admin" -> {
                    startActivity(Intent(this, DashboardAdminActivity::class.java))
                    finish() // Hapus LoginActivity dari tumpukan agar tidak bisa di-back
                }
                "Kasir" -> {
                    startActivity(Intent(this, DashboardKasirActivity::class.java))
                    finish()
                }
            }
        }
    }
    private fun arahkanBerdasarkanRole(role: String) {
        when (role) {
            "Admin" -> {
                val intent = Intent(this, DashboardAdminActivity::class.java)
                startActivity(intent)
                finish()
            }
            "Kasir" -> {
                // PERBAIKAN: Melengkapi navigasi untuk Kasir
                val intent = Intent(this, DashboardKasirActivity::class.java)
                startActivity(intent)
                finish()
            }
            else -> {
                Toast.makeText(this, "Role tidak valid!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}