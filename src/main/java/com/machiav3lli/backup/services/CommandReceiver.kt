package com.machiav3lli.backup.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.backup.OABX

class CommandReceiver : //TODO hg42 how to maintain security?
//TODO machiav3lli by making the receiver only internally accessible (not exported)
//TODO hg42 but it's one of the purposes to be remotely controllable from other apps like Tasker
//TODO hg42 no big prob for now: cancel, starting or changing schedule isn't very critical
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        OABX.command(intent)
    }
}