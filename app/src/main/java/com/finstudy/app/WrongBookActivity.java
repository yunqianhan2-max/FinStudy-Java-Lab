package com.finstudy.app;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** ListView 直接占据剩余屏幕，不再嵌套在 ScrollView 中。 */
public class WrongBookActivity extends BaseActivity {
    private Spinner filter;
    private CheckBox history;
    private ListView list;
    private final ArrayList<String> displayedIds = new ArrayList<String>();
    private ArrayList<JSONObject> chapters;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setScreen(R.layout.activity_wrong, "错题本");
        filter = findViewById(R.id.sp_wrong_chapter);
        history = findViewById(R.id.cb_show_mastered);
        list = findViewById(R.id.lv_wrong);
        list.setEmptyView(findViewById(R.id.tv_wrong_empty));
        chapters = repo.availableChapters(false);
        ArrayList<String> titles = new ArrayList<String>(); titles.add("全部章节");
        for (JSONObject chapter : chapters) titles.add(chapter.optString("title"));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, titles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filter.setAdapter(adapter);
        filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { refresh(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        history.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean checked) { refresh(); }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String questionId = displayedIds.get(position);
                new AlertDialog.Builder(WrongBookActivity.this).setTitle("错题操作")
                        .setItems(new String[]{"重新作答", "标记为已掌握", "恢复为待复习"}, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    Bundle data = new Bundle(); data.putString("question_id", questionId);
                                    openPage(QuizActivity.class, data);
                                } else { store.mastered(questionId, which == 1); refresh(); }
                            }
                        }).show();
            }
        });
    }
    @Override protected void onResume() { super.onResume(); if (list != null) refresh(); }
    private void refresh() {
        displayedIds.clear();
        int selected = filter.getSelectedItemPosition();
        int chapterFilter = selected > 0 ? chapters.get(selected - 1).optInt("chapterId") : 0;
        ArrayList<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        String sql = "SELECT id,count,mastered,at FROM wrong "
                + (history.isChecked() ? "" : "WHERE mastered=0 ") + "ORDER BY at DESC";
        try (Cursor cursor = store.getReadableDatabase().rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                JSONObject question = repo.find(repo.questions, "questionId", cursor.getString(0));
                if (question == null || (chapterFilter != 0 && question.optInt("chapterId") != chapterFilter)) continue;
                displayedIds.add(cursor.getString(0));
                Map<String, String> row = new HashMap<String, String>();
                row.put("title", question.optString("stem"));
                row.put("subtitle", repo.chapter(question.optInt("chapterId")) + " · 错 " + cursor.getInt(1)
                        + " 次 · " + (cursor.getInt(2) == 1 ? "已掌握" : "待复习") + "\n"
                        + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(new Date(cursor.getLong(3))));
                rows.add(row);
            }
        }
        list.setAdapter(new SimpleAdapter(this, rows, R.layout.item_knowledge,
                new String[]{"title", "subtitle"}, new int[]{R.id.row_title, R.id.row_subtitle}));
    }
}
