package com.rasti.selaraspos.adapters

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rasti.selaraspos.R

class AdapterPrinter(
    private val printerList: List<BluetoothDevice>,
    private val onItemClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<AdapterPrinter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaPrinter: TextView = itemView.findViewById(R.id.tvNamaPrinter)
        val tvAlamatPrinter: TextView = itemView.findViewById(R.id.tvAlamatPrinter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_printer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val printer = printerList[position]
        holder.tvNamaPrinter.text = printer.name ?: "Perangkat Bluetooth"
        holder.tvAlamatPrinter.text = printer.address

        holder.itemView.setOnClickListener {
            onItemClick(printer)
        }
    }

    override fun getItemCount(): Int = printerList.size
}