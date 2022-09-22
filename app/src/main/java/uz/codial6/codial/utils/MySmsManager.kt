package uz.codial6.codial.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast

class MySmsManager(val activity: Activity) {
    lateinit var sentPendingIntent: PendingIntent
    lateinit var deliveredPendingIntent: PendingIntent
    lateinit var smsSentReceiever: BroadcastReceiver
    lateinit var smsDeliveredReceiever: BroadcastReceiver

    var SENT = "SMS_SENT"
    var DELIVERED = "SMS_DELIVERED"

    init {
        sentPendingIntent =
            PendingIntent.getBroadcast(activity, 0, Intent(SENT), PendingIntent.FLAG_MUTABLE)
        deliveredPendingIntent =
            PendingIntent.getBroadcast(activity, 0, Intent(DELIVERED), PendingIntent.FLAG_MUTABLE)
    }

    fun sentSmsMessage(
        phoneNumberToSendSms: String,
        userNameSurname: String,
        userPhoneNumber: String,
        selectedCourseName: String,
    ) {
        val message =
            "Salom. Men $userNameSurname $selectedCourseName kursi yangi guruhida oʻqimoqchiman. $userPhoneNumber \n\nSms Codial Test ilovasi orqali joʻnatildi."

        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }

        val longMessage = smsManager.divideMessage(message)
        val sentIntents = ArrayList<PendingIntent>()
        val deliveredIntents = ArrayList<PendingIntent>()
        sentIntents.add(sentPendingIntent)
        deliveredIntents.add(deliveredPendingIntent)

        smsManager.sendMultipartTextMessage(phoneNumberToSendSms, null, longMessage, null, null)
    }

    fun registerReceiver() {
        smsSentReceiever = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Toast.makeText(activity,
                            "Sms sent successfully!",
                            Toast.LENGTH_SHORT).show()
                    }

                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                        Toast.makeText(activity, "Generic failure!", Toast.LENGTH_SHORT)
                            .show()
                    }

                    SmsManager.RESULT_ERROR_NO_SERVICE -> {
                        Toast.makeText(activity, "No servise!", Toast.LENGTH_SHORT).show()
                    }

                    SmsManager.RESULT_ERROR_NULL_PDU -> {
                        Toast.makeText(activity, "Null PDU!", Toast.LENGTH_SHORT).show()
                    }

                    SmsManager.RESULT_ERROR_RADIO_OFF -> {
                        Toast.makeText(activity, "Radio off!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        smsDeliveredReceiever = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Toast.makeText(activity, "Sms delivered!", Toast.LENGTH_SHORT)
                            .show()
                    }

                    Activity.RESULT_CANCELED -> {
                        Toast.makeText(activity, "Sms not delivered!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        activity.registerReceiver(smsSentReceiever, IntentFilter(SENT))
        activity.registerReceiver(smsDeliveredReceiever, IntentFilter(DELIVERED))
    }

    fun unregisterReceiver() {
        activity.unregisterReceiver(smsSentReceiever)
        activity.unregisterReceiver(smsDeliveredReceiever)
    }
}
