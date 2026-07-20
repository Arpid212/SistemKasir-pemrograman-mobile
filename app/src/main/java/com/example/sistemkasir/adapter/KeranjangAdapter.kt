package com.example.sistemkasir.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sistemkasir.R
import com.example.sistemkasir.model.ItemKeranjang
import java.text.NumberFormat
import java.util.Locale

class KeranjangAdapter(
    private var listKeranjang: List<ItemKeranjang>
) : RecyclerView.Adapter<KeranjangAdapter.KeranjangViewHolder>() {

    inner class KeranjangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgFoto: ImageView = itemView.findViewById(R.id.imgFotoKeranjang)
        private val tvKategori: TextView = itemView.findViewById(R.id.tvKategoriKeranjang)
        private val tvNama: TextView = itemView.findViewById(R.id.tvNamaKeranjang)
        private val tvKuantitas: TextView = itemView.findViewById(R.id.tvKuantitasKeranjang)
        private val tvHarga: TextView = itemView.findViewById(R.id.tvHargaKeranjang)

        fun bind(item: ItemKeranjang) {
            tvKategori.text = item.produk.kategori
            tvNama.text = item.produk.nama
            tvKuantitas.text = "Quantity: ${item.kuantitas}"

            val totalHargaItem = item.produk.harga * item.kuantitas
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            tvHarga.text = formatRupiah.format(totalHargaItem).replace("Rp", "Rp ")

            if (!item.produk.foto.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(item.produk.foto)
                    .into(imgFoto)
            } else {
                // Jika URL foto kosong dari database, pasang gambar default bawaan Android
                imgFoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeranjangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_keranjang, parent, false)
        return KeranjangViewHolder(view)
    }

    override fun onBindViewHolder(holder: KeranjangViewHolder, position: Int) {
        holder.bind(listKeranjang[position])
    }

    override fun getItemCount(): Int = listKeranjang.size

    fun updateData(newList: List<ItemKeranjang>) {
        listKeranjang = newList
        notifyDataSetChanged()
    }
}