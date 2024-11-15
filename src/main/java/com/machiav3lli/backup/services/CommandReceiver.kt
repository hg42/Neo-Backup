package com.machiav3lli.backup.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.machiav3lli.backup.ACTION_CANCEL
import com.machiav3lli.backup.ACTION_CANCEL_SCHEDULE
import com.machiav3lli.backup.ACTION_CRASH
import com.machiav3lli.backup.ACTION_RESCHEDULE
import com.machiav3lli.backup.ACTION_SCHEDULE
import com.machiav3lli.backup.EXTRA_SCHEDULE_ID
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.dbs.repository.ScheduleRepository
import com.machiav3lli.backup.preferences.traceSchedule
import com.machiav3lli.backup.tasks.ScheduleWork
import com.machiav3lli.backup.utils.SystemUtils
import com.machiav3lli.backup.utils.scheduleNext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class CommandReceiver : //TODO hg42 how to maintain security?
//TODO machiav3lli by making the receiver only internally accessible (not exported)
//TODO hg42 but it's one of the purposes to be remotely controllable from other apps like Tasker
//TODO hg42 no big prob for now: cancel, starting or changing schedule isn't very critical
    BroadcastReceiver(), KoinComponent {
    private val scheduleRepo: ScheduleRepository by inject()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val command = intent.action
        Timber.i("Command: command $command")
        when (command) {
            ACTION_CANCEL          -> {
                val batchName = intent.getStringExtra("name")
                Timber.d("################################################### command intent cancel -------------> name=$batchName")
                OABX.addInfoLogText("$command $batchName")
                OABX.work.cancel(batchName)
            }

            ACTION_SCHEDULE        -> {
                intent.getStringExtra("name")?.let { name ->
                    OABX.addInfoLogText("$command $name")
                    Timber.d("################################################### command intent schedule -------------> name=$name")
                    CoroutineScope(Dispatchers.Default).launch {
                        scheduleRepo.getSchedule(name)?.let { schedule ->
                            ScheduleWork.schedule(context, schedule, true)
                        }
                    }
                }
            }

            ACTION_CANCEL_SCHEDULE -> {
                intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L).takeIf { it != -1L }?.let { id ->
                    Timber.d("################################################### command cancel schedule -------------> id=$id")
                    ScheduleWork.cancel(context, id)
                }
            }

            ACTION_RESCHEDULE      -> { // TODO reconsider when ScheduleWork is fully implemented
                intent.getStringExtra("name")?.let { name ->
                    val now = SystemUtils.now
                    val time = intent.getStringExtra("time")
                    val setTime = time ?: SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(now + 120)
                    OABX.addInfoLogText("$command $name $time -> $setTime")
                    Timber.d("################################################### command intent reschedule -------------> name=$name time=$time -> $setTime")
                    CoroutineScope(Dispatchers.Default).launch {
                        scheduleRepo.getSchedule(name)?.let { schedule ->
                            val (hour, minute) = setTime.split(":").map { it.toInt() }
                            traceSchedule { "[${schedule.id}] command receiver -> re-schedule to hour=$hour minute=$minute" }
                            val newSched = schedule.copy(
                                timeHour = hour,
                                timeMinute = minute,
                            )
                            scheduleRepo.update(newSched)
                            scheduleNext(context, newSched.id, true)
                        }
                    }
                }
            }

            ACTION_CRASH           -> {
                throw Exception("this is a crash via command intent")
            }

            null                   -> {}
            else                   -> {
                OABX.addInfoLogText("Command: command '$command'")
            }
        }
    }
}