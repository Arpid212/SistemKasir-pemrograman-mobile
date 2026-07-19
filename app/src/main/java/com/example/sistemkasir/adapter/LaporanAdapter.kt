package com.example.sistemkasir.ui.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sistemkasir.R
import com.example.sistemkasir.model.Laporan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class LaporanAdapter(
    private var listLaporan: List<Laporan>
) : RecyclerView.Adapter<LaporanAdapter.LaporanViewHolder>() {

    class LaporanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomorStruk: TextView = itemView.findViewById(R.id.tvNomorStruk)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvTotalHarga: TextView = itemView.findViewById(R.id.tvTotalHarga)
        val tvMetodePembayaran: TextView = itemView.findViewById(R.id.tvMetodePembayaran)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaporanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_laporan, parent, false)
        return LaporanViewHolder(view)
    }

    override fun onBindViewHolder(holder: LaporanViewHolder, position: Int) {
        val laporan = listLaporan[position]

        // 1. Mengubah format Date menjadi teks yang rapi (Contoh: 16 Jul 2026, 08:45)
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        holder.tvTanggal.text = sdf.format(laporan.tanggal)

        // 2. Mengambil data dari model Laporan
        holder.tvMetodePembayaran.text = laporan.jenis

        // 3. Format angka ke Rupiah
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        holder.tvTotalHarga.text = formatRupiah.format(laporan.totalPenjualan)

        // 4. Data Tambahan (Karena di Laporan tidak ada variabel struk & status, kita manfaatkan ID Firestore)
        // Memotong ID agar tidak terlalu panjang di layar
        val idPendek = if (laporan.id.length > 6) laporan.id.take(6).uppercase() else laporan.id
        holder.tvNomorStruk.text = "TRX-$idPendek"
        holder.tvStatus.text = "Selesai" // Transaksi yang masuk ke DB sudah pasti Selesai
    }

    override fun getItemCount(): Int {
        return listLaporan.size
    }

    // ✨ Fungsi update menerima List<Laporan>
    fun updateData(newList: List<Laporan>) {
        listLaporan = newList
        notifyDataSetChanged()
    }
}