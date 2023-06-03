package com.lobitos.mensajespamandroid

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var socketServiceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val botonEnviar: Button = findViewById(R.id.button)
        val telefono : EditText = findViewById(R.id.edtTelefono)
        val mensaje  : EditText = findViewById(R.id.edtTexto)
        // Verificar y solicitar los permisos necesarios
        if (!hasSmsPermission()) {
            requestSmsPermission()
        }

        // Iniciar el servicio
        socketServiceIntent = Intent(this, SocketService::class.java)
        startService(socketServiceIntent)


        botonEnviar.setOnClickListener {
            val phoneNumber = telefono.text.toString()
            val message = mensaje.text.toString()
            try {
                if(phoneNumber != "" && message !=""){
                    lifecycleScope.launch(Dispatchers.IO) {
                        // Enviar mensaje
                        enviarMensaje(phoneNumber, message)
                    }
                }else{
                    Toast.makeText(applicationContext, "Ingrese un número de teléfono y un mensaje", Toast.LENGTH_SHORT).show()
                }
            }catch (e: Exception){
                Toast.makeText(applicationContext, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show()
            }



        }


    }


    suspend fun enviarMensaje(phoneNumber: String, mensaje : String) {

        // Enviar mensaje

        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            applicationContext.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
        smsManager.sendTextMessage(phoneNumber, null, mensaje, null, null)

        withContext(Dispatchers.Main) {
            // Actualizar UI
            Toast.makeText(applicationContext, "Enviando Mensaje !!", Toast.LENGTH_SHORT).show()
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        // Detener el servicio
        stopService(socketServiceIntent)
    }

    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.SEND_SMS),
            SMS_PERMISSION_REQUEST_CODE
        )
    }

    companion object {
        private const val SMS_PERMISSION_REQUEST_CODE = 123
    }
}