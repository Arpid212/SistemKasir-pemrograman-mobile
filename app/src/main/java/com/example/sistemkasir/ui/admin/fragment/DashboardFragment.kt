package com.example.sistemkasir.ui.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sistemkasir.R
import com.example.sistemkasir.databinding.FragmentDashboardBinding
import com.example.sistemkasir.model.Pengguna
import com.example.sistemkasir.ui.admin.adapter.KasirAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    private lateinit var adapterKasir: KasirAdapter
    private val listPreviewKasir = ArrayList<Pair<String, Pengguna>>()

    // Variabel untuk menyimpan angka pendapatan utuh agar bisa ditarik oleh Pop-up
    private var nominalPendapatanUtuh: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hitungStatistikOtomatis()

        adapterKasir = KasirAdapter(
            daftar = listPreviewKasir,
            onEdit = { _, _ ->
                Toast.makeText(requireContext(), "Buka halaman Kasir untuk mengedit data.", Toast.LENGTH_SHORT).show()
                geserKeHalamanKasir()
            },
            onDelete = { _, _ ->
                Toast.makeText(requireContext(), "Buka halaman Kasir untuk menghapus data.", Toast.LENGTH_SHORT).show()
                geserKeHalamanKasir()
            }
        )

        binding.rvRiwayat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRiwayat.adapter = adapterKasir

        muatPreviewKasir()

        binding.btnLihatSemuaKasir.setOnClickListener {
            geserKeHalamanKasir()
        }

        // Aksi klik untuk CardView Pendapatan (Memunculkan Pop-up)
        binding.cardPendapatan.setOnClickListener {
            tampilkanPopupPendapatan()
        }
    }

    private fun tampilkanPopupPendapatan() {
        // Format ulang angka menjadi Rupiah yang rapi
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val teksNominal = formatRupiah.format(nominalPendapatanUtuh).replace("Rp", "Rp ")

        // Membuat dan menampilkan dialog pop-up Material Design
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Total Pendapatan Sistem")
            .setMessage("Akumulasi seluruh pendapatan yang tercatat adalah:\n\n$teksNominal")
            .setPositiveButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun muatPreviewKasir() {
        db.collection("Pengguna")
            .whereEqualTo("role", "Kasir")
            .limit(3)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                listPreviewKasir.clear()
                for (dokumen in snapshots) {
                    val idAngka = dokumen.getLong("id")?.toInt() ?: 0

                    val pengguna = Pengguna(
                        id = idAngka,
                        nama = dokumen.getString("nama") ?: "",
                        email = dokumen.getString("email") ?: "",
                        pin = dokumen.getString("pin") ?: "",
                        role = dokumen.getString("role") ?: "Kasir"
                    )

                    listPreviewKasir.add(Pair(dokumen.id, pengguna))
                }
                adapterKasir.updateData(listPreviewKasir)
            }
    }

    private fun geserKeHalamanKasir() {
        val tombolKasir = requireActivity().findViewById<Button>(R.id.btnKasir)
        if (tombolKasir != null) {
            tombolKasir.performClick()
        } else {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, KasirFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun hitungStatistikOtomatis() {
        db.collection("Transaksi").addSnapshotListener { snapshots, error ->
            if (error != null || snapshots == null) return@addSnapshotListener

            var totalPendapatan = 0.0
            var penjualanHariIni = 0

            val kalenderAwal = Calendar.getInstance()
            kalenderAwal.set(Calendar.HOUR_OF_DAY, 0)
            kalenderAwal.set(Calendar.MINUTE, 0)
            kalenderAwal.set(Calendar.SECOND, 0)
            kalenderAwal.set(Calendar.MILLISECOND, 0)
            val mulaiHariIni = kalenderAwal.time

            val kalenderAkhir = Calendar.getInstance()
            kalenderAkhir.set(Calendar.HOUR_OF_DAY, 23)
            kalenderAkhir.set(Calendar.MINUTE, 59)
            kalenderAkhir.set(Calendar.SECOND, 59)
            kalenderAkhir.set(Calendar.MILLISECOND, 999)
            val akhirHariIni = kalenderAkhir.time

            for (dokumen in snapshots) {
                val totalTagihan = dokumen.getDouble("total_tagihan") ?: 0.0
                val waktuTransaksi = dokumen.getTimestamp("waktu_transaksi")?.toDate() ?: Date()

                totalPendapatan += totalTagihan

                if (waktuTransaksi.after(mulaiHariIni) && waktuTransaksi.before(akhirHariIni)) {
                    penjualanHariIni += 1
                }
            }

            // Simpan angka utuh ke dalam variabel global
            nominalPendapatanUtuh = totalPendapatan

            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.txtPendapatan.text = formatRupiah.format(totalPendapatan).replace("Rp", "Rp ")
            binding.txtPenjualan.text = penjualanHariIni.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}