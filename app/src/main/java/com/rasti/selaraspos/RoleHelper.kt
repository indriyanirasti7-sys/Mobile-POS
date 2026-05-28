package com.rasti.selaraspos

import android.content.Context
import android.widget.Toast

/**
 * RoleHelper
 * Utility object untuk mengecek role pengguna yang sedang login.
 * Role disimpan di SharedPreferences saat proses login.
 *
 * Penggunaan:
 *   if (RoleHelper.isAdmin(this)) { ... }
 *   RoleHelper.cekAkses(this) { ... }  // langsung jalankan jika admin
 */
object RoleHelper {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_ROLE = "role"

    /** Kembalikan true jika role adalah "admin" */
    fun isAdmin(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ROLE, "kasir") == "admin"
    }

    /** Kembalikan role saat ini ("admin" atau "kasir") */
    fun getRole(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ROLE, "kasir") ?: "kasir"
    }

    /** Kembalikan nama user saat ini */
    fun getNama(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("nama", "Pengguna") ?: "Pengguna"
    }

    /** Kembalikan UID user saat ini */
    fun getUid(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("uid", "") ?: ""
    }

    /**
     * Jalankan [aksi] hanya jika user adalah admin.
     * Jika bukan admin, tampilkan Toast peringatan.
     */
    fun cekAkses(context: Context, aksi: () -> Unit) {
        if (isAdmin(context)) {
            aksi()
        } else {
            Toast.makeText(
                context,
                "⛔ Akses ditolak. Fitur ini hanya untuk Admin.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}