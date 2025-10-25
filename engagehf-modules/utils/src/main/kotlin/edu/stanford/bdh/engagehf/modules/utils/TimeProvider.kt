package edu.stanford.bdh.engagehf.modules.utils

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject

class TimeProvider @Inject constructor() {
    fun currentTimeMillis(): Long = nowInstant().toEpochMilli()
    fun nowInstant(): Instant = Instant.now()
    fun nowLocalTime(): LocalTime = LocalTime.now()
    fun nowZonedDateTime(): ZonedDateTime = ZonedDateTime.now()
    fun currentOffset(): ZoneOffset = ZonedDateTime.now().offset
}
