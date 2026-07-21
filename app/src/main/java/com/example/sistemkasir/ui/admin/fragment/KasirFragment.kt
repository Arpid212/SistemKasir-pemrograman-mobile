package com.example.sistemkasir.ui.admin.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sistemkasir.databinding.DialogKasirBinding
import com.example.sistemkasir.databinding.FragmentKasirBinding
import com.example.sistemkasir.model.Pengguna
import com.example.sistemkasir.ui.admin.adapter.KasirAdapter
import com.example.sistemkasir.utils.GeneratorUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions


// Pola sama persis kayak ProdukFragment: 1 dialog buat Tambah & Update.
// Bedanya sama Produk: pas Tambah, email & PIN digenerate otomatis (gak diinput
// manual), dan pas Update cuma NAMA yang bisa diubah - email/PIN dikunci karena
// itu kredensial login yang sudah dipakai kasir.
class KasirFragment : Fragment() {

    private var _binding: FragmentKasirBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: KasirAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKasirBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = KasirAdapter(
            emptyList(),
            onEdit = { docId, pengguna -> tampilkanDialog(docId, pengguna) },
            onDelete = { docId, pengguna -> hapus(docId, pengguna) }
        )
        binding.rvKasir.layoutManager = LinearLayoutManager(requireContext())
        binding.rvKasir.adapter = adapter

        muatKasir()
        binding.btnTambahPegawai.setOnClickListener { tampilkanDialog() }
    }

    private fun muatKasir() {
        db.collection("Pengguna").whereEqualTo("role", "Kasir")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.map { doc ->
                    doc.id to Pengguna(
                        id = 0,
                        nama = doc.getString("nama") ?: "",
                        email = doc.getString("email") ?: "",
                        pin = doc.getString("pin") ?: "",
                        role = doc.getString("role") ?: "Kasir"
                    )
                } ?: emptyList()
                adapter.updateData(list)
            }
    }

    private fun tampilkanDialog(docId: String? = null, penggunaLama: Pengguna? = null) {
        val form = DialogKasirBinding.inflate(layoutInflater)
        val modeEdit = docId != null

        form.edtNama.setText(penggunaLama?.nama ?: "")

        if (modeEdit) {
            form.groupKredensial.visibility = View.VISIBLE
            form.tvEmail.text = "Email: ${penggunaLama?.email}"
            form.tvPin.text = "PIN: ${penggunaLama?.pin}"
        } else {
            form.groupKredensial.visibility = View.GONE
        }

        form.btnSimpan.text = if (modeEdit) "SIMPAN DATA PEGAWAI" else "TAMBAHKAN PEGAWAI"

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (modeEdit) "Update Pegawai" else "Tambahkan Pegawai")
            .setView(form.root)
            .setNegativeButton("Batal", null)
            .create()

        form.btnSimpan.setOnClickListener {
            val nama = form.edtNama.text.toString().trim()
            if (nama.isEmpty()) {
                Toast.makeText(requireContext(), "Nama lengkap wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!modeEdit) {
                simpanKasirBaru(nama, dialog)
            } else {
                updateNamaKasir(docId!!, nama, dialog)
            }
        }
        dialog.show()
    }

    // Cek dulu berdasarkan NAMA (bukan email) - kalau sudah ada nama yang sama,
    // tanya dulu ke admin: ini orang yang sama (salah klik/typo) atau memang
    // 2 orang beda nama sama? Kalau lanjut, email dibedakan otomatis pakai angka.
    private fun simpanKasirBaru(nama: String, dialog: AlertDialog) {
        db.collection("Pengguna").whereEqualTo("nama", nama).get()
            .addOnSuccessListener { hasilNama ->
                if (!hasilNama.isEmpty) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Nama sudah terdaftar")
                        .setMessage("Sudah ada kasir bernama \"$nama\".")
                        .setPositiveButton("Buat dengan email yang berbeda") { _, _ ->
                            lanjutkanSimpanKasir(nama, dialog)
                        }
                        .setNegativeButton("Batal", null)
                        .show()
                } else {
                    lanjutkanSimpanKasir(nama, dialog)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal cek nama: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun lanjutkanSimpanKasir(nama: String, dialog: AlertDialog) {
        val emailDasar = GeneratorUtil.generateEmail(nama)
        val pin = GeneratorUtil.generatePin()

        cariEmailUnik(emailDasar, 0) { emailUnik ->
            val mainApp = FirebaseApp.getInstance()
            val options = mainApp.options

            var secondaryApp = FirebaseApp.getApps(requireContext()).find { it.name == "SecondaryApp" }
            if (secondaryApp == null) {
                secondaryApp = FirebaseApp.initializeApp(requireContext(), options, "SecondaryApp")
            }

            val secondaryAuth = FirebaseAuth.getInstance(secondaryApp!!)

            secondaryAuth.createUserWithEmailAndPassword(emailUnik, pin)
                .addOnSuccessListener {

                    secondaryAuth.signOut()

                    val data = hashMapOf<String, Any>(
                        "nama" to nama, "email" to emailUnik, "pin" to pin, "role" to "Kasir"
                    )

                    db.collection("Pengguna").add(data)
                        .addOnSuccessListener {
                            dialog.dismiss()
                            tampilkanKredensial(emailUnik, pin)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Profil gagal disimpan: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Gagal buat akses: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // Cek email ke Firestore satu-satu: budisan@kafe.com sudah dipakai? coba
    // budisan1@kafe.com, masih dipakai? coba budisan2@kafe.com, dst sampai ketemu yang bebas.
    private fun cariEmailUnik(emailDasar: String, percobaan: Int, onFound: (String) -> Unit) {
        val emailDicoba = if (percobaan == 0) {
            emailDasar
        } else {
            val bagianDepan = emailDasar.substringBefore("@")
            val domain = emailDasar.substringAfter("@")
            "$bagianDepan$percobaan@$domain"
        }

        db.collection("Pengguna").whereEqualTo("email", emailDicoba).get()
            .addOnSuccessListener { hasil ->
                if (hasil.isEmpty) onFound(emailDicoba)
                else cariEmailUnik(emailDasar, percobaan + 1, onFound)
            }
            .addOnFailureListener { onFound(emailDicoba) }
    }

    private fun updateNamaKasir(docId: String, nama: String, dialog: AlertDialog) {
        db.collection("Pengguna").document(docId).update("nama", nama)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Berhasil disimpan", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun tampilkanKredensial(email: String, pin: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Akun kasir berhasil dibuat")
            .setMessage("Email: $email\nPIN: $pin\n\nSampaikan kredensial ini ke kasir yang bersangkutan.")
            .setPositiveButton("OK", null)
            .setCancelable(false)
            .show()
    }

    private fun hapus(docId: String, pengguna: Pengguna) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Akun Kasir")
            .setMessage("Yakin hapus akun \"${pengguna.nama}\"?")
            .setPositiveButton("Hapus") { _, _ -> db.collection("Pengguna").document(docId).delete() }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}