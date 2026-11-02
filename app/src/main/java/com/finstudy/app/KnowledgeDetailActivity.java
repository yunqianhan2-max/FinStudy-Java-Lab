package com.finstudy.app;


import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.ArrayList;

/** 详情独立为 Activity，展示第五章显式跳转；学习状态沿用原数据库。 */
public class KnowledgeDetailActivity extends BaseActivity implements View.OnClickListener {
    private String knowledgeId;
    private int chapterId;
    private int position;
    private ArrayList<JSONObject> chapterKnowledge;
    private boolean binding;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setScreen(R.layout.activity_detail, "知识详情");
        knowledgeId = state == null ? getIntent().getStringExtra("knowledge_id") : state.getString("knowledge");
        if (knowledgeId == null) knowledgeId = "c1k1";
        int[] ids = {R.id.ib_favorite, R.id.btn_previous_knowledge, R.id.btn_next_knowledge,
                R.id.btn_related_quiz, R.id.btn_return_directory};
        for (int id : ids) findViewById(id).setOnClickListener(this);
        ((CheckBox) findViewById(R.id.cb_mastered)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                if (!binding) store.status(knowledgeId, checked ? 2 : 1);
            }
        });
        showKnowledge();
    }
    private void showKnowledge() {
        JSONObject item = repo.find(repo.knowledge, "knowledgeId", knowledgeId);
        if (item == null) { toast("知识点不存在"); finish(); return; }
        chapterId = item.optInt("chapterId");
        if (!repo.available(chapterId)) { toast("本章内容待更新"); finish(); return; }
        ((TextView) findViewById(R.id.tv_toolbar_title)).setText(repo.chapter(chapterId));
        ((TextView) findViewById(R.id.tv_knowledge_title)).setText(item.optString("title"));
        ((TextView) findViewById(R.id.tv_source)).setText(item.optString("source"));
        ((TextView) findViewById(R.id.tv_concept)).setText(item.optString("content"));
        ((TextView) findViewById(R.id.tv_core)).setText(item.optString("core"));
        ((TextView) findViewById(R.id.tv_note)).setText(item.optString("note"));
        String code = item.optString("codeExample");
        ((TextView) findViewById(R.id.tv_code)).setText(code);
        findViewById(R.id.tv_code).setVisibility(code.isEmpty() ? View.GONE : View.VISIBLE);
        findViewById(R.id.detail_code_heading).setVisibility(code.isEmpty() ? View.GONE : View.VISIBLE);
        if (store.status(knowledgeId) == 0) store.status(knowledgeId, 1);
        getSharedPreferences("learning", MODE_PRIVATE).edit().putString("last", knowledgeId).apply();
        binding = true;
        ((CheckBox) findViewById(R.id.cb_mastered)).setChecked(store.status(knowledgeId) == 2);
        binding = false;
        updateFavorite();
        chapterKnowledge = repo.items(repo.knowledge, chapterId);
        for (int i = 0; i < chapterKnowledge.size(); i++) {
            if (chapterKnowledge.get(i).optString("knowledgeId").equals(knowledgeId)) position = i;
        }
        findViewById(R.id.btn_previous_knowledge).setEnabled(position > 0);
        findViewById(R.id.btn_next_knowledge).setEnabled(position + 1 < chapterKnowledge.size());
    }
    private void updateFavorite() {
        boolean favorite = store.favorite(knowledgeId);
        ImageButton button = findViewById(R.id.ib_favorite);
        button.setAlpha(favorite ? 1f : 0.35f);
        button.setContentDescription(favorite ? "取消收藏" : "收藏知识点");
        ((TextView) findViewById(R.id.tv_favorite_label)).setText(favorite ? "已收藏，点击取消" : "点击收藏重点");
    }
    @Override public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ib_favorite) { store.toggle(knowledgeId); updateFavorite(); }
        else if (id == R.id.btn_previous_knowledge || id == R.id.btn_next_knowledge) {
            position += id == R.id.btn_next_knowledge ? 1 : -1;
            knowledgeId = chapterKnowledge.get(position).optString("knowledgeId");
            showKnowledge();
        } else if (id == R.id.btn_related_quiz) {
            Bundle bundle = new Bundle(); bundle.putInt("chapter_id", chapterId);
            openPage(QuizActivity.class, bundle);
        } else if (id == R.id.btn_return_directory) finish();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state); state.putString("knowledge", knowledgeId);
    }
}
