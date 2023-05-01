package kr.ac.hanbat.notice;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import kr.ac.hanbat.notice.databinding.ActivityKeywordBinding;

public class KeywordActivity extends AppCompatActivity {
    private static final int MAX_SIZE = 10;

    private ActivityKeywordBinding binding;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityKeywordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String keyword = preferences.getString(getString(R.string.pref_keyword), "[]");

        Gson gson = new Gson();
        Type type = new TypeToken<Collection<String>>() {}.getType();
        List<String> keywords = gson.fromJson(keyword, type);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_list_item_single_choice, keywords);
        binding.listKeywords.setAdapter(adapter);
        updateKeywordViews(keywords.size());

        binding.buttonAddKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = binding.editKeyword.getText().toString().trim();
                binding.editKeyword.setText("");
                if (text.isEmpty()) return;

                keywords.add(text);
                adapter.notifyDataSetChanged();

                updateKeywordViews(keywords.size());
                setKeywordPreference(keywords);
            }
        });

        binding.buttonRemoveKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = binding.listKeywords.getCheckedItemPosition();
                if (position == -1) return;

                keywords.remove(position);
                binding.listKeywords.clearChoices();
                adapter.notifyDataSetChanged();

                updateKeywordViews(keywords.size());
                setKeywordPreference(keywords);
            }
        });
    }

    private void setKeywordTitle(int size) {
        binding.textKeyword.setText(getString(R.string.title_keyword, size, MAX_SIZE));
    }

    private void setButtonsState(int size) {
        if (size == MAX_SIZE) {
            binding.buttonAddKeyword.setEnabled(false);
            binding.buttonRemoveKeyword.setEnabled(true);
        } else if (size < MAX_SIZE && size > 0) {
            binding.buttonAddKeyword.setEnabled(true);
            binding.buttonRemoveKeyword.setEnabled(true);
        } else if (size == 0) {
            binding.buttonAddKeyword.setEnabled(true);
            binding.buttonRemoveKeyword.setEnabled(false);
        } else {
            binding.buttonAddKeyword.setEnabled(false);
            binding.buttonRemoveKeyword.setEnabled(false);
        }
    }

    private void updateKeywordViews(int size) {
        setKeywordTitle(size);
        setButtonsState(size);
    }

    private void setKeywordPreference(List<String> keywords) {
        Gson gson = new Gson();
        String keyword = gson.toJson(keywords);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.pref_keyword), keyword);
        editor.apply();
    }
}