package com.finstudy.app;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/** 第五章：Bundle 传值、返回结果、Fragment、生命周期。 */
@SuppressWarnings("deprecation")
public class NavigationActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_REPLY = 1001;
    private String returnedMessage = "尚未收到返回结果";
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setScreen(R.layout.activity_navigation, "页面跳转实验");
        findViewById(R.id.btn_send_message).setOnClickListener(this);
        findViewById(R.id.btn_fragment_guide).setOnClickListener(this);
        findViewById(R.id.btn_fragment_check).setOnClickListener(this);
        if (state == null) showFragment("guide");
        else returnedMessage = state.getString("returned", returnedMessage);
        ((TextView) findViewById(R.id.tv_returned_message)).setText(returnedMessage);
    }
    @Override public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_send_message) {
            String message = ((EditText) findViewById(R.id.et_message)).getText().toString().trim();
            if (message.isEmpty()) { toast("请先输入学习目标"); return; }
            Bundle bundle = new Bundle();
            bundle.putString("message", message);
            bundle.putInt("chapter_id", 5);
            Intent intent = new Intent(NavigationActivity.this, MessageActivity.class);
            intent.putExtras(bundle);
            // 按第五章课件展示旧式返回结果 API；现代项目可改用 Activity Result API。
            startActivityForResult(intent, REQUEST_REPLY);
        } else if (id == R.id.btn_fragment_guide) showFragment("guide");
        else if (id == R.id.btn_fragment_check) showFragment("check");
    }
    private void showFragment(String kind) {
        StudyFragment fragment = new StudyFragment();
        Bundle arguments = new Bundle(); arguments.putString("kind", kind);
        fragment.setArguments(arguments);
        getFragmentManager().beginTransaction().replace(R.id.fragment_host, fragment, kind).commit();
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_REPLY) {
            if (resultCode == RESULT_OK && data != null) {
                returnedMessage = "收到子页面回复：" + data.getStringExtra("reply");
            } else returnedMessage = "子页面已关闭，没有返回回复。";
            ((TextView) findViewById(R.id.tv_returned_message)).setText(returnedMessage);
        }
    }
    @Override protected void onResume() { super.onResume(); showLifecycle(); }
    private void showLifecycle() {
        StringBuilder text = new StringBuilder();
        for (String event : lifecycleEvents) text.append(event).append('\n');
        ((TextView) findViewById(R.id.tv_lifecycle)).setText(text.toString().trim());
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state); state.putString("returned", returnedMessage);
    }
}
