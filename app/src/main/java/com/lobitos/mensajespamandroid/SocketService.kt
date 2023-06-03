package com.lobitos.mensajespamandroid

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import android.widget.Toast
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URISyntaxException

class SocketService : Service() {

    private var socket: Socket? = null

    override fun onCreate() {
        super.onCreate()
        startSocketConnection()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSocketConnection()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun startSocketConnection() {
        try {
            val options = IO.Options()
            options.forceNew = true
            options.reconnection = true
            socket = IO.socket("https://08c5-38-25-25-114.ngrok-free.app", options)

            socket!!.on(Socket.EVENT_CONNECT, onConnect)
            socket!!.on(Socket.EVENT_DISCONNECT, onDisconnect)
            socket!!.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
//            socket!!.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)

            socket!!.on("message") { args ->
                val message = args[0] as JSONObject
                val phone = message.getString("phone")
                val messageEnviado = message.getString("message")
                val dataAuxiliar1 = message.getString("dataAuxiliar1")
                val dataAuxiliar2 = message.getString("dataAuxiliar2")

                println("--------------- 2 Received message:")
                println("------------ 2 Phone: $phone")
                println("-------------------- 2 Message: $messageEnviado")
                println("----------------- 2 Data Auxiliar 1: $dataAuxiliar1")
                println("----------------------- 2 Data Auxiliar 2: $dataAuxiliar2")

                enviarMensaje(phone, messageEnviado)
            }

            socket!!.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    private fun stopSocketConnection() {
        socket?.disconnect()
        socket?.off(Socket.EVENT_CONNECT, onConnect)
        socket?.off(Socket.EVENT_DISCONNECT, onDisconnect)
        socket?.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
//        socket?.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError)
    }

    private val onConnect = Emitter.Listener {
        println("Socket connected")
    }

    private val onDisconnect = Emitter.Listener {
        println("Socket disconnected")
    }

    private val onConnectError = Emitter.Listener {
        println("Socket connection error")
    }

    private fun enviarMensaje(phone: String, message: String) {
        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            applicationContext.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
        smsManager.sendTextMessage(phone, null, message, null, null)
        showToast("Mensaje enviado a $phone")
    }

    private fun showToast(message: String) {

        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}
