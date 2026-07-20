package com.example.sistemkasir.ui.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sistemkasir.databinding.ItemProdukBinding
import com.example.sistemkasir.model.Produk
import java.text.NumberFormat
import java.util.Locale


class ProdukAdapter(
    private var daftar: List<Pair<String, Produk>>,
    private val onEdit: (String, Produk) -> Unit,
    private val onDelete: (String, Produk) -> Unit
) : RecyclerView.Adapter<ProdukAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemProdukBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProdukBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (docId, produk) = daftar[position]
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        holder.binding.tvNama.text = produk.nama
        holder.binding.tvHarga.text = format.format(produk.harga)
        holder.binding.tvStok.text = "Stok: ${produk.stok}"

        Glide.with(holder.itemView)
            .load(produk.foto.ifEmpty { null })
            .into(holder.binding.ivFoto)

        holder.binding.btnEdit.setOnClickListener { onEdit(docId, produk) }
        holder.binding.btnHapus.setOnClickListener { onDelete(docId, produk) }
    }

    override fun getItemCount() = daftar.size

    fun updateData(baru: List<Pair<String, Produk>>) {
        daftar = baru
        notifyDataSetChanged()
    }
}