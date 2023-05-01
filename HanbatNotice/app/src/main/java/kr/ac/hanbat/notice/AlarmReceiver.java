package kr.ac.hanbat.notice;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String URL = "https://www.hanbat.ac.kr/bbs/BBSMSTR_000000000050/list.do";
    private static final int REQUEST_CODE = 0;
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmUtils.set(context);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String keyword = preferences.getString(context.getString(R.string.pref_keyword), "[]");
        if (!keyword.equals("[]")) {
            findNotice(context, keyword);
        }
    }

    private void findNotice(Context context, String keyword) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        PendingResult pendingResult = goAsync();
        new Thread(() -> {
            try {
                if (NoticeScraper.find(context, keyword, calendar.getTime())) {
                    showNotification(context);
                }
            } catch (Exception ignored) {
            } finally {
                pendingResult.finish();
            }
        }).start();
    }

    private void showNotification(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
            context, context.getString(R.string.notice_channel_id));
        builder.setSmallIcon(R.drawable.ic_stat_notify)
               .setContentTitle(context.getString(R.string.notice_channel_name))
               .setContentText(context.getText(R.string.msg_notice_found))
               .setPriority(NotificationCompat.PRIORITY_DEFAULT)
               .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
               .setContentIntent(pendingIntent)
               .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(NOTIFICATION_ID, builder.build());
    }
}