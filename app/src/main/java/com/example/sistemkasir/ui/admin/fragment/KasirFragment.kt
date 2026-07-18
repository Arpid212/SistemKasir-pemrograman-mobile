package com.example.sistemkasir.ui.admin.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sistemkasir.R
import com.example.sistemkasir.ui.auth.LoginActivity
import com.example.sistemkasir.utils.GeneratorUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class KasirFragment : Fragment() {

    // Wajib ada untuk Fragment agar layout fragment_kasir.xml bisa ditampilkan
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_kasir, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hubungkan tombol tambah kasir dari fragment_kasir.xml
        // Pastikan ID-nya sesuai dengan yang ada di layout XML Anda
        val btnTambahKasir = view.findViewById<Button>(R.id.btnTambahKasir)
        btnTambahKasir?.setOnClickListener {
            tampilkanDialogTambahKasir()
        }

        // TODO: Inisialisasi RecyclerView untuk menampilkan daftar kasir di sini
    }

    private fun tampilkanDialogTambahKasir() {
        // Jika nama file Anda dialog_kasir.xml, ubah menjadi seperti ini:
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_kasir, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val inputNama = dialogView.findViewById<EditText>(R.id.inputNamaLengkap)
        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val btnSimpan = dialogView.findViewById<Button>(R.id.btnSimpan)

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnSimpan.setOnClickListener {
            val namaLengkap = inputNama.text.toString().trim()

            if (namaLengkap.isNotEmpty()) {
                dialog.dismiss()
                prosesPembuatanAkun(namaLengkap)
            } else {
                Toast.makeText(requireContext(), "Nama lengkap harus diisi!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun prosesPembuatanAkun(namaLengkap: String) {
        val emailBaru = GeneratorUtil.generateEmail(namaLengkap)
        val pinBaru = GeneratorUtil.generatePin()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailBaru, pinBaru)
            .addOnCompleteListener { task ->
                // PERBAIKAN: Cek apakah fragment masih aktif sebelum update UI
                if (!isAdded) return@addOnCompleteListener

                if (task.isSuccessful) {
                    simpanKeFirestore(namaLengkap, emailBaru, pinBaru)
                } else {
                    Toast.makeText(requireContext(), "Gagal buat akun Auth: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun simpanKeFirestore(namaLengkap: String, emailBaru: String, pinBaru: String) {
        val dataKasir = hashMapOf(
            "name" to namaLengkap,
            "email" to emailBaru,
            "pin" to pinBaru,
            "role" to "Kasir"
        )

        FirebaseFirestore.getInstance().collection("Pengguna")
            .add(dataKasir)
            .addOnSuccessListener {
                // PERBAIKAN: Cek apakah fragment masih aktif
                if (!isAdded) return@addOnSuccessListener

                tampilkanInfoAkunSukses(namaLengkap, emailBaru, pinBaru)
            }
            .addOnFailureListener { e ->
                // PERBAIKAN: Cek apakah fragment masih aktif
                if (!isAdded) return@addOnFailureListener

                Toast.makeText(requireContext(), "Gagal simpan database: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun tampilkanInfoAkunSukses(nama: String, email: String, pin: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Kasir Berhasil Ditambahkan!")
            .setMessage("Serahkan data ini kepada Kasir:\n\nNama: $nama\nEmail: $email\nPIN: $pin")
            .setPositiveButton("Tutup & Relogin") { dialog, _ ->
                dialog.dismiss()
                kembaliKeLogin()
            }
            .show()
    }

    private fun kembaliKeLogin() {
        FirebaseAuth.getInstance().signOut()

        val sharedPref = requireActivity().getSharedPreferences("SesiSistemKasir", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}