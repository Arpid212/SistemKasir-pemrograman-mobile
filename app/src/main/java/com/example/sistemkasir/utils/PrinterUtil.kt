package com.example.sistemkasir.utils

import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothAdapter
import java.io.OutputStream
import java.util.UUID

object PrinterUtil {
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    fun connectBluetooth(macAddress: String): Boolean{
        return try{
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val device =  adapter.getRemoteDevice(macAddress)
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream

            true
        } catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    fun print(teksStruk: String): Boolean{
        return try {
            if(outputStream!=null){
                outputStream?.write(teksStruk.toByteArray())
                true
            }else{
                false
            }
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    fun putusKoneksi(): Boolean{
        return try{
            outputStream?.close()
            bluetoothSocket?.close()
            true
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }
}