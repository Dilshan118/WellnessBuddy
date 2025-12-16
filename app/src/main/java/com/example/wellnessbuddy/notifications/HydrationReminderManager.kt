package com.example.wellnessbuddy.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.HydrationManager
import java.util.*

/**
 * Manages hydration reminders using AlarmManager and notifications
 */
class HydrationReminderManager(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = NotificationManagerCompat.from(context)
    private val hydrationManager = HydrationManager(context)
    
    companion object {
        const val CHANNEL_ID = "hydration_reminders"
        const val NOTIFICATION_ID = 1001
        const val REQUEST_CODE = 2001
    }
    
    init {
        createNotificationChannel()
    }
    
        private fun createNotificationChannel() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        "Hydration Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Reminds you to drink water throughout the day"
                        enableVibration(true)
                        enableLights(true)
                        setShowBadge(true)
                        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                        setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
                    }

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)
                    android.util.Log.d("HydrationReminder", "Notification channel created successfully")
                }
            } catch (e: Exception) {
                android.util.Log.e("HydrationReminder", "Error creating notification channel", e)
            }
        }
    
    fun scheduleReminders() {
        val settings = hydrationManager.loadSettings()
        
        if (!settings.isEnabled) {
            cancelReminders()
            return
        }
        
        // Get interval in minutes using the new system
        val intervalMinutes = when (settings.reminderIntervalType) {
            com.example.wellnessbuddy.data.ReminderIntervalType.MINUTES -> settings.reminderInterval
            com.example.wellnessbuddy.data.ReminderIntervalType.HOURS -> settings.reminderInterval * 60
        }
        
        // For testing: if interval is very short (≤ 5 minutes), schedule an immediate test alarm
        if (intervalMinutes <= 5) {
            scheduleImmediateTestAlarm()
        }
        
        // Parse start and end times from settings with validation
        val startTimeParts = settings.startTime.split(":")
        val endTimeParts = settings.endTime.split(":")
        
        if (startTimeParts.size != 2 || endTimeParts.size != 2) {
            android.util.Log.e("HydrationReminder", "Invalid time format: start=${settings.startTime}, end=${settings.endTime}")
            return
        }
        
        val startHour = try { startTimeParts[0].toInt() } catch (e: NumberFormatException) { 8 }
        val startMinute = try { startTimeParts[1].toInt() } catch (e: NumberFormatException) { 0 }
        val endHour = try { endTimeParts[0].toInt() } catch (e: NumberFormatException) { 22 }
        val endMinute = try { endTimeParts[1].toInt() } catch (e: NumberFormatException) { 0 }
        
        // Validate interval
        if (intervalMinutes <= 0) {
            android.util.Log.e("HydrationReminder", "Invalid interval: $intervalMinutes minutes")
            return
        }
        
        // Calculate total active minutes
        val startTotalMinutes = startHour * 60 + startMinute
        val endTotalMinutes = endHour * 60 + endMinute
        val totalActiveMinutes = endTotalMinutes - startTotalMinutes
        
        if (totalActiveMinutes <= 0) {
            android.util.Log.e("HydrationReminder", "Invalid time range: start=$startHour:$startMinute, end=$endHour:$endMinute")
            return
        }
        
        // Calculate number of reminders
        val numberOfReminders = (totalActiveMinutes / intervalMinutes).coerceAtLeast(1)
        
        android.util.Log.d("HydrationReminder", "Scheduling $numberOfReminders reminders every $intervalMinutes minutes from $startHour:$startMinute to $endHour:$endMinute")
        
        // Cancel existing reminders first
        cancelReminders(numberOfReminders)
        
        // Schedule reminders throughout the active period
        try {
            for (i in 0 until numberOfReminders) {
                val reminderMinutes = startTotalMinutes + (i * intervalMinutes)
                val reminderHour = reminderMinutes / 60
                val reminderMinute = reminderMinutes % 60

                val reminderTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, reminderHour)
                    set(Calendar.MINUTE, reminderMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)

                    // If the time has passed today, schedule for tomorrow
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }
                }

                val intent = Intent(context, HydrationReminderReceiver::class.java).apply {
                    putExtra("reminder_id", i)
                    putExtra("interval_minutes", intervalMinutes)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE + i,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
                
                android.util.Log.d("HydrationReminder", "Scheduled reminder $i at ${reminderHour}:${reminderMinute}")
            }
        } catch (e: Exception) {
            android.util.Log.e("HydrationReminder", "Error scheduling reminders", e)
        }
    }
    
    private fun scheduleImmediateTestAlarm() {
        try {
            android.util.Log.d("HydrationReminder", "Scheduling immediate test alarm")
            
            // Schedule alarm for 10 seconds from now
            val testTime = Calendar.getInstance().apply {
                add(Calendar.SECOND, 10)
            }
            
            val intent = Intent(context, HydrationReminderReceiver::class.java).apply {
                putExtra("reminder_id", -1) // Special ID for test alarm
                putExtra("is_test", true)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                9999, // Special request code for test alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                testTime.timeInMillis,
                pendingIntent
            )
            
            android.util.Log.d("HydrationReminder", "Test alarm scheduled for ${testTime.time}")
        } catch (e: Exception) {
            android.util.Log.e("HydrationReminder", "Error scheduling test alarm", e)
        }
    }
    
    fun cancelReminders(maxReminders: Int = 20) {
        for (i in 0 until maxReminders) {
            val intent = Intent(context, HydrationReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE + i,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
        }
    }
    
    fun checkNotificationStatus(): String {
        val status = StringBuilder()
        
        // Check if notifications are enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            status.append("Notifications enabled: ${notificationManager.areNotificationsEnabled()}\n")
        }
        
        // Check channel status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .getNotificationChannel(CHANNEL_ID)
            status.append("Channel exists: ${channel != null}\n")
            if (channel != null) {
                status.append("Channel importance: ${channel.importance}\n")
                status.append("Channel enabled: ${channel.importance != NotificationManager.IMPORTANCE_NONE}\n")
            }
        }
        
        return status.toString()
    }
    
    fun showHydrationNotification() {
        try {
            // Check notification permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!notificationManager.areNotificationsEnabled()) {
                    android.util.Log.e("HydrationReminder", "Notifications are disabled by user")
                    return
                }
            }

            val settings = hydrationManager.loadSettings()
            val dailyConsumption = settings.glassesConsumed
            val progress = (dailyConsumption.toFloat() / settings.dailyGoal).coerceAtMost(1f)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_hydration)
                .setContentTitle("💧 Time to Hydrate!")
                .setContentText("You've had ${dailyConsumption}/${settings.dailyGoal} glasses today")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setProgress(settings.dailyGoal, dailyConsumption, false)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Stay hydrated! You're ${String.format("%.0f", progress * 100)}% towards your daily goal."))
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(longArrayOf(0, 300, 100, 300))
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
            android.util.Log.d("HydrationReminder", "Notification shown successfully")
        } catch (e: Exception) {
            android.util.Log.e("HydrationReminder", "Error showing notification", e)
        }
    }
}

/**
 * Broadcast receiver for hydration reminders
 */
class HydrationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val reminderManager = HydrationReminderManager(context)
            reminderManager.showHydrationNotification()
            android.util.Log.d("HydrationReminder", "Reminder received and notification shown")
        } catch (e: Exception) {
            android.util.Log.e("HydrationReminder", "Error in receiver", e)
        }
    }
}
