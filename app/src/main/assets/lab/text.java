package com.finstudy.app;


import android.view.View;
import android.widget.TextView;

/** demoView 是 demo_lab_text.xml 加载后的根 View。 */
public class DemoText implements LabExampleBinding {
    @Override
    public void bind(View demoView) {
        TextView message = demoView.findViewById(R.id.demo_text_message);
        message.setText("欢迎学习 Android！");
        message.setTextSize(24);
        message.setTextColor(0xFFB53B70);
    }
}
