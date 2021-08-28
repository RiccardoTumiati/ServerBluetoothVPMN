package com.example.serverbluetoothvpmn.connectivity

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import com.example.serverbluetoothvpmn.StatusCode
import com.example.serverbluetoothvpmn.connectivity.routing.Packet
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BluetoothConnection(val mmSocket:BluetoothSocket,val handler:Handler,val machine:NetworkMachine):Thread() {
    private var keepAlive=true
    private val inFlow: InputStream=mmSocket.inputStream
    private val outFlow: OutputStream=mmSocket.outputStream
    private var mmBufferIn: ByteArray =ByteArray(1024)
    private var mmBufferOut: ByteArray =ByteArray(1024)

    override fun run(){
        var firstMessage=true
        while(keepAlive){
            try{
                inFlow.read(mmBufferIn)
                val p= Packet.createPacket(removeUnusedByte(mmBufferIn).decodeToString())
                Log.d("BluetoothConnection","Pacchetto ricevuto: "+p.toString())
                if(firstMessage){
                    p.contenuto=p.contenuto+"+"+mmSocket.remoteDevice.address
                    firstMessage=false
                }
                machine.forward(p,mmSocket.remoteDevice.address)
                mmBufferIn=ByteArray(1024)
            }catch(e:IOException){
                Log.d("BluetoothConnection","Device disconnected: "+mmSocket.remoteDevice.name)
                // Procedura di disconnessione dalla network machine
                machine.deviceDisconnected(mmSocket)
                handler.sendMessage(handler.obtainMessage(StatusCode.DEVICE_DISCONNECTED))
                Log.d("BluetoothConnection","Routing table: "+machine.routing.routeList.toString())

                keepAlive=false
            }
        }
    }

    fun sendPacket(p:Packet){
        try {
            mmBufferOut=p.createBuffer()
            Log.d("ClientMesh","Pacchetto inviato: "+mmBufferOut.decodeToString())
            outFlow.write(mmBufferOut)

            //Pulisco il buffer
            mmBufferOut=ByteArray(1024)
        } catch (e: IOException) {
            Log.e("BluetoothConnection", "Error occurred when sending data")
        }

    }

    fun removeUnusedByte(buffer:ByteArray):ByteArray{
        return buffer.filter{
            it!=0.toByte()
        }.toByteArray()
    }

    fun disconnect(){
        try {
            mmSocket.close()
        } catch (e: IOException) {
            Log.d("BluetoothConnection", "Could not close the connect socket", e)
        }
        keepAlive=false
    }

}