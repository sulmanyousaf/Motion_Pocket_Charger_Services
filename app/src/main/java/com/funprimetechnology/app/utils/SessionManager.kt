package com.funprimetechnology.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.funprimetechnology.app.R

class SessionManager(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preferrence_name), Context.MODE_PRIVATE)

    fun saveAlarmState(
        saveAlarmState: Boolean
    ) {
        val editor = prefs.edit()
        editor.putBoolean("AlarmState", saveAlarmState)
        editor.apply()

    }

    fun fetchAlarmState(): Boolean {
        return prefs.getBoolean("AlarmState", false)
    }

    fun savePocketBtnState(
        savePocketBtnState: Boolean
    ) {
        val editor = prefs.edit()
        editor.putBoolean("PocketBtnState", savePocketBtnState)
        editor.apply()

    }

    fun fetchPocketBtnState(): Boolean {
        return prefs.getBoolean("PocketBtnState", false)
    }

    fun saveChargerBtnState(
        saveChargerBtnState: Boolean
    ) {
        val editor = prefs.edit()
        editor.putBoolean("ChargerBtnState", saveChargerBtnState)
        editor.apply()

    }

    fun fetchChargerBtnState(): Boolean {
        return prefs.getBoolean("ChargerBtnState", false)
    }

    fun saveMotionBtnState(
        saveMotionBtnState: Boolean
    ) {
        val editor = prefs.edit()
        editor.putBoolean("MotionBtnState", saveMotionBtnState)
        editor.apply()

    }

    fun fetchMotionBtnState(): Boolean {
        return prefs.getBoolean("MotionBtnState", false)
    }
}