package com.example.sistemkasir.ui.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sistemkasir.R
import com.example.sistemkasir.model.Produk
import com.example.sistemkasir.ui.admin.adapter.ProdukAdapter
import com.google.firebase.firestore.FirebaseFirestore
import android.app.AlertDialog
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Button

class ProdukFragment : Fragment() {

    private lateinit var rvProduk: RecyclerView
    private lateinit var btnTambah: Button
    private lateinit var adapter: ProdukAdapter

    private val listProduk = ArrayList<Produk>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_produk, container, false)

        initView(view)
        initRecyclerView()
        loadProduk()

        btnTambah.setOnClickListener {
            showTambahDialog()
        }

        return view
    }

    private fun initView(view: View) {
        rvProduk = view.findViewById(R.id.rvProduk)
        btnTambah = view.findViewById(R.id.btnTambahProduk)
    }

    private fun initRecyclerView() {

        adapter = ProdukAdapter(
            listProduk,

            onEdit = { produk ->
                showEditDialog(produk)
            },

            onDelete = { produk ->
                hapusProduk(produk)
            }

        )

        rvProduk.layoutManager = LinearLayoutManager(requireContext())
        rvProduk.adapter = adapter
    }

    private fun loadProduk() {

        db.collection("Produk")
            .get()
            .addOnSuccessListener { documents ->

                listProduk.clear()

                for (document in documents) {

                    val produk = document.toObject(Produk::class.java)

                    listProduk.add(produk)

                }

                adapter.notifyDataSetChanged()

            }
    }

    private fun generateId(onComplete: (Int) -> Unit) {

        db.collection("Produk")
            .get()
            .addOnSuccessListener { documents ->

                var maxId = 0

                for (document in documents) {

                    val id = document.getLong("id")?.toInt() ?: 0

                    if (id > maxId) {
                        maxId = id
                    }

                }

                onComplete(maxId + 1)

            }

    }

    private fun showTambahDialog() {

        val dialogView = layoutInflater.inflate(R.layout.dialog_produk, null)

        val edtNama = dialogView.findViewById<EditText>(R.id.edtNama)
        val edtHarga = dialogView.findViewById<EditText>(R.id.edtHarga)
        val edtStok = dialogView.findViewById<EditText>(R.id.edtStok)
        val edtDeskripsi = dialogView.findViewById<EditText>(R.id.edtDeskripsi)
        val spKategori = dialogView.findViewById<Spinner>(R.id.spKategori)

        val kategori = arrayOf("Kopi", "Non Kopi")

        spKategori.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            kategori
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Produk")
            .setView(dialogView)

            .setPositiveButton("Simpan") { _, _ ->

                val nama = edtNama.text.toString().trim()
                val harga = edtHarga.text.toString().trim()
                val stok = edtStok.text.toString().trim()
                val deskripsi = edtDeskripsi.text.toString().trim()
                val kategoriDipilih = spKategori.selectedItem.toString()

                if (nama.isEmpty() ||
                    harga.isEmpty() ||
                    stok.isEmpty() ||
                    deskripsi.isEmpty()
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Semua data harus diisi!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                generateId { idBaru ->

                    val produk = hashMapOf(

                        "id" to idBaru,

                        "nama" to nama,

                        "harga" to harga.toDouble(),

                        "stok" to stok.toInt(),

                        "deskripsi" to deskripsi,

                        "kategori" to kategoriDipilih,

                        "foto" to ""

                    )

                    db.collection("Produk")
                        .add(produk)
                        .addOnSuccessListener {

                            Toast.makeText(
                                requireContext(),
                                "Produk berhasil ditambahkan",
                                Toast.LENGTH_SHORT
                            ).show()

                            loadProduk()

                        }
                        .addOnFailureListener {

                            Toast.makeText(
                                requireContext(),
                                "Gagal menambahkan produk",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                }

            }

            .setNegativeButton("Batal", null)

            .show()

    }

    private fun showEditDialog(produk: Produk) {

        val dialogView = layoutInflater.inflate(R.layout.dialog_produk, null)

        val edtNama = dialogView.findViewById<EditText>(R.id.edtNama)
        val edtHarga = dialogView.findViewById<EditText>(R.id.edtHarga)
        val edtStok = dialogView.findViewById<EditText>(R.id.edtStok)
        val edtDeskripsi = dialogView.findViewById<EditText>(R.id.edtDeskripsi)
        val spKategori = dialogView.findViewById<Spinner>(R.id.spKategori)

        val kategori = arrayOf("Kopi", "Non Kopi")

        spKategori.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            kategori
        )

        // Isi data lama
        edtNama.setText(produk.nama)
        edtHarga.setText(produk.harga.toString())
        edtStok.setText(produk.stok.toString())
        edtDeskripsi.setText(produk.deskripsi)

        val posisiKategori = kategori.indexOf(produk.kategori)

        if (posisiKategori != -1) {
            spKategori.setSelection(posisiKategori)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Produk")
            .setView(dialogView)

            .setPositiveButton("Simpan") { _, _ ->

                val namaBaru = edtNama.text.toString().trim()
                val hargaBaru = edtHarga.text.toString().trim()
                val stokBaru = edtStok.text.toString().trim()
                val deskripsiBaru = edtDeskripsi.text.toString().trim()
                val kategoriBaru = spKategori.selectedItem.toString()

                if (
                    namaBaru.isEmpty() ||
                    hargaBaru.isEmpty() ||
                    stokBaru.isEmpty() ||
                    deskripsiBaru.isEmpty()
                ) {

                    Toast.makeText(
                        requireContext(),
                        "Semua data harus diisi!",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setPositiveButton
                }

                val dataBaru = hashMapOf<String, Any>(
                    "id" to produk.id,
                    "nama" to namaBaru,
                    "harga" to hargaBaru.toDouble(),
                    "stok" to stokBaru.toInt(),
                    "deskripsi" to deskripsiBaru,
                    "kategori" to kategoriBaru,
                    "foto" to ""
                )

                db.collection("Produk")
                    .whereEqualTo("nama", produk.nama)
                    .get()
                    .addOnSuccessListener { documents ->

                        if (!documents.isEmpty) {

                            val docId = documents.documents[0].id

                            db.collection("Produk")
                                .document(docId)
                                .update(dataBaru)
                                .addOnSuccessListener {

                                    Toast.makeText(
                                        requireContext(),
                                        "Produk berhasil diubah",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    loadProduk()

                                }
                                .addOnFailureListener {

                                    Toast.makeText(
                                        requireContext(),
                                        "Gagal mengubah produk",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }

                        } else {

                            Toast.makeText(
                                requireContext(),
                                "Produk tidak ditemukan",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                    }

            }

            .setNegativeButton("Batal", null)

            .show()

    }

    private fun hapusProduk(produk: Produk) {

        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Yakin ingin menghapus ${produk.nama}?")

            .setPositiveButton("Hapus") { _, _ ->

                Toast.makeText(
                    requireContext(),
                    "Mencari ID = ${produk.id}",
                    Toast.LENGTH_SHORT
                ).show()

                db.collection("Produk")
                    .whereEqualTo("id", produk.id)
                    .get()
                    .addOnSuccessListener { documents ->

                        Toast.makeText(
                            requireContext(),
                            "Jumlah data ditemukan = ${documents.size()}",
                            Toast.LENGTH_LONG
                        ).show()

                        if (documents.isEmpty) {

                            Toast.makeText(
                                requireContext(),
                                "Data dengan ID ${produk.id} tidak ditemukan!",
                                Toast.LENGTH_LONG
                            ).show()

                            return@addOnSuccessListener
                        }

                        val docId = documents.documents[0].id

                        Toast.makeText(
                            requireContext(),
                            "Document ID = $docId",
                            Toast.LENGTH_LONG
                        ).show()

                        db.collection("Produk")
                            .document(docId)
                            .delete()
                            .addOnSuccessListener {

                                Toast.makeText(
                                    requireContext(),
                                    "Produk berhasil dihapus",
                                    Toast.LENGTH_SHORT
                                ).show()

                                loadProduk()

                            }
                            .addOnFailureListener { e ->

                                Toast.makeText(
                                    requireContext(),
                                    "Gagal menghapus: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()

                            }

                    }
                    .addOnFailureListener { e ->

                        Toast.makeText(
                            requireContext(),
                            "Query gagal: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()

                    }

            }

            .setNegativeButton("Batal", null)

            .show()

    }

}