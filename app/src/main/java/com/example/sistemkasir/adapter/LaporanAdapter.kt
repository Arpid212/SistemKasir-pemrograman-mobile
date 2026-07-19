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
        val tvDetailPesanan: TextView = itemView.findViewById(R.id.tvDetailPesanan)
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

        val idPendek = if (laporan.id.length > 6) laporan.id.take(6).uppercase() else laporan.id
        holder.tvNomorStruk.text = "TRX-$idPendek"

        holder.tvDetailPesanan.text = laporan.detailPesanan

        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        holder.tvTanggal.text = sdf.format(laporan.tanggal)

        holder.tvMetodePembayaran.text = laporan.jenis

        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        holder.tvTotalHarga.text = formatRupiah.format(laporan.totalPenjualan)

        holder.tvStatus.text = "Selesai"
    }

    override fun getItemCount(): Int {
        return listLaporan.size
    }

    fun updateData(newList: List<Laporan>) {
        listLaporan = newList
        notifyDataSetChanged()
    }
}