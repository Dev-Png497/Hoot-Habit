package com.hoothabit.app.data.db

import androidx.room.TypeConverter
import com.hoothabit.app.data.db.entity.DayState
import com.hoothabit.app.data.db.entity.JourneyStatus
import com.hoothabit.app.data.db.entity.SessionSource
import java.time.Instant
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromEpochMilli(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun toEpochMilli(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun fromDayState(value: String?): DayState? = value?.let { DayState.valueOf(it) }

    @TypeConverter
    fun toDayState(state: DayState?): String? = state?.name

    @TypeConverter
    fun fromJourneyStatus(value: String?): JourneyStatus? = value?.let { JourneyStatus.valueOf(it) }

    @TypeConverter
    fun toJourneyStatus(status: JourneyStatus?): String? = status?.name

    @TypeConverter
    fun fromSessionSource(value: String?): SessionSource? = value?.let { SessionSource.valueOf(it) }

    @TypeConverter
    fun toSessionSource(source: SessionSource?): String? = source?.name
}
