# Hoot Habit

> One habit. Start with 21 days. Keep going.

A native Android app (Kotlin + Jetpack Compose) implementing the Hoot Habit
product spec: a single-habit tracker built around a 21-day starting
foundation, honest streak tracking, Night Watch protection, deep statistics,
and shareable milestones.

## Requirements

- Android Studio (a recent stable release)
- JDK 17 (bundled with modern Android Studio)
- Android SDK Platform 35, minSdk 26 (Android 8.0+)
- Internet access on first sync (downloads Gradle 8.7, AGP 8.5.2, Kotlin
  1.9.24, and all dependencies)

## Getting started

1. `File → Open...` in Android Studio, select the **root** of this
   repository (the folder containing `settings.gradle.kts`, `app/`,
   `gradle/` — not the `app` subfolder itself).
2. Let Gradle sync finish (first sync downloads everything, can take a
   few minutes).
3. Run ▶ on an emulator or device running Android 8.0 (API 26) or newer.

## What's implemented

**Core loop**
- Cinematic onboarding (habit name, timed/simple completion, motivation,
  reminders, custom day-cutoff, summary) that creates a persisted habit
- Today screen: 21-day foundation grid, streak, owl companion, one-tap
  completion, manual duration logging, retroactive "did you miss
  yesterday?" flow, Night Watch streak protection
- Reliable countdown timer as a real foreground service (survives
  backgrounding and process death)
- Journey screen: full history grid, milestones list, archived journeys
- Insights screen: completion %, streaks, weekday consistency, records
- Settings: theme (7 dark palettes + true-black), day cutoff, haptics/
  sound, reminders + notification personality, archive journey

**Milestones & sharing**
- Day 7/14/21/30/50/75/100/150/200/250/365+ milestone celebration screens
  (spec-accurate copy — never claims a habit is "formed" on Day 21)
- Shareable milestone cards in 3 layouts (Companion / Minimal / Stats) and
  3 aspect ratios (Square / Story / Portrait), with privacy toggles for
  what's shown, rendered natively and shared via the system share sheet

**Notifications**
- Two daily reminder slots with 4 notification personalities (Minimal /
  Gentle / Focused / Playful), self-rescheduling exact alarms, automatic
  cancellation once today's habit is completed, and re-arming after reboot

**Widget**
- A home-screen widget (Jetpack Glance) showing streak / completion status
  for the active habit, tapping through into the app

**Data**
- Room database (habits, daily records, timer sessions, Night Watches,
  milestones) + DataStore-backed settings — everything persists locally

## Architecture

Single-module app, manual dependency injection (`HootHabitApp` holds the
repository/prefs singletons; `HootViewModelFactory` wires them into
ViewModels) rather than a DI framework, kept deliberately simple for a
single-module app. Domain logic (streak/stats calculation, milestone and
Night Watch rules, day-cutoff handling) is pure Kotlin with no Android
dependencies, unit-testable in isolation.

## Known gaps / next steps

- The 21-day and history grids render eagerly (no virtualization) — fine
  at realistic scale, would want a proper lazy calendar heatmap for
  multi-year histories.
- Widget layouts are a single medium-sized layout; small/large variants
  and additional widget styles (spec #66-70) are not yet built.
- Smart reminders, evolving owl environment, and annual recap (spec
  Phase Two, #92) are intentionally out of scope for this pass.
