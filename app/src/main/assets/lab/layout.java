package com.finstudy.app;


import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DemoLayout implements LabExampleBinding {
    @Override
    public void bind(View demoView) {
        final LinearLayout row = demoView.findViewById(R.id.demo_layout_row);
        final Button toggle = demoView.findViewById(R.id.demo_layout_toggle);
        final TextView state = demoView.findViewById(R.id.demo_layout_state);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean horizontal = row.getOrientation() == LinearLayout.HORIZONTAL;
                row.setOrientation(horizontal ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
                state.setText(horizontal ? "当前：vertical 纵向排列" : "当前：horizontal 横向排列");
                toggle.setText(horizontal ? "切换为横向排列" : "切换为纵向排列");
            }
        });
    }
}
