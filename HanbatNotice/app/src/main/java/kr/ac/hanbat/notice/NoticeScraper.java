package kr.ac.hanbat.notice;

import android.content.Context;
import android.webkit.WebSettings;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NoticeScraper {
    private static final String URL = "https://www.hanbat.ac.kr/bbs/BBSMSTR_000000000050/list.do";

    private NoticeScraper() {}

    public static boolean find(Context context, String keyword, Date date) throws Exception {
        Document document = Jsoup.connect(URL).data(getData())
                                 .userAgent(WebSettings.getDefaultUserAgent(context))
                                 .referrer(URL).timeout(5000).post();

        Elements elements = document.select("table[class^=board_list]")
                                    .select("tbody > tr:not(.notice)");
        Elements subjects = elements.select("td.subject > a");
        Elements regDates = elements.select("td.regDate");

        Gson gson = new Gson();
        Type type = new TypeToken<Collection<String>>() {}.getType();
        List<String> keywords = gson.fromJson(keyword, type);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < elements.size(); ++i) {
            String reg = regDates.get(i).text();
            if (reg.equals(dateFormat.format(date))) {
                for (String subject : keywords) {
                    if (subjects.get(i).text().contains(subject)) return true;
                }
            } else {
                Date regDate = dateFormat.parse(reg);
                if (regDate != null && regDate.before(date)) break;
            }
        }

        return false;
    }

    private static Map<String, String> getData() {
        Map<String, String> data = new HashMap<>();
        data.put("pageIndex", "1");
        data.put("cmsNoStr", "");
        data.put("nttId", "");
        data.put("mno", "sub07_01");
        data.put("searchCondition", "all");
        data.put("searchKeyword", "");
        data.put("pageUnit", "50");

        return data;
    }
}