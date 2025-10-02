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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to drink water throughout the day"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun scheduleReminders() {
        val settings = hydrationManager.loadSettings()
        
        if (!settings.isEnabled) {
            cancelReminders()
            return
        }
        
        val intervalMinutes = settings.reminderInterval * 60 // Convert hours to minutes
        val startHour = 8 // Start at 8 AM
        val endHour = 22 // End at 10 PM
        
        // Calculate number of reminders per day
        val totalMinutes = (endHour - startHour) * 60
        val numberOfReminders = totalMinutes / intervalMinutes
        
        // Schedule reminders throughout the day
        for (i in 0 until numberOfReminders) {
            val reminderTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, i * intervalMinutes)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // If the time has passed today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            val intent = Intent(context, HydrationReminderReceiver::class.java).apply {
                putExtra("reminder_id", i)
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
        }
    }
    
    fun cancelReminders() {
        val numberOfReminders = 20 // Maximum expected reminders per day
        
        for (i in 0 until numberOfReminders) {
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
    
    fun showHydrationNotification() {
        val settings = hydrationManager.loadSettings()
        val dailyConsumption = settings.glassesConsumed
        val progress = (dailyConsumption.toFloat() / settings.dailyGoal).coerceAtMost(1f)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_hydration)
            .setContentTitle("💧 Time to Hydrate!")
            .setContentText("You've had ${dailyConsumption}/${settings.dailyGoal} glasses today")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setProgress(settings.dailyGoal, dailyConsumption, false)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Stay hydrated! You're ${String.format("%.0f", progress * 100)}% towards your daily goal."))
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}

/**
 * Broadcast receiver for hydration reminders
 */
class HydrationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderManager = HydrationReminderManager(context)
        reminderManager.showHydrationNotification()
    }
}
