package com.example.sistemkasir.ui.admin.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sistemkasir.databinding.DialogProdukBinding
import com.example.sistemkasir.databinding.FragmentProdukBinding
import com.example.sistemkasir.model.Produk
import com.example.sistemkasir.ui.admin.adapter.ProdukAdapter
import com.google.firebase.firestore.FirebaseFirestore

// VERSI FALLBACK - tanpa Firebase Storage. Foto pakai input URL manual
// (edtFoto). Gak nambah dependency baru sama sekali, aman kalau internet
// buat resolve Storage lagi bermasalah.
class ProdukFragment : Fragment() {

    private var _binding: FragmentProdukBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ProdukAdapter
    private val kategoriList = arrayOf("Kopi", "Non-Kopi", "Makanan")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProdukBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ProdukAdapter(
            emptyList(),
            onEdit = { docId, produk -> tampilkanDialog(docId, produk) },
            onDelete = { docId, produk -> hapus(docId, produk) }
        )
        binding.rvProduk.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProduk.adapter = adapter

        muatProduk()
        binding.btnTambahProduk.setOnClickListener { tampilkanDialog() }
    }

    private fun muatProduk() {
        db.collection("Produk").addSnapshotListener { snapshot, _ ->
            val list = snapshot?.documents?.map { doc ->
                doc.id to Produk(
                    nama = doc.getString("nama") ?: "",
                    harga = doc.getDouble("harga") ?: 0.0,
                    deskripsi = doc.getString("deskripsi") ?: "",
                    stok = (doc.getLong("stok") ?: 0).toInt(),
                    foto = doc.getString("foto") ?: "",
                    kategori = doc.getString("kategori") ?: ""
                )
            } ?: emptyList()
            adapter.updateData(list)
        }
    }

    private fun tampilkanDialog(docId: String? = null, produkLama: Produk? = null) {
        val form = DialogProdukBinding.inflate(layoutInflater)
        form.spKategori.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, kategoriList
        )

        produkLama?.let {
            form.edtNama.setText(it.nama)
            form.edtHarga.setText(it.harga.toString())
            form.edtStok.setText(it.stok.toString())
            form.edtDeskripsi.setText(it.deskripsi)
            form.edtFoto.setText(it.foto)
            form.spKategori.setSelection(kategoriList.indexOf(it.kategori).coerceAtLeast(0))
        }

        form.btnSimpan.text = if (docId == null) "TAMBAH MENU BARU" else "SIMPAN DATA MENU"

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (docId == null) "Tambah Produk" else "Edit Produk")
            .setView(form.root)
            .setNegativeButton("Batal", null)
            .create()

        form.btnSimpan.setOnClickListener {
            val nama = form.edtNama.text.toString().trim()
            val harga = form.edtHarga.text.toString().toDoubleOrNull()
            val stok = form.edtStok.text.toString().toIntOrNull()
            val deskripsi = form.edtDeskripsi.text.toString().trim()
            val foto = form.edtFoto.text.toString().trim()
            val kategori = form.spKategori.selectedItem.toString()

            if (nama.isEmpty() || harga == null || stok == null) {
                Toast.makeText(requireContext(), "Lengkapi semua data dengan benar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = hashMapOf<String, Any>(
                "nama" to nama, "harga" to harga, "stok" to stok,
                "deskripsi" to deskripsi, "kategori" to kategori, "foto" to foto
            )

            val tugas = if (docId == null) db.collection("Produk").add(data)
            else db.collection("Produk").document(docId).update(data)

            tugas.addOnSuccessListener {
                Toast.makeText(requireContext(), "Berhasil disimpan", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun hapus(docId: String, produk: Produk) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Yakin hapus \"${produk.nama}\"?")
            .setPositiveButton("Hapus") { _, _ -> db.collection("Produk").document(docId).delete() }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}