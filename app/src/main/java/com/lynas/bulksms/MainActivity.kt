package com.lynas.bulksms

import android.Manifest
import android.os.Bundle
import android.provider.Contacts
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.coroutines.*
import java.net.URL
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    lateinit var buttonSend: Button
    lateinit var etSMSMessage: EditText
    lateinit var etNumberUrl: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonSend = findViewById(R.id.button)
        etSMSMessage = findViewById(R.id.editTextTextMultiLine)
        etNumberUrl = findViewById(R.id.editTextTextNumberUrl)
        val smsManager: SmsManager = SmsManager.getDefault()

        buttonSend.setOnClickListener {
            Toast.makeText(this, "Sending SMS", LENGTH_SHORT).show()
            val message = etSMSMessage.text.split("\n")
            val messageArrayList = arrayListOf<String>()
            messageArrayList.addAll(message)

            println(message)
            runBlocking {
                withContext(Dispatchers.IO) {
                    val numberArray = send().await()
                    println(numberArray.size)
                    for (number in numberArray) {
                        if (number.trim().isNotEmpty()) {
                            println("Sending sms to $number")
                            smsManager.sendMultipartTextMessage(number.trim(), null, messageArrayList, null, null)
                            delay(20000L)
                        }
                    }
                }

            }
        }




        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.SEND_SMS
            )
        ) {
            Toast.makeText(this, "SMS permission available", LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1)
        }


    }

    fun send() = GlobalScope.async {
        val url = etNumberUrl.text.toString()
        val (_, _, result) = url.httpGet().responseString()
        when (result) {
            is Result.Failure -> {
                throw RuntimeException("")
            }
            is Result.Success -> {
                val data: String = result.get()
                println(data.split("\n").size)
                val numberArray = data.split("\n")
                return@async numberArray
            }
        }

    }


}