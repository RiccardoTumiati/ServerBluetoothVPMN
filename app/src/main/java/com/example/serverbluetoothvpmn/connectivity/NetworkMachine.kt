package com.example.serverbluetoothvpmn.connectivity

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import com.example.serverbluetoothvpmn.StatusCode
import com.example.serverbluetoothvpmn.connectivity.routing.Packet
import com.example.serverbluetoothvpmn.connectivity.routing.Route
import com.example.serverbluetoothvpmn.connectivity.routing.RoutingTable

class NetworkMachine(val handler: Handler) {
    val routing=RoutingTable()
    val poolDhcp= mutableListOf<String>()

    var server:ServerMesh?=null
    set(value){ field=value }

    init{
        // Inseriamo noi stessi nella tabella di routing
        routing.addRoute(Route("#","192.168.1.1","#","#"))
        // Riempie il pool dhcp
        var ipTemp=2
        while(ipTemp<255){
            poolDhcp.add("192.168.1."+ipTemp)
            ipTemp++
        }
    }

    fun forward(p: Packet,previousMac: String){
        val hop=routing.searchNextHop(p)
        when(hop){
            null->{
                if(p.destinazione.equals("disconnected")) {
                    routing.removeRouteByMAC(p.contenuto)
                    handler.sendMessage(handler.obtainMessage(StatusCode.DEVICE_DISCONNECTED))
                    Log.d("NetworkMachine", routing.routeList.toString())
                } else if(p.destinazione.startsWith("192.168.1.")){
                    val response=Packet("192.168.1.1",p.sorgente,"01010101|DEVICE_UNFINDED_IN_VPMN")
                    if(previousMac.equals("#"))
                        handler.sendMessage(handler.obtainMessage(StatusCode.LOG_MESSAGE,response))
                    else
                        server?.sendPacket(response,previousMac)
                }
                else
                    ClientWeb(this,p).start()
            }
            "#"->{
                if(p.sorgente.equals("new")){
                    // Aggiorna tabella di routing
                    val newIp=getIpAddress()
                    val newName=p.contenuto.takeWhile{
                        it!='+'
                    }
                    val newMac=p.contenuto.takeLastWhile {
                        it!='+'
                    }
                    val newRoute = Route(newName,newIp,newMac,previousMac)
                    routing.addRoute(newRoute)
                    Log.d("NetworkMachine",routing.routeList.toString())

                    // Inoltro risposta dhcp
                    val response=Packet("192.168.1.1","new",newIp)
                    server?.sendPacket(response,previousMac)

                    // Notifica la grafica
                    handler.sendMessage(handler.obtainMessage(StatusCode.DEVICE_CONNECTED))
                } else if(p.contenuto.startsWith("01010101|")) {
                    handler.sendMessage(handler.obtainMessage(StatusCode.LOG_MESSAGE,p))
                } else{
                    handler.sendMessage(handler.obtainMessage(StatusCode.MESSAGE_RECEIVED,p))
                }
            }
            else->server?.sendPacket(p,hop)
        }
    }


    fun getIpAddress():String{
        return poolDhcp.removeFirst()
    }

    fun deviceDisconnected(socket:BluetoothSocket){
        // Riaggiungo indirizzi Ip nuovamente disponibili nella lista
        poolDhcp.add(routing.ipRoute(socket.remoteDevice.address))
        val list = routing.subTree(socket.remoteDevice.address)
        for (item in list){
            poolDhcp.add(item.ipDest)
        }

        // Tolgo dalla lista delle connessioni del server il socket che disconnesso
        server?.removeConnection(socket)

        // Cancello le route associate a quel socket
        routing.removeRouteByMAC(socket.remoteDevice.address)
    }
}