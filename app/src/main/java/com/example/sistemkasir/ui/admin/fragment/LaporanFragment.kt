package com.example.sistemkasir.ui.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sistemkasir.databinding.FragmentLaporanBinding
import com.example.sistemkasir.model.Laporan
import com.example.sistemkasir.ui.admin.adapter.LaporanAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.Calendar
import java.util.Date

class LaporanFragment : Fragment() {

    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    private lateinit var adapter: LaporanAdapter
    private val listLaporan = ArrayList<Laporan>()

    // Variabel ini penting untuk mencegah penumpukan data saat ganti opsi tab dropdown
    private var listenerRegistrasi: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LaporanAdapter(listLaporan)
        binding.rvLaporan.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLaporan.adapter = adapter

        setupDropdownFilter()
    }

    private fun setupDropdownFilter() {
        // Opsi yang akan muncul saat dropdown diklik
        val opsiFilter = arrayOf("Semua Waktu", "Hari Ini", "Kemarin", "Bulan Ini")
        val dropdownAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, opsiFilter)

        binding.dropdownFilter.setAdapter(dropdownAdapter)

        // Atur teks awalan (Default)
        binding.dropdownFilter.setText("Semua Waktu", false)

        // Panggil data pertama kali
        tarikDataTransaksi("Semua Waktu")

        // Aksi ketika kasir mengklik salah satu opsi di dropdown
        binding.dropdownFilter.setOnItemClickListener { _, _, position, _ ->
            val pilihan = opsiFilter[position]
            tarikDataTransaksi(pilihan)
        }
    }

    private fun tarikDataTransaksi(filterWaktu: String) {
        // Hapus koneksi Real-time sebelumnya agar data tidak dobel saat tab diganti
        listenerRegistrasi?.remove()

        var query: Query = db.collection("Transaksi")
        val kalender = Calendar.getInstance()

        when (filterWaktu) {
            "Hari Ini" -> {
                kalender.set(Calendar.HOUR_OF_DAY, 0)
                kalender.set(Calendar.MINUTE, 0)
                kalender.set(Calendar.SECOND, 0)
                val mulai = kalender.time

                kalender.set(Calendar.HOUR_OF_DAY, 23)
                kalender.set(Calendar.MINUTE, 59)
                kalender.set(Calendar.SECOND, 59)
                val akhir = kalender.time

                query = query.whereGreaterThanOrEqualTo("waktu_transaksi", mulai)
                    .whereLessThanOrEqualTo("waktu_transaksi", akhir)
            }
            "Kemarin" -> {
                kalender.add(Calendar.DAY_OF_YEAR, -1) // Mundur 1 hari
                kalender.set(Calendar.HOUR_OF_DAY, 0)
                kalender.set(Calendar.MINUTE, 0)
                kalender.set(Calendar.SECOND, 0)
                val mulai = kalender.time

                kalender.set(Calendar.HOUR_OF_DAY, 23)
                kalender.set(Calendar.MINUTE, 59)
                kalender.set(Calendar.SECOND, 59)
                val akhir = kalender.time

                query = query.whereGreaterThanOrEqualTo("waktu_transaksi", mulai)
                    .whereLessThanOrEqualTo("waktu_transaksi", akhir)
            }
            "Bulan Ini" -> {
                kalender.set(Calendar.DAY_OF_MONTH, 1) // Set ke Tanggal 1
                kalender.set(Calendar.HOUR_OF_DAY, 0)
                kalender.set(Calendar.MINUTE, 0)
                kalender.set(Calendar.SECOND, 0)
                val mulai = kalender.time

                val akhir = Date() // Waktu saat ini (batas maksimal)

                query = query.whereGreaterThanOrEqualTo("waktu_transaksi", mulai)
                    .whereLessThanOrEqualTo("waktu_transaksi", akhir)
            }
            // Jika "Semua Waktu", biarkan query mengambil semua data tanpa 'where'
        }

        // Urutkan laporan dari yang paling baru
        query = query.orderBy("waktu_transaksi", Query.Direction.DESCENDING)

        // Simpan listener ke dalam variabel agar bisa dihentikan saat dropdown diganti lagi
        listenerRegistrasi = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Toast.makeText(requireContext(), "Gagal memuat laporan: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            listLaporan.clear()

            if (snapshots != null) {
                for (dokumen in snapshots) {
                    val timestamp = dokumen.getTimestamp("waktu_transaksi")
                    val tanggalAsli = timestamp?.toDate() ?: Date()
                    val metode = dokumen.getString("metode_pembayaran") ?: "Tidak Diketahui"

                    val total = (dokumen.getDouble("nominal_bayar") ?: 0.0) - (dokumen.getDouble("kembalian") ?: 0.0)

                    val daftarItem = dokumen.get("rincian") as? List<Map<String, Any>>
                    var stringPesanan = ""

                    if (daftarItem != null && daftarItem.isNotEmpty()) {
                        val stringBuilder = StringBuilder()
                        for ((index, item) in daftarItem.withIndex()) {
                            val nama = item["nama"] as? String ?: "Item"
                            val qty = (item["kuantitas"] as? Number)?.toLong() ?: 1L
                            stringBuilder.append("$nama x$qty")
                            if (index < daftarItem.size - 1) stringBuilder.append(", ")
                        }
                        stringPesanan = stringBuilder.toString()
                    } else {
                        stringPesanan = "Detail pesanan tidak tersedia"
                    }

                    val dataLaporan = Laporan(
                        id = dokumen.id,
                        detailPesanan = stringPesanan, // Tanpa memasukkan fotoUrl
                        tanggal = tanggalAsli,
                        jenis = metode,
                        totalPenjualan = total
                    )

                    listLaporan.add(dataLaporan)
                }
            }
            adapter.updateData(listLaporan)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hapus listener saat keluar dari halaman laporan untuk menghemat RAM memori HP
        listenerRegistrasi?.remove()
        _binding = null
    }
}