package com.finstudy.app;


import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import org.json.JSONObject;
import java.util.ArrayList;

public class FavoritesActivity extends BaseActivity {
    private ListView list;
    private ArrayList<JSONObject> favorites;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setScreen(R.layout.activity_favorites, "重点收藏");
        list = findViewById(R.id.lv_favorites);
        list.setEmptyView(findViewById(R.id.tv_favorites_empty));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openKnowledge(favorites.get(position).optString("knowledgeId"));
            }
        });
    }
    @Override protected void onResume() {
        super.onResume();
        favorites = new ArrayList<JSONObject>();
        for (JSONObject item : repo.items(repo.knowledge, 0)) {
            if (store.favorite(item.optString("knowledgeId"))) favorites.add(item);
        }
        list.setAdapter(new KnowledgeAdapter(this, favorites, repo, store));
    }
}
