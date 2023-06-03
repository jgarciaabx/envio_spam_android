package com.lobitos.mensajespamandroid

import android.content.Context
import androidx.lifecycle.lifecycleScope
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URISyntaxException

class SocketHelper(private val context: Context) {

    var isConectado = false
    private var socket: Socket? = null
    private var options: IO.Options? = null

    fun startSocket() {
        GlobalScope.launch(Dispatchers.IO) {
            connect()
        }
    }

    init {
        init() // inicializa por primera vez el socket
    }

    fun connect() {
        if (isConectado) {
            try {
                disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (socket != null && options != null) {
            init() // graba nuevamente los parámetros de cabecera..
            socket!!.connect()
        }
    }

    fun disconnect() {
        if (socket != null && options != null) {
            socket!!.disconnect()
            isConectado = false
        }
    }

    fun init() {
        try {
            options = IO.Options()
            options!!.transports = arrayOf("websocket")
            val URL = "https://08c5-38-25-25-114.ngrok-free.app" // Reemplaza "your-server-url" con la URL del servidor
            socket = IO.socket(URL, options)
            socket!!.on(Socket.EVENT_CONNECT) {
                isConectado = true
                println("Socket connected")
            }
            socket!!.on(Socket.EVENT_DISCONNECT) {
                isConectado = false
                println("Socket disconnected")
            }
            socket!!.on("message") { args ->
                val message = args[0] as JSONObject
                val phone = message.getString("phone")
                val messageEnviado = message.getString("message")
                val dataAuxiliar1 = message.getString("dataAuxiliar1")
                val dataAuxiliar2 = message.getString("dataAuxiliar2")
                println("Received message:")
                println("Phone----------------: $phone")
                println("Message----------------: $messageEnviado")
                println("Data Auxiliar 1 ----------------: $dataAuxiliar1")
                println("Data Auxiliar 2 ------------------: $dataAuxiliar2")
//                mensajeEntrante(phone, messageEnviado)
            }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }
//
//    suspend fun enviarMensaje(phoneNumber: String, mensaje: String) {
//        val mainActivity = context as MainActivity
//        mainActivity.enviarMensaje(phoneNumber, mensaje)
//    }
//
//    fun mensajeEntrante(phoneNumber: String, mensaje: String) {
//        GlobalScope.launch(Dispatchers.IO) {
//            enviarMensaje(phoneNumber, mensaje)
//        }
//    }
}