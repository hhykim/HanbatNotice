package kr.ac.hanbat.notice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.AlarmManagerCompat;
import androidx.preference.PreferenceManager;

import java.util.Calendar;

public class AlarmUtils {
    private static final int REQUEST_CODE = 0;

    private AlarmUtils() {}

    public static void set(Context context) {
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !manager.canScheduleExactAlarms()) {

            AlarmManagerCompat.setAndAllowWhileIdle(
                manager, AlarmManager.RTC_WAKEUP,
                getCalendar(context).getTimeInMillis(), pendingIntent);
        } else {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                manager, AlarmManager.RTC_WAKEUP,
                getCalendar(context).getTimeInMillis(), pendingIntent);
        }
    }

    public static void cancel(Context context) {
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);

        if (pendingIntent != null) {
            manager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static Calendar getCalendar(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String[] time = preferences.getString(context.getString(R.string.pref_time), "00:00")
                                   .split(":");
        int hourOfDay = Integer.parseInt(time[0]);
        int minute = Integer.parseInt(time[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.compareTo(Calendar.getInstance()) <= 0) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return calendar;
    }
}