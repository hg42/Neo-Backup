/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.utils

import android.content.Context
import android.icu.util.Calendar
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.dbs.repository.ScheduleRepository
import com.machiav3lli.backup.preferences.pref_fakeScheduleMin
import com.machiav3lli.backup.preferences.traceSchedule
import com.machiav3lli.backup.tasks.ScheduleWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import org.koin.java.KoinJavaComponent.get
import timber.log.Timber
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.math.max

fun calculateTimeToRun(schedule: Schedule, now: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = schedule.timePlaced

    val limitIncrements = 366 / schedule.interval
    val minTimeFromNow = TimeUnit.MINUTES.toMillis(1)

    val fakeMin = pref_fakeScheduleMin.value
    if (fakeMin > 1) {
        //c[Calendar.HOUR_OF_DAY] = schedule.timeHour
        c[Calendar.MINUTE] = (c[Calendar.MINUTE] / fakeMin + 1) * fakeMin % 60
        c[Calendar.SECOND] = 0
        c[Calendar.MILLISECOND] = 0
        var nIncrements = 0
        repeat(limitIncrements) {
            if (c.timeInMillis > now + minTimeFromNow)
                return@repeat
            c.add(Calendar.MINUTE, fakeMin)
            nIncrements++
        }
        traceSchedule { "[${schedule.id}] added $nIncrements * ${schedule.interval} min" }
    } else if (fakeMin == 1) {
        //c[Calendar.HOUR_OF_DAY] = schedule.timeHour
        c[Calendar.MINUTE] = schedule.timeHour
        c[Calendar.SECOND] = schedule.timeMinute
        c[Calendar.MILLISECOND] = 0
        var nIncrements = 0
        repeat(limitIncrements) {
            if (c.timeInMillis > now + minTimeFromNow)
                return@repeat
            c.add(Calendar.HOUR, schedule.interval)
            nIncrements++
        }
        traceSchedule { "[${schedule.id}] added $nIncrements * ${schedule.interval} min" }
    } else {
        c[Calendar.HOUR_OF_DAY] = schedule.timeHour
        c[Calendar.MINUTE] = schedule.timeMinute
        c[Calendar.SECOND] = 0
        c[Calendar.MILLISECOND] = 0
        var nIncrements = 0
        repeat(limitIncrements) {
            if (c.timeInMillis > now + minTimeFromNow)
                return@repeat
            c.add(Calendar.DAY_OF_MONTH, schedule.interval)
            nIncrements++
        }
        traceSchedule { "[${schedule.id}] added $nIncrements * ${schedule.interval} days" }
    }

    traceSchedule {
        "[${schedule.id}] calculateTimeToRun: next: ${
            ISO_DATE_TIME_FORMAT.format(c.timeInMillis)
        } now: ${
            ISO_DATE_TIME_FORMAT.format(now)
        } placed: ${
            ISO_DATE_TIME_FORMAT.format(schedule.timePlaced)
        } interval: ${
            schedule.interval
        }"
    }
    return c.timeInMillis
}

val updateInterval = 1_000L
val useSeconds = updateInterval < 60_000

fun calcTimeLeft(schedule: Schedule): Pair<String, String> {
    var absTime = ""
    var relTime = ""
    val now = SystemUtils.now
    val at = calculateTimeToRun(schedule, now)
    absTime = ISO_DATE_TIME_FORMAT_MIN.format(at)
    val timeDiff = max(at - now, 0)
    val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff).toInt()
    val days = TimeUnit.MILLISECONDS.toDays(timeDiff).toInt()
    if (days != 0) {
        relTime +=
            "${OABX.context.resources.getQuantityString(R.plurals.days_left, days, days)} + "
    }
    val hours = TimeUnit.MILLISECONDS.toHours(timeDiff).toInt() % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff).toInt() % 60
    if (useSeconds && remainingMinutes < 10) {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff).toInt() % 60
        relTime += LocalTime.of(hours, minutes, seconds).toString()
    } else {
        relTime += LocalTime.of(hours, minutes).toString()
    }
    return Pair(absTime, relTime)
}

fun timeLeft(
    schedule: Schedule,
): StateFlow<Pair<String, String>> = MutableStateFlow(calcTimeLeft(schedule))
    .debounce(updateInterval)
    .stateIn(
        scope = CoroutineScope(Dispatchers.IO),
        started = SharingStarted.Lazily,
        initialValue = calcTimeLeft(schedule)
    )

// TODO clean up fully
fun scheduleNext(context: Context, scheduleId: Long, rescheduleBoolean: Boolean) {
    if (scheduleId >= 0) {
        val scheduleRepo = get<ScheduleRepository>(ScheduleRepository::class.java)
        var schedule = scheduleRepo.getSchedule(scheduleId)

        if (schedule?.enabled == true) {
            val now = SystemUtils.now

            if (rescheduleBoolean) {
                schedule = schedule.copy(timePlaced = now)
                traceSchedule { "[${schedule?.id}] re-scheduling $schedule" }
                scheduleRepo.update(schedule)
            }
            ScheduleWork.schedule(context, schedule)
        }
    } else {
        Timber.e("[$scheduleId] got id from $context")
    }
}

var alarmsHaveBeenScheduled = false

// TODO clean up fully
fun scheduleAlarmsOnce() { // TODO replace with ScheduleWorker.scheduleAll()

    // schedule alarms only once
    // whichever event comes first:
    //   any activity started
    //   after all current schedules are queued
    //   the app is terminated (too early)
    //   on a timeout

    if (alarmsHaveBeenScheduled)
        return
    alarmsHaveBeenScheduled = true
    ScheduleWork.scheduleAll(OABX.context)
}

fun Context.getStartScheduleMessage(schedule: Schedule) = StringBuilder()
    .append(
        "\n${getString(R.string.sched_mode)} ${
            modesToString(
                this,
                modeToModes(schedule.mode)
            )
        }"
    )
    .append(
        "\n${getString(R.string.backup_filters)} ${
            filterToString(
                this,
                schedule.filter
            )
        }"
    )
    .append(
        "\n${getString(R.string.other_filters_options)} ${
            specialFilterToString(
                this,
                schedule.specialFilter
            )
        }"
    )
    // TODO list the CL packages
    .append(
        "\n${getString(R.string.customListTitle)}: ${
            if (schedule.customList.isNotEmpty()) getString(
                R.string.dialogYes
            ) else getString(R.string.dialogNo)
        }"
    )
    // TODO list the BL packages
    .append(
        "\n${getString(R.string.sched_blocklist)}: ${
            if (schedule.blockList.isNotEmpty()) getString(
                R.string.dialogYes
            ) else getString(R.string.dialogNo)
        }"
    )
    .append(
        "\n${getString(R.string.filters_tags)}: ${
            if (schedule.tagsList.isNotEmpty()) schedule.tagsList.joinToString(",")
            else getString(R.string.dialogNo)
        }"
    )
    .toString()