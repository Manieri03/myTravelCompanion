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

    //Geofencing

    private const val PREF_NAME = "geofence_state"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isInside(context: Context, geofenceId: String): Boolean {
        return prefs(context).getBoolean(geofenceId, false)
    }

    fun setInside(context: Context, geofenceId: String, inside: Boolean) {
        prefs(context).edit().putBoolean(geofenceId, inside).apply()
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
