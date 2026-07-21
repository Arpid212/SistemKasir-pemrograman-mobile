package com.example.sistemkasir.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sistemkasir.R
import com.example.sistemkasir.model.Produk
import java.text.NumberFormat
import java.util.Locale

class KatalogProdukAdapter(
    private var listProduk: List<Produk>,
    private val onItemClick: (Produk) -> Unit
) : RecyclerView.Adapter<KatalogProdukAdapter.ProdukViewHolder>() {

    inner class ProdukViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgFoto: ImageView = itemView.findViewById(R.id.imgFotoProduk)
        private val tvKategori: TextView = itemView.findViewById(R.id.tvKategoriProduk)
        private val tvNama: TextView = itemView.findViewById(R.id.tvNamaProduk)
        private val tvHarga: TextView = itemView.findViewById(R.id.tvHargaProduk)

        fun bind(produk: Produk) {
            tvNama.text = produk.nama
            tvKategori.text = produk.kategori

            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            tvHarga.text = formatRupiah.format(produk.harga).replace("Rp", "Rp ")

            if (!produk.foto.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(produk.foto)
                    .into(imgFoto)
            } else {
                // Gambar fallback jika URL foto kosong
                imgFoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            itemView.setOnClickListener { onItemClick(produk) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_katalog_produk, parent, false)
        return ProdukViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        holder.bind(listProduk[position])
    }

    override fun getItemCount(): Int = listProduk.size

    fun updateData(newList: List<Produk>) {
        listProduk = newList
        notifyDataSetChanged()
    }
}