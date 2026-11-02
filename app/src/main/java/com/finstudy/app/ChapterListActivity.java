package com.finstudy.app;


import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** 第四章 SimpleAdapter 的字段映射；第五章 Bundle 携带 chapter_id。 */
public class ChapterListActivity extends BaseActivity {
    private ArrayList<JSONObject> chapters;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreen(R.layout.activity_chapters, "课程学习");
        chapters = repo.items(repo.chapters, 0);
        ArrayList<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        for (JSONObject chapter : chapters) {
            Map<String, String> row = new HashMap<String, String>();
            row.put("title", String.format(java.util.Locale.CHINA, "%02d  %s",
                    chapter.optInt("chapterId"), chapter.optString("title")));
            row.put("subtitle", chapter.optString("description") +
                    (repo.available(chapter.optInt("chapterId")) ? "\n进入知识目录" : "\n待更新"));
            rows.add(row);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, rows, R.layout.item_chapter,
                new String[]{"title", "subtitle"}, new int[]{R.id.row_title, R.id.row_subtitle});
        ListView listView = findViewById(R.id.lv_chapters);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int chapterId = chapters.get(position).optInt("chapterId");
                if (!repo.available(chapterId)) { toast("本章内容待更新"); return; }
                Bundle bundle = new Bundle();
                bundle.putInt("chapter_id", chapterId);
                openPage(KnowledgeActivity.class, bundle);
            }
        });
    }
}
