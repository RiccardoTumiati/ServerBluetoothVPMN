package com.example.serverbluetoothvpmn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serverbluetoothvpmn.R
import com.example.serverbluetoothvpmn.connectivity.routing.Route

class DeviceAdapter(val deviceList:MutableList<Route>): RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

        // Describes an item view and its place within the RecyclerView
        class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val deviceName: TextView = itemView.findViewById(R.id.deviceName)
            private val deviceMAC: TextView = itemView.findViewById(R.id.deviceMAC)
            private val deviceIP: TextView = itemView.findViewById(R.id.deviceIP)

            fun bind(device: Route) {
                deviceName.text = device.nomeDest
                deviceMAC.text=device.macDest
                deviceIP.text=device.ipDest
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
            return DeviceViewHolder(view)
        }

        override fun getItemCount(): Int {
            return deviceList.size
        }

        override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
            holder.bind(deviceList.get(position))
        }


}