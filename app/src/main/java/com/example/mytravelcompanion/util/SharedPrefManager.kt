package com.example.mytravelcompanion.util

import android.content.Context

object SharedPrefManager {

    //Notifiche di inattività

    private const val NAME = "inactivity_prefs"
    private const val KEY_ALREADY_NOTIFIED = "already_notified"

    fun hasAlreadyNotified(context: Context): Boolean {
        val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ALREADY_NOTIFIED, false)
    }

    fun markNotified(context: Context) {
        val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ALREADY_NOTIFIED, true).apply()
    }

    fun resetNotified(context: Context) {
        val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ALREADY_NOTIFIED, false).apply()
    }


}
