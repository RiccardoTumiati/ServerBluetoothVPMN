package com.example.serverbluetoothvpmn.connectivity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.serverbluetoothvpmn.StatusCode
import com.example.serverbluetoothvpmn.connectivity.routing.Packet
import com.example.serverbluetoothvpmn.connectivity.routing.Route
import java.io.IOException
import java.util.UUID

class ServerMesh(val bluetoothAdapter:BluetoothAdapter,val handler: Handler,val machine:NetworkMachine):Thread(){
    val connected=mutableListOf<BluetoothConnection>()
    val mUUID=UUID.fromString("fe964a9c-184c-11e6-b6ba-3e1d05defe78")
    val mNAME="VPMN"
    var keepAlive=true

    private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(mNAME, mUUID)
    }

    init{
        machine.server=this
    }

    override fun run(){
        while (keepAlive) {
            val socket: BluetoothSocket? = try {
                mmServerSocket?.accept()
            } catch (e: IOException) {
                Log.e("ServerMesh", "Socket's accept() method failed", e)
                keepAlive = false
                null
            }
            socket?.also {
                Log.d("ServerMesh","Device connected: "+it.remoteDevice.name+"_"+it.remoteDevice.address)
                // Aggiungi il dispositivo connesso alla lista
                connected.add(BluetoothConnection(it,handler,machine))
                connected.lastOrNull()?.start()

                Log.d("ServerMesh","Number of devices connected: "+connected.size)
                //mmServerSocket?.close()
            }
        }
    }

    fun sendPacket(p: Packet,macHop:String){
        val connectionX=connected.find{
            it.mmSocket.remoteDevice.address.equals(macHop)
        }
        connectionX?.sendPacket(p)
    }

    fun removeConnection(socket:BluetoothSocket){
        connected.removeAll {
            it.mmSocket==socket
        }
    }

    fun disconnect(){
        for (item in connected)
            item.disconnect()

        try {
            mmServerSocket?.close()
        } catch (e: IOException) {
            Log.e("ServerMesh", "Could not close the connect socket", e)
        }
    }

}