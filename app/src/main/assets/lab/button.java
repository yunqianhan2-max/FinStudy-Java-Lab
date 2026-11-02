package com.finstudy.app;


import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DemoButton implements LabExampleBinding {
    @Override
    public void bind(View demoView) {
        Button button = demoView.findViewById(R.id.demo_button_action);
        final TextView message = demoView.findViewById(R.id.demo_button_message);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message.setText("欢迎学习 Android！");
            }
        });
    }
}
