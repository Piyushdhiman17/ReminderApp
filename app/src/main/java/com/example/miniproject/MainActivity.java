package com.example.miniproject;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText editTextReminder;
    private TimePicker timePicker;
    private TextView textViewReminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextReminder = findViewById(R.id.editTextReminder);
        timePicker = findViewById(R.id.timePicker);
        textViewReminders = findViewById(R.id.textViewReminders);
        Button buttonSetReminder = findViewById(R.id.buttonSetReminder);

        // Create notification channel
        createNotificationChannel();

        // Ask for POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        buttonSetReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reminderText = editTextReminder.getText().toString().trim();
                if (reminderText.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a reminder", Toast.LENGTH_SHORT).show();
                    return;
                }

                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                // If selected time is before current time, set for next day
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DATE, 1);
                }

                long triggerTime = calendar.getTimeInMillis();

                Intent intent = new Intent(MainActivity.this, ReminderReceiver.class);
                intent.putExtra("reminderText", reminderText);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        MainActivity.this,
                        (int) System.currentTimeMillis(), // unique ID
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                );

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                }

                // Display confirmation
                String displayTime = String.format("%02d:%02d", hour, minute);
                textViewReminders.append("\n• " + reminderText + " at " + displayTime);
                Toast.makeText(MainActivity.this, "Reminder set!", Toast.LENGTH_SHORT).show();
                editTextReminder.setText("");
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "reminder_channel",
                    "Reminder Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for Reminder Notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
