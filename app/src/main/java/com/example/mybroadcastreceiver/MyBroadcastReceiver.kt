package com.example.mybroadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.lang.StringBuilder
import java.util.*

class MyBroadcastReceiver : BroadcastReceiver() {

    private var displayOtp: ((String) -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {
        if (isSmsAction(intent)) {
            val message = getMessageFromSms(intent)
            val otp = getOtpFromMessage(message)
            otp?.let { displayOtp?.invoke(otp) }
        }
    }

    fun callDisplayOtp(displayOtp: (String) -> Unit) {
        this.displayOtp = displayOtp
    }

    private fun isSmsAction(intent: Intent) = intent.action == SMS_ACTION

    private fun getOtpFromMessage(message: String?): String? = message

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getMessageFromSms(intent: Intent?): String? {
        return intent?.extras?.let { bundle ->

            val format = bundle.getString(FORMAT)
            val pdus = bundle.get(PDUS) as Array<*>

            val smsMessage = arrayOfNulls<SmsMessage>(pdus.size)
            for (i in pdus.indices) {
                smsMessage[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray, format)
            }

            smsMessage[0]?.messageBody
        }
    }

    companion object {
        private const val SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED"
        private const val PDUS = "pdus"
        private const val FORMAT = "format"
    }
}