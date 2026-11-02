package com.finstudy.app;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.json.JSONObject;
import java.util.ArrayList;

/** 第四章 BaseAdapter 四个方法，配合自定义 item_knowledge.xml。 */
public class KnowledgeAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<JSONObject> items;
    private final Repo repo;
    private final Store store;
    public KnowledgeAdapter(Context context, ArrayList<JSONObject> items, Repo repo, Store store) {
        this.context = context; this.items = items; this.repo = repo; this.store = store;
    }
    @Override public int getCount() { return items.size(); }
    @Override public JSONObject getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return position; }
    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) row = LayoutInflater.from(context).inflate(R.layout.item_knowledge, parent, false);
        JSONObject item = getItem(position);
        String id = item.optString("knowledgeId");
        ((TextView) row.findViewById(R.id.row_title)).setText(item.optString("title"));
        String[] states = {"未学习", "学习中", "已掌握"};
        String description = repo.chapter(item.optInt("chapterId")) + " · " + states[store.status(id)];
        if (store.favorite(id)) description += " · 已收藏";
        ((TextView) row.findViewById(R.id.row_subtitle)).setText(description);
        return row;
    }
}
