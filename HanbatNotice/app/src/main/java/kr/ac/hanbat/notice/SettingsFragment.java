package kr.ac.hanbat.notice;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final int REQUEST_CODE = 0;
    private AlarmManager manager;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        SharedPreferences preferences = getPreferenceManager().getSharedPreferences();

        if (getActivity() != null) {
            manager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        }

        SwitchPreferenceCompat pushPreference = findPreference(getString(R.string.pref_push));
        if (pushPreference != null && preferences != null) {
            requestPostNotifications();

            pushPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                    if (getActivity() == null) return false;

                    if ((boolean)newValue) {
                        String keyword = preferences.getString(getString(R.string.pref_keyword), "[]");
                        if (keyword.equals("[]")) {
                            Toast.makeText(getActivity(), R.string.error_empty_keyword,
                                           Toast.LENGTH_SHORT).show();
                            return false;
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                                   !manager.canScheduleExactAlarms()) {
                            showExactAlarmDialog();
                            return false;
                        } else {
                            AlarmUtils.set(getActivity());
                            return true;
                        }
                    } else {
                        AlarmUtils.cancel(getActivity());
                        return true;
                    }
                }
            });
        }

        Preference timePreference = findPreference(getString(R.string.pref_time));
        if (timePreference != null && preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            timePreference.setSummary(preferences.getString(getString(R.string.pref_time), ""));

            if (TextUtils.isEmpty(timePreference.getSummary())) {
                createNoticeChannel();

                editor.putString(getString(R.string.pref_time), "00:00");
                editor.apply();
                timePreference.setSummary("00:00");
            }

            timePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    String[] time = timePreference.getSummary().toString().split(":");
                    int hourOfDay = Integer.parseInt(time[0]);
                    int minute = Integer.parseInt(time[1]);

                    TimePickerDialog dialog = new TimePickerDialog(
                        getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                String time = new SimpleDateFormat(
                                    "HH:mm", Locale.getDefault()).format(calendar.getTime());
                                editor.putString(getString(R.string.pref_time), time);
                                editor.apply();
                                timePreference.setSummary(time);

                                if (getActivity() != null) {
                                    AlarmUtils.set(getActivity());
                                }
                            }
                        }, hourOfDay, minute, false);
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.show();

                    return true;
                }
            });
        }

        Preference licensePreference = findPreference(getString(R.string.pref_license));
        if (licensePreference != null) {
            licensePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
                    return true;
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SwitchPreferenceCompat pushPreference = findPreference(getString(R.string.pref_push));
        if (pushPreference != null) {
            if (pushPreference.isChecked() && getActivity() != null) {
                AlarmUtils.set(getActivity());
            }
        }
    }

    private void requestPostNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        if (getActivity() == null) return;

        if (ContextCompat.checkSelfPermission(
            getActivity(), Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_DENIED) {

            String[] permissions = { Manifest.permission.POST_NOTIFICATIONS };
            ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_CODE);
        }
    }

    private void showExactAlarmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.app_name)
               .setMessage(R.string.msg_exact_alarm)
               .setPositiveButton("예", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;

                       Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                       startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, uri));
                   }
               })
               .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       SwitchPreferenceCompat push = findPreference(getString(R.string.pref_push));
                       if (push != null && getActivity() != null) {
                           push.setChecked(true);
                           AlarmUtils.set(getActivity());
                       }
                   }
               })
               .setCancelable(false)
               .show();
    }

    private void createNoticeChannel() {
        if (getActivity() == null) return;

        NotificationManagerCompat manager = NotificationManagerCompat.from(getActivity());
        manager.createNotificationChannel(
            new NotificationChannelCompat.Builder(getString(R.string.notice_channel_id),
                                                  NotificationManagerCompat.IMPORTANCE_DEFAULT)
                                         .setName(getString(R.string.notice_channel_name))
                                         .build());
    }
}