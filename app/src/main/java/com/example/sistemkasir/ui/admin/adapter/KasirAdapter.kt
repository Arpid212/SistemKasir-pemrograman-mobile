package com.example.sistemkasir.ui.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sistemkasir.databinding.ItemKasirBinding
import com.example.sistemkasir.model.Pengguna


class KasirAdapter(
    private var daftar: List<Pair<String, Pengguna>>,
    private val onEdit: (String, Pengguna) -> Unit,
    private val onDelete: (String, Pengguna) -> Unit
) : RecyclerView.Adapter<KasirAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemKasirBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKasirBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (docId, pengguna) = daftar[position]
        holder.binding.tvNama.text = pengguna.nama
        holder.binding.tvEmail.text = pengguna.email
        holder.binding.tvPin.text = pengguna.pin
        holder.binding.tvAvatar.text = pengguna.nama.firstOrNull()?.uppercase() ?: "?"

        holder.binding.btnEdit.setOnClickListener { onEdit(docId, pengguna) }
        holder.binding.btnHapus.setOnClickListener { onDelete(docId, pengguna) }
    }

    override fun getItemCount() = daftar.size

    fun updateData(baru: List<Pair<String, Pengguna>>) {
        daftar = baru
        notifyDataSetChanged()
    }
}