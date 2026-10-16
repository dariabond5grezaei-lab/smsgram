package com.example.domain.scheduling

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

/**
 * Helper object for detecting and opening battery optimization settings (S13).
 */
object BatteryOptimizationHelper {

    private const val TAG = "BatteryOptimization"

    /**
     * Checks if the app is currently whitelisted / ignoring battery optimizations.
     * Returns true on API < 23 where Doze mode does not exist.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking isIgnoringBatteryOptimizations: ${e.message}")
            true
        }
    }

    /**
     * Creates an intent to navigate to battery optimization settings.
     * Uses Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS per policy guidelines.
     */
    fun batterySettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    }

    /**
     * Safely opens battery optimization settings with fallback to Application Details Settings.
     */
    fun openBatterySettings(context: Context) {
        try {
            val intent = batterySettingsIntent(context).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS not found, opening app details: ${e.message}")
            try {
                val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Failed to open app details fallback: ${fallbackEx.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error launching battery settings: ${e.message}")
        }
    }
}
