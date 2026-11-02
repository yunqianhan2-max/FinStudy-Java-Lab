package com.finstudy.app;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

/** XML EditText + TextWatcher 搜索；BaseAdapter 显示知识列表。 */
public class KnowledgeActivity extends BaseActivity {
    private int chapterId;
    private EditText search;
    private ListView listView;
    private ArrayList<JSONObject> displayed = new ArrayList<JSONObject>();
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setScreen(R.layout.activity_knowledge, "知识目录");
        Bundle bundle = getIntent().getExtras();
        chapterId = bundle == null ? 1 : bundle.getInt("chapter_id", 1);
        if (state != null) chapterId = state.getInt("chapter", chapterId);
        if (!repo.available(chapterId)) { toast("章节不存在或内容待更新"); finish(); return; }
        ((TextView) findViewById(R.id.tv_chapter_intro)).setText(repo.chapter(chapterId));
        search = findViewById(R.id.et_search);
        listView = findViewById(R.id.lv_knowledge);
        listView.setEmptyView(findViewById(R.id.tv_knowledge_empty));
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refresh(); }
            @Override public void afterTextChanged(Editable editable) { }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openKnowledge(displayed.get(position).optString("knowledgeId"));
            }
        });
        if (state != null) search.setText(state.getString("search", ""));
    }
    @Override protected void onResume() { super.onResume(); if (listView != null) refresh(); }
    private void refresh() {
        String keyword = search.getText().toString().trim().toLowerCase(Locale.ROOT);
        displayed = new ArrayList<JSONObject>();
        for (JSONObject item : repo.items(repo.knowledge, keyword.isEmpty() ? chapterId : 0)) {
            String searchable = item.optString("title") + item.optString("content") + item.optString("core")
                    + item.optString("codeExample") + item.optString("note");
            if (searchable.toLowerCase(Locale.ROOT).contains(keyword)) displayed.add(item);
        }
        listView.setAdapter(new KnowledgeAdapter(this, displayed, repo, store));
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("chapter", chapterId);
        if (search != null) state.putString("search", search.getText().toString());
    }
}
