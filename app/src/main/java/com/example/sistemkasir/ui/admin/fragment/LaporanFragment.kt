package com.example.sistemkasir.ui.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sistemkasir.databinding.FragmentLaporanBinding
import com.example.sistemkasir.model.Laporan
import com.example.sistemkasir.ui.admin.adapter.LaporanAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class LaporanFragment : Fragment() {

    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    private lateinit var adapter: LaporanAdapter
    private val listLaporan = ArrayList<Laporan>()

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

        tarikDataTransaksiAsli()
    }

    private fun tarikDataTransaksiAsli() {
        db.collection("Transaksi")
            .orderBy("waktu_transaksi", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    Toast.makeText(requireContext(), "Gagal memuat laporan: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                listLaporan.clear()

                for (dokumen in snapshots!!) {
                    val timestamp = dokumen.getTimestamp("waktu_transaksi")
                    val tanggalAsli = timestamp?.toDate() ?: Date()
                    val metode = dokumen.getString("metode_pembayaran") ?: "Tidak Diketahui"
                    val total = dokumen.getDouble("total_tagihan") ?: 0.0

                    val dataLaporan = Laporan(
                        id = dokumen.id,
                        tanggal = tanggalAsli,
                        jenis = metode,
                        totalPenjualan = total
                    )

                    listLaporan.add(dataLaporan)
                }

                adapter.updateData(listLaporan)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}