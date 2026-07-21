package com.example.sistemkasir.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sistemkasir.R
import com.example.sistemkasir.ui.admin.DashboardAdminActivity
import com.example.sistemkasir.ui.kasir.DashboardKasirActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val inputEmail = findViewById<TextInputEditText>(R.id.inputEmail)
        val inputPin = findViewById<TextInputEditText>(R.id.inputPin)
        val tombolMasuk = findViewById<Button>(R.id.tombolMasuk)

        tombolMasuk.setOnClickListener {
            val email = inputEmail.text.toString().trim()
            val pin = inputPin.text.toString().trim()

            if (email.isNotEmpty() && pin.isNotEmpty()) {
                prosesLogin(email, pin)
            } else {
                Toast.makeText(this, "Email dan PIN tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prosesLogin(email: String, pin: String) {
        auth.signInWithEmailAndPassword(email, pin)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    cekRolePengguna(email)
                } else {
                    val pesanError = task.exception?.message ?: "Kesalahan tidak diketahui"
                    Toast.makeText(this, "Gagal Login: $pesanError", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun cekRolePengguna(email: String) {
        db.collection("Pengguna")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val role = document.getString("role") ?: ""
                        val name = document.getString("nama") ?: document.getString("name") ?: "Pengguna"

                        val sharedPref = getSharedPreferences("SesiSistemKasir", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("NAMA_USER", name)
                            putString("ROLE_USER", role)
                            apply()
                        }

                        Toast.makeText(this, "Selamat datang, $name ($role)", Toast.LENGTH_LONG).show()

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
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val sharedPref = getSharedPreferences("SesiSistemKasir", Context.MODE_PRIVATE)
            val role = sharedPref.getString("ROLE_USER", "")

            when (role) {
                "Admin" -> {
                    startActivity(Intent(this, DashboardAdminActivity::class.java))
                    finish()
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