package com.finstudy.app;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

/** 所有页面共用标题、返回按钮与身份页脚；具体页面仍有独立 XML。 */
public class BaseActivity extends Activity {
    protected Repo repo;
    protected Store store;
    protected final ArrayList<String> lifecycleEvents = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = new Repo(this);
        store = new Store(this);
        recordLifecycle("onCreate");
    }

    protected void setScreen(int layoutId, String title) {
        setContentView(layoutId);
        ((TextView) findViewById(R.id.tv_toolbar_title)).setText(title);
        View back = findViewById(R.id.btn_back);
        if (this instanceof MainActivity) {
            back.setVisibility(View.GONE);
        } else {
            back.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) { finish(); }
            });
        }
        // 对应第三章页面适配：给系统状态栏、导航栏留下空间。
        final View root = findViewById(R.id.screen_root);
        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
                return insets;
            }
        });
        root.requestApplyInsets();
        if (repo.error != null) {
            new AlertDialog.Builder(this).setTitle("课程内容加载失败")
                    .setMessage("请检查 assets 中的课程 JSON 文件。\n" + repo.error)
                    .setPositiveButton("知道了", null).show();
        }
    }

    /** 第五章：显式 Intent。需要传值的页面使用带 Bundle 的重载。 */
    protected void openPage(Class<?> pageClass) {
        Intent intent = new Intent(this, pageClass);
        startActivity(intent);
    }
    protected void openPage(Class<?> pageClass, Bundle data) {
        Intent intent = new Intent(this, pageClass);
        intent.putExtras(data);
        startActivity(intent);
    }
    protected void openKnowledge(String knowledgeId) {
        Bundle bundle = new Bundle();
        bundle.putString("knowledge_id", knowledgeId);
        openPage(KnowledgeDetailActivity.class, bundle);
    }
    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    protected void recordLifecycle(String name) {
        lifecycleEvents.add(name);
        if (lifecycleEvents.size() > 14) lifecycleEvents.remove(0);
        Log.d("FinStudyLifecycle", getClass().getSimpleName() + " : " + name);
    }
    @Override protected void onStart() { super.onStart(); recordLifecycle("onStart"); }
    @Override protected void onResume() { super.onResume(); recordLifecycle("onResume"); }
    @Override protected void onPause() { recordLifecycle("onPause"); super.onPause(); }
    @Override protected void onStop() { recordLifecycle("onStop"); super.onStop(); }
    @Override protected void onRestart() { super.onRestart(); recordLifecycle("onRestart"); }
    @Override protected void onDestroy() {
        recordLifecycle("onDestroy");
        if (store != null) store.close();
        super.onDestroy();
    }
}
