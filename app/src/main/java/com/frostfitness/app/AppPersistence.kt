package com.frostfitness.app

import android.content.Context

private const val PREFS = "frost_fitness_state_v1"

fun loadProfile(context: Context): Profile {
    val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    return Profile().also { p ->
        p.goal = sp.getString("goal", "") ?: ""
        p.age = sp.getString("age", "") ?: ""
        p.height = sp.getString("height", "") ?: ""
        p.weight = sp.getString("weight", "") ?: ""
        p.experience = sp.getString("experience", "") ?: ""
        p.days = sp.getInt("days", 3)
        p.place = sp.getString("place", "Зал") ?: "Зал"
        p.health = sp.getString("health", "") ?: ""
    }
}

fun loadScreen(context: Context): Screen {
    val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val completed = sp.getBoolean("onboarding_complete", false)
    if (!completed) return Screen.WELCOME
    val saved = sp.getString("screen", Screen.HOME.name) ?: Screen.HOME.name
    return runCatching { Screen.valueOf(saved) }.getOrDefault(Screen.HOME)
}

fun loadCompleted(context: Context): Int =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("completed_workouts", 0)

fun saveAppState(context: Context, p: Profile, screen: Screen, completed: Int) {
    val onboardingComplete = p.goal.isNotBlank() && p.age.isNotBlank() && p.height.isNotBlank() &&
        p.weight.isNotBlank() && p.experience.isNotBlank() && p.health.isNotBlank()
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        .putString("goal", p.goal)
        .putString("age", p.age)
        .putString("height", p.height)
        .putString("weight", p.weight)
        .putString("experience", p.experience)
        .putInt("days", p.days)
        .putString("place", p.place)
        .putString("health", p.health)
        .putBoolean("onboarding_complete", onboardingComplete)
        .putString("screen", if (onboardingComplete) screen.name else Screen.WELCOME.name)
        .putInt("completed_workouts", completed)
        .apply()
}
