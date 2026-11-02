package com.finstudy.app;


import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import org.json.JSONObject;

public class StatisticsActivity extends BaseActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setScreen(R.layout.activity_statistics, "学习统计");
    }
    @Override protected void onResume() {
        super.onResume();
        int total = store.scalar("SELECT COALESCE(SUM(total),0) FROM attempt");
        int correct = store.scalar("SELECT COALESCE(SUM(correct),0) FROM attempt");
        String summary = "已完成章节  " + store.completed(repo) + " / " + repo.availableChapters(false).size()
                + "\n已掌握知识点  " + store.scalar("SELECT COUNT(*) FROM progress WHERE status=2")
                + " / " + repo.knowledge.length() + "\n累计答题量  " + total
                + "\n累计正确率  " + (total == 0 ? "暂无数据" : Grading.percentage(correct, total) + "%")
                + "\n当前错题数  " + store.scalar("SELECT COUNT(*) FROM wrong WHERE mastered=0");
        ((TextView) findViewById(R.id.tv_statistics_summary)).setText(summary);
        StringBuilder rates = new StringBuilder();
        for (JSONObject chapter : repo.availableChapters(false)) {
            String rate = "暂无数据";
            try (Cursor cursor = store.getReadableDatabase().rawQuery(
                    "SELECT correct,total FROM attempt WHERE chapter=? AND kind='chapter' ORDER BY id DESC LIMIT 1",
                    new String[]{String.valueOf(chapter.optInt("chapterId"))})) {
                if (cursor.moveToFirst()) rate = Grading.percentage(cursor.getInt(0), cursor.getInt(1)) + "%";
            }
            rates.append(chapter.optString("title")).append("：").append(rate).append('\n');
        }
        ((TextView) findViewById(R.id.tv_chapter_rates)).setText(rates.toString().trim());
        StringBuilder history = new StringBuilder();
        try (Cursor cursor = store.getReadableDatabase().rawQuery(
                "SELECT chapter,correct,total,kind FROM attempt ORDER BY id DESC LIMIT 12", null)) {
            while (cursor.moveToNext()) {
                history.append(repo.chapter(cursor.getInt(0))).append(" · ")
                        .append("review".equals(cursor.getString(3)) ? "错题重练" : "章节测验")
                        .append(" · ").append(cursor.getInt(1)).append('/').append(cursor.getInt(2)).append(" 正确\n");
            }
        }
        ((TextView) findViewById(R.id.tv_attempt_history)).setText(history.length() == 0 ? "尚未提交测验。" : history.toString().trim());
    }
}
