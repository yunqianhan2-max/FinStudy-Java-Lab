package com.finstudy.app;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuizResultActivity extends BaseActivity implements View.OnClickListener {
    private int chapterId;
    private String reviewQuestionId;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setScreen(R.layout.activity_results, "测验结果");
        long attemptId = getIntent().getLongExtra("attempt_id", -1);
        String[] args = {String.valueOf(attemptId)};
        try (Cursor cursor = store.getReadableDatabase().rawQuery(
                "SELECT chapter,correct,total,kind FROM attempt WHERE id=?", args)) {
            if (!cursor.moveToFirst()) { toast("测验记录不存在"); finish(); return; }
            chapterId = cursor.getInt(0);
            int correct = cursor.getInt(1), total = cursor.getInt(2);
            boolean review = "review".equals(cursor.getString(3));
            String summary = review ? "错题重练" : repo.chapter(chapterId);
            summary += "\n" + (review ? correct + " / " + total + " 题正确" : correct * 10 + " 分");
            summary += " · 正确率 " + Grading.percentage(correct, total) + "%";
            ((TextView) findViewById(R.id.tv_result_summary)).setText(summary);
            if (review) reviewQuestionId = "";
        }
        ArrayList<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        try (Cursor cursor = store.getReadableDatabase().rawQuery(
                "SELECT question,user,correct FROM answer WHERE attempt=? ORDER BY question", args)) {
            while (cursor.moveToNext()) {
                String questionId = cursor.getString(0);
                JSONObject question = repo.find(repo.questions, "questionId", questionId);
                if (question == null) continue;
                if (reviewQuestionId != null) reviewQuestionId = questionId;
                Map<String, String> row = new HashMap<String, String>();
                row.put("title", (cursor.getInt(2) == 1 ? "正确 · " : "错误 · ") + question.optString("stem"));
                StringBuilder options = new StringBuilder();
                JSONArray values = question.optJSONArray("options");
                for (int i = 0; i < values.length(); i++) options.append((char) ('A' + i)).append(". ").append(values.optString(i)).append('\n');
                row.put("subtitle", options.toString().trim());
                row.put("detail", "你的答案：" + Repo.labels(cursor.getString(1))
                        + "\n正确答案：" + Repo.labels(Repo.answer(question.optJSONArray("correctAnswers")))
                        + "\n解析：" + question.optString("explanation"));
                rows.add(row);
            }
        }
        ((ListView) findViewById(R.id.lv_results)).setAdapter(new SimpleAdapter(this, rows,
                R.layout.item_result, new String[]{"title", "subtitle", "detail"},
                new int[]{R.id.row_title, R.id.row_subtitle, R.id.row_detail}));
        findViewById(R.id.btn_retry).setOnClickListener(this);
        findViewById(R.id.btn_result_wrong).setOnClickListener(this);
        findViewById(R.id.btn_result_home).setOnClickListener(this);
    }
    @Override public void onClick(View view) {
        if (view.getId() == R.id.btn_retry) {
            Bundle bundle = new Bundle();
            if (reviewQuestionId != null) bundle.putString("question_id", reviewQuestionId);
            else bundle.putInt("chapter_id", chapterId);
            openPage(QuizActivity.class, bundle); finish();
        } else if (view.getId() == R.id.btn_result_wrong) openPage(WrongBookActivity.class);
        else if (view.getId() == R.id.btn_result_home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent); finish();
        }
    }
}
