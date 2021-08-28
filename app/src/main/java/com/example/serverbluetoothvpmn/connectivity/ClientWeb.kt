package com.example.serverbluetoothvpmn.connectivity

import android.util.Log
import com.example.serverbluetoothvpmn.connectivity.routing.Packet
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException

class ClientWeb(val machine: NetworkMachine, val p:Packet):Thread() {
    private lateinit var inFlow:InputStream
    private lateinit var outFlow:OutputStream

    private var mmBufferIn: ByteArray =ByteArray(1024)
    private var mmBufferOut: ByteArray =ByteArray(1024)

    override fun run(){
        val inet:InetAddress
        try{
            inet=InetAddress.getByName(p.destinazione)
        }catch(e:UnknownHostException){
            val p=Packet("192.168.1.1",p.sorgente,"01010101|IP_ADDRESS_NOT_FOUND")
            machine.forward(p,"#")
            return
        }catch(e:SecurityException){
            val tempPacket=Packet("192.168.1.1",p.sorgente,"01010101|SECURITY_PROBLEM")
            machine.forward(tempPacket,"#")
            return
        }

        val socket:Socket
        try{
            socket=Socket(inet,7777)
        }catch(e:IOException){
            val tempPacket=Packet("192.168.1.1",p.sorgente,"01010101|IMPOSSIBLE_CREATE_CONNECTION")
            machine.forward(tempPacket,"#")
            return
        }catch(e:SecurityException){
            val tempPacket=Packet("192.168.1.1",p.sorgente,"01010101|SECURITY_PROBLEM")
            machine.forward(tempPacket,"#")
            return
        }

        inFlow=socket.getInputStream()
        outFlow=socket.getOutputStream()

        sendPacket()
        receivePacket()
    }

    private fun sendPacket(){
        try {
            mmBufferOut=p.contenuto.encodeToByteArray()
            Log.d("ClientMesh","Pacchetto inviato: "+mmBufferOut.decodeToString())
            outFlow.write(mmBufferOut)
        } catch (e: IOException) {
            Log.e("BluetoothConnection", "Error occurred when sending data")
        }
    }

    private fun receivePacket(){
        try{
            inFlow.read(mmBufferIn)
            val response= removeUnusedByte(mmBufferIn).decodeToString()
            val tempPacket=Packet(p.destinazione,p.sorgente,response)
            Log.d("BluetoothConnection","Pacchetto ricevuto: "+p.toString())
            machine.forward(tempPacket,"#")
        }catch(e:IOException){
            Log.e("BluetoothConnection", "Error occurred when reading data")
        }
    }

    fun removeUnusedByte(buffer:ByteArray):ByteArray{
        return buffer.filter{
            it!=0.toByte()
        }.toByteArray()
    }

}