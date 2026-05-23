package com.rasti.selaraspos.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.rasti.selaraspos.R
import com.rasti.selaraspos.model.ModelAkun

class AkunActivity : AppCompatActivity() {

    private lateinit var tvNamaPenggunaProfil: TextView
    private lateinit var tvRoleBadge: TextView
    private lateinit var tvEmailProfil: TextView
    private lateinit var tvInfoNama: TextView
    private lateinit var tvInfoEmail: TextView
    private lateinit var tvInfoRole: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnBack: ImageButton

    private val database = FirebaseDatabase.getInstance()

    private var modelAkun: ModelAkun? = null
    private var idKasir = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_akun)

        // Ambil ID Kasir dari SharedPreferences
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        idKasir = prefs.getString("idKasir", "") ?: ""

        initViews()
        loadProfilAkun()

        btnBack.setOnClickListener { finish() }
        btnLogout.setOnClickListener { konfirmasiLogout() }

        // Catatan: Jika tombol Edit Nama sudah dihapus dari XML,
        // pastikan tidak ada kode btnEditProfil.setOnClickListener di sini.
    }

    private fun initViews() {
        tvNamaPenggunaProfil = findViewById(R.id.tvNamaPenggunaProfil)
        tvRoleBadge = findViewById(R.id.tvRoleBadge)
        tvEmailProfil = findViewById(R.id.tvEmailProfil)
        tvInfoNama = findViewById(R.id.tvInfoNama)
        tvInfoEmail = findViewById(R.id.tvInfoEmail)
        tvInfoRole = findViewById(R.id.tvInfoRole)
        btnLogout = findViewById(R.id.btnLogout)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun loadProfilAkun() {
        if (idKasir.isEmpty()) return

        database.getReference("akun/$idKasir")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val akun = snapshot.getValue(ModelAkun::class.java) ?: return
                    modelAkun = akun

                    tvNamaPenggunaProfil.text = akun.namaPengguna
                    tvRoleBadge.text = akun.role.replaceFirstChar { it.uppercase() }
                    tvEmailProfil.text = akun.email.ifEmpty { "–" }
                    tvInfoNama.text = akun.namaPengguna.ifEmpty { "–" }
                    tvInfoEmail.text = akun.email.ifEmpty { "–" }
                    tvInfoRole.text = akun.role.replaceFirstChar { it.uppercase() }.ifEmpty { "–" }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AkunActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun konfirmasiLogout() {
        AlertDialog.Builder(this)
            .setTitle("Keluar")
            .setMessage("Yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply()
                finishAffinity()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}