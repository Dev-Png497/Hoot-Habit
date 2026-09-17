package com.hoothabit.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.hoothabit.app.HootHabitApp
import com.hoothabit.app.data.db.entity.DayState
import com.hoothabit.app.domain.HabitDayUtils
import com.hoothabit.app.domain.StreakCalculator
import com.hoothabit.app.ui.MainActivity
import java.time.temporal.ChronoUnit

/**
 * A focused home-screen widget (spec #65-69) — since only one habit is
 * active at a time, it stays simple: streak, day-of-21, and today's status.
 */
class HootWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as HootHabitApp
        val habit = app.repository.getActiveHabit()

        val backgroundColor = ColorProvider(day = Color(0xFF0B0B0D), night = Color(0xFF0B0B0D))
        val accentColor = ColorProvider(day = Color(0xFF5AC8FA), night = Color(0xFF5AC8FA))
        val textColor = ColorProvider(day = Color(0xFFF2F2F5), night = Color(0xFFF2F2F5))
        val secondaryColor = ColorProvider(day = Color(0xFFA0A0A8), night = Color(0xFFA0A0A8))

        var streakCurrent = 0
        var dayNumber = 1
        var isDone = false
        var habitName = ""

        if (habit != null) {
            val today = HabitDayUtils.currentHabitDate(habit.dayCutoffHour)
            val records = app.repository.getAllRecords(habit.id)
            val streak = StreakCalculator.calculate(habit.startDate, today, records)
            streakCurrent = streak.currentStreak
            dayNumber = ChronoUnit.DAYS.between(habit.startDate, today).toInt() + 1
            isDone = records.firstOrNull { it.habitDate == today }?.state == DayState.COMPLETED
            habitName = habit.name
        }

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                if (habit == null) {
                    Text("Hoot Habit", style = TextStyle(color = textColor, fontWeight = FontWeight.Bold))
                    Spacer(GlanceModifier.height(4.dp))
                    Text("Start a habit to see it here.", style = TextStyle(color = secondaryColor))
                } else {
                    Text(habitName, style = TextStyle(color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                    Spacer(GlanceModifier.height(6.dp))
                    Text(
                        if (isDone) "✓ DONE" else "🔥 $streakCurrent day streak",
                        style = TextStyle(color = accentColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        if (dayNumber <= 21) "Day $dayNumber / 21" else "Day $dayNumber",
                        style = TextStyle(color = secondaryColor, fontSize = 13.sp)
                    )
                }
            }
        }
    }
}
