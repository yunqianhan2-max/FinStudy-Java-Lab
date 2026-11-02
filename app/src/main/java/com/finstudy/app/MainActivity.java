package com.finstudy.app;


import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/** 第三章 ConstraintLayout 首页；第四章按钮；第五章显式 Intent。 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreen(R.layout.activity_main, "FinStudy");
        int[] ids = {R.id.btn_continue, R.id.btn_course, R.id.btn_quiz, R.id.btn_lab,
                R.id.btn_navigation, R.id.btn_wrong, R.id.btn_favorites, R.id.btn_statistics};
        for (int id : ids) findViewById(id).setOnClickListener(this);
    }
    @Override protected void onResume() {
        super.onResume();
        int completed = store.completed(repo);
        int total = repo.availableChapters(false).size();
        int percentage = Grading.percentage(completed, total);
        ((TextView) findViewById(R.id.tv_progress)).setText("学习进度  " + completed + " / " + total
                + " 章\n" + percentage + "% 已完成");
        ((ProgressBar) findViewById(R.id.pb_progress)).setProgress(percentage);
        ((TextView) findViewById(R.id.tv_summary)).setText(total + " 章课程 · " + repo.questions.length()
                + " 道练习 · 十个代码运行示例 · 页面跳转与 Fragment\n离线学习，数据保存在本机。");
    }
    @Override public void onClick(View view) {
        // 使用 if 而非 switch：新版 AGP 的 R.id 不保证是编译期常量。
        int id = view.getId();
        if (id == R.id.btn_continue) {
            String last = getSharedPreferences("learning", MODE_PRIVATE).getString("last", "c1k1");
            if (repo.find(repo.knowledge, "knowledgeId", last) == null) last = "c1k1";
            openKnowledge(last);
        } else if (id == R.id.btn_course) openPage(ChapterListActivity.class);
        else if (id == R.id.btn_quiz) openPage(QuizActivity.class);
        else if (id == R.id.btn_lab) openPage(LabActivity.class);
        else if (id == R.id.btn_navigation) openPage(NavigationActivity.class);
        else if (id == R.id.btn_wrong) openPage(WrongBookActivity.class);
        else if (id == R.id.btn_favorites) openPage(FavoritesActivity.class);
        else if (id == R.id.btn_statistics) openPage(StatisticsActivity.class);
    }
}
