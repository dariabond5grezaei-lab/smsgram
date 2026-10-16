package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receiver declared to fulfill Android Default SMS App contract for WAP_PUSH_DELIVER.
 */
class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // SMS-only system: ignore MMS push
    }
}
