package com.finstudy.app;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/** 第五章：接收 Bundle，设置结果，再 finish() 返回调用者。 */
public class MessageActivity extends BaseActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setScreen(R.layout.activity_message, "接收并返回数据");
        Bundle bundle = getIntent().getExtras();
        String message = bundle == null ? "未收到数据" : bundle.getString("message", "未收到数据");
        int chapterId = bundle == null ? 0 : bundle.getInt("chapter_id", 0);
        ((TextView) findViewById(R.id.tv_received_message)).setText("来自第 " + chapterId + " 章实验\n学习目标：" + message);
        findViewById(R.id.btn_return_reply).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                String reply = ((EditText) findViewById(R.id.et_reply)).getText().toString().trim();
                if (reply.isEmpty()) { toast("请先填写回复"); return; }
                Intent result = new Intent();
                Bundle data = new Bundle(); data.putString("reply", reply);
                result.putExtras(data);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }
}
