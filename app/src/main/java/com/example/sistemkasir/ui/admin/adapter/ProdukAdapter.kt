package com.example.sistemkasir.ui.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sistemkasir.R
import com.example.sistemkasir.model.Produk

class ProdukAdapter(
    private val listProduk: ArrayList<Produk>,
    private val onEdit: (Produk) -> Unit,
    private val onDelete: (Produk) -> Unit
) : RecyclerView.Adapter<ProdukAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtId: TextView = itemView.findViewById(R.id.txtId)
        val txtNama: TextView = itemView.findViewById(R.id.txtNama)
        val txtHarga: TextView = itemView.findViewById(R.id.txtHarga)
        val txtKategori: TextView = itemView.findViewById(R.id.txtKategori)
        val txtStok: TextView = itemView.findViewById(R.id.txtStok)

        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnHapus: Button = itemView.findViewById(R.id.btnHapus)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk, parent, false)

        return ViewHolder(view)

    }

    override fun getItemCount(): Int {

        return listProduk.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val produk = listProduk[position]

        holder.txtId.text = "ID : ${produk.id}"
        holder.txtNama.text = produk.nama
        holder.txtHarga.text = "Rp ${produk.harga}"
        holder.txtKategori.text = "Kategori : ${produk.kategori}"
        holder.txtStok.text = "Stok : ${produk.stok}"

        holder.btnEdit.setOnClickListener {
            onEdit(produk)
        }

        holder.btnHapus.setOnClickListener {
            onDelete(produk)
        }

    }

}