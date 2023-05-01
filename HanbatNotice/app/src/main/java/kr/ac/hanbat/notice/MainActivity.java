package kr.ac.hanbat.notice;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.Date;

import kr.ac.hanbat.notice.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        binding.buttonFindNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = preferences.getString(getString(R.string.pref_keyword), "[]");
                if (keyword.equals("[]")) {
                    Toast.makeText(getApplicationContext(), R.string.error_empty_keyword,
                                   Toast.LENGTH_SHORT).show();
                } else {
                    findNotice(keyword);
                }
            }
        });

        binding.buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
    }

    private void findNotice(String keyword) {
        binding.buttonFindNotice.setEnabled(false);
        binding.buttonSettings.setEnabled(false);
        binding.progressFindNotice.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                if (NoticeScraper.find(this, keyword, new Date())) {
                    runOnUiThread(() -> showNoticeDialog(true));
                } else {
                    runOnUiThread(() -> showNoticeDialog(false));
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), R.string.error_jsoup_exception,
                                   Toast.LENGTH_SHORT).show();
                });
            } finally {
                runOnUiThread(() -> {
                    binding.buttonFindNotice.setEnabled(true);
                    binding.buttonSettings.setEnabled(true);
                    binding.progressFindNotice.setVisibility(View.INVISIBLE);
                });
            }
        }).start();
    }

    private void showNoticeDialog(boolean found) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (found) {
            builder.setMessage(R.string.msg_notice_found);
        } else {
            builder.setMessage(R.string.msg_notice_not_found);
        }

        builder.setTitle(R.string.app_name)
               .setPositiveButton("확인", null)
               .show();
    }
}