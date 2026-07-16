package com.example.sistemkasir.ui.admin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sistemkasir.R
import com.google.firebase.firestore.FirebaseFirestore



class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var productName: EditText
    private lateinit var productPrice: EditText
    private lateinit var productCategory: Spinner
    private lateinit var productDesc: EditText
    private lateinit var productStock: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_admin)

        productName = findViewById<EditText>(R.id.productName)
        productPrice = findViewById<EditText>(R.id.productPrice)
        productCategory = findViewById<Spinner>(R.id.productCategory)
        productDesc = findViewById<EditText>(R.id.productDesc)
        productStock = findViewById<EditText>(R.id.productStock)

        findViewById<Button>(R.id.validButton).setOnClickListener {
            val name = productName.text.toString()
            val price= productPrice.text.toString()
            val category = productCategory.selectedItem.toString()
            val desc = productDesc.text.toString()
            val stock = productStock.text.toString()

            if (name.isNotEmpty() && price.isNotEmpty() && category.isNotEmpty() && desc.isNotEmpty() && stock.isNotEmpty()){
                val hargaAngka = price.toDoubleOrNull() ?: 0.0
                val jumlahStock = stock.toIntOrNull()

                val dataKirim = hashMapOf(
                    "nama" to name,
                    "harga" to hargaAngka,
                    "kategori" to category,
                    "deskripsi" to desc,
                    "stok" to jumlahStock,
                    "foto" to "" // Dikosongkan sementara foto
                )

                FirebaseFirestore.getInstance().collection("Produk")
                    .document().set(dataKirim)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Produk berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                        productName.text.clear()
                        productPrice.text.clear()
                        productDesc.text.clear()
                        productStock.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_LONG).show()
                    }

            }else {
                Toast.makeText(this, "Harap isi semua kolom!", Toast.LENGTH_SHORT).show()
            }
        }

    }
}