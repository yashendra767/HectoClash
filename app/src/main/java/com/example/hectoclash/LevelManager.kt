package com.example.hectoclash

import android.content.Context
import android.content.SharedPreferences

object LevelManager {
    private const val PREFS_NAME = "level_prefs"
    private const val UNLOCKED_LEVEL_KEY = "unlocked_level"

    fun getUnlockedLevel(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(UNLOCKED_LEVEL_KEY, 1) // Level 1 is unlocked by default
    }

    fun unlockNextLevel(context: Context, currentLevel: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val currentUnlocked = getUnlockedLevel(context)

        if (currentLevel >= currentUnlocked) {
            editor.putInt(UNLOCKED_LEVEL_KEY, currentLevel + 1)
            editor.apply()
        }
    }
}
