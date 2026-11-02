package com.finstudy.app;


import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class DemoCheck implements LabExampleBinding {
    @Override
    public void bind(View demoView) {
        final CheckBox[] boxes = {
            demoView.findViewById(R.id.demo_check_java),
            demoView.findViewById(R.id.demo_check_xml),
            demoView.findViewById(R.id.demo_check_sqlite)
        };
        final TextView result = demoView.findViewById(R.id.demo_check_result);
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                StringBuilder text = new StringBuilder();
                for (CheckBox box : boxes) {
                    if (box.isChecked()) text.append(box.getText()).append(' ');
                }
                result.setText("已选：" + (text.length() == 0 ? "无" : text.toString().trim()));
            }
        };
        for (CheckBox box : boxes) box.setOnCheckedChangeListener(listener);
    }
}
