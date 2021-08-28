package com.example.serverbluetoothvpmn.graphics

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serverbluetoothvpmn.MainActivity
import com.example.serverbluetoothvpmn.R
import com.example.serverbluetoothvpmn.adapter.DeviceAdapter
import com.example.serverbluetoothvpmn.connectivity.routing.Route

class SettingsFragment : Fragment() {
    // Riferimenti agli oggetti grafici del fragment
    lateinit var sendButton:Button
    lateinit var switch:Switch
    lateinit var main: MainActivity
    lateinit var recyclerViewBonded:RecyclerView
    lateinit var recAdapter:DeviceAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inizializzazione oggetti grafici

        val view= inflater.inflate(R.layout.fragment_settings, container, false)
        sendButton=view.findViewById(R.id.send)
        switch=view.findViewById(R.id.switch2)
        main=activity as MainActivity

        recyclerViewBonded=view.findViewById(R.id.recyclerBonded)
        recAdapter= DeviceAdapter(main.networkMachine.routing.routeList)
        recyclerViewBonded.adapter=recAdapter
        recyclerViewBonded.layoutManager= LinearLayoutManager(view.context)


        // Al click del sendButton cambio il fragment e salvo lo stato passando il mac del dispositivo connesso
        sendButton.setOnClickListener {
            main.messageFragment()
        }

        // Se il bluetooth `e attivo ne recupero i dispositivi accoppiati
        if(main.bluetoothAdapter?.isEnabled==true){
            setSwitch(true)
        }

        // Gestione del cambiamento di stato del bluetooth
        switch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                main.intentSenderLauncher.launch(enableBtIntent)
            }else{
                main.disconnect()
                main.bluetoothAdapter?.disable()
                main.networkMachine.routing.routeList.clear()
                recAdapter.notifyDataSetChanged()
            }
        }

        return view
    }

    fun setSwitch(value: Boolean){
        switch.setChecked(value)
    }

    fun addDeviceConnected(){
        recAdapter.notifyItemInserted(main.networkMachine.routing.routeList.lastIndex)
    }

    fun removeDeviceConnected(){
        recAdapter.notifyDataSetChanged()
    }
}