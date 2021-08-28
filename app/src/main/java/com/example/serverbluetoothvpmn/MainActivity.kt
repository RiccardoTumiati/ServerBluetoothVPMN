package com.example.serverbluetoothvpmn

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.example.serverbluetoothvpmn.connectivity.NetworkMachine
import com.example.serverbluetoothvpmn.connectivity.ServerMesh
import com.example.serverbluetoothvpmn.connectivity.routing.Packet
import com.example.serverbluetoothvpmn.graphics.DenyFragment
import com.example.serverbluetoothvpmn.graphics.MessageFragment
import com.example.serverbluetoothvpmn.graphics.SettingsFragment

class MainActivity : AppCompatActivity() {
    val setFrag: SettingsFragment = SettingsFragment()
    val mesFrag: MessageFragment = MessageFragment()
    val denyFrag: DenyFragment = DenyFragment()
    val fragmentManager=supportFragmentManager

    val bluetoothAdapter: BluetoothAdapter?= BluetoothAdapter.getDefaultAdapter()

    val handler: Handler = UIHandler()
    lateinit var server: ServerMesh
    val networkMachine=NetworkMachine(handler)

    lateinit var intentSenderLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted)
                denyFragment()
        }

        if(bluetoothAdapter?.isEnabled==true){
            connect()
        }

        intentSenderLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode== RESULT_CANCELED){
                bluetoothAdapter?.disable()
                setFrag.setSwitch(false)
            }else{
                connect()
            }
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }


        fragmentManager.commit {
            add(R.id.container,setFrag)
            setReorderingAllowed(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }


    fun messageFragment(){
        fragmentManager.commit {
            replace(R.id.container,mesFrag)
            setReorderingAllowed(true)
        }
    }

    fun settingsFragment(){
        fragmentManager.commit {
            replace(R.id.container,setFrag)
            setReorderingAllowed(true)
        }
    }

    fun denyFragment(){
        fragmentManager.commit {
            replace(R.id.container,denyFrag)
            setReorderingAllowed(true)
        }
    }


    fun connect(){
        instantiateServer()
    }

    fun disconnect(){
        server.disconnect()
    }

    private fun instantiateServer(){
        if(bluetoothAdapter!=null){
            server= ServerMesh(bluetoothAdapter,handler,networkMachine)
            server.start()
        }
    }

    fun hideKeyboard(){
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if( inputMethodManager.isAcceptingText())
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()?.getWindowToken(), 0)
    }

    inner class UIHandler: Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            when(msg.what){
                StatusCode.DEVICE_CONNECTED->{
                    setFrag.addDeviceConnected()
                }
                StatusCode.DEVICE_DISCONNECTED->{
                    setFrag.removeDeviceConnected()
                }
                StatusCode.MESSAGE_RECEIVED->{
                    mesFrag.setLastMessageFrom((msg.obj as Packet).sorgente)
                    mesFrag.setLastMessageText((msg.obj as Packet).contenuto)
                }
                StatusCode.LOG_MESSAGE->{
                    val content=(msg.obj as Packet).contenuto
                    mesFrag.setLog(content.subSequence(9,content.lastIndex+1).toString())
                }
            }
            super.handleMessage(msg)
        }
    }



}