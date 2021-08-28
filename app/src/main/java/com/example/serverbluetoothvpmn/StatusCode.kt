package com.example.serverbluetoothvpmn

class StatusCode {
    companion object{
        const val CONNECTION_ERROR:Int=1
        const val CONNECTION_SUCCESSFUL:Int=2
        const val DEVICE_CONNECTED:Int=3
        const val MESSAGE_RECEIVED:Int=4
        const val DEVICE_DISCONNECTED:Int=5
        const val LOG_MESSAGE:Int=6
    }
}