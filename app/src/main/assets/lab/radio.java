package com.finstudy.app;


import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class DemoRadio implements LabExampleBinding {
    @Override
    public void bind(final View demoView) {
        RadioGroup group = demoView.findViewById(R.id.demo_radio_group);
        final TextView result = demoView.findViewById(R.id.demo_radio_result);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == -1) return;
                RadioButton selected = demoView.findViewById(checkedId);
                result.setText("当前选择：" + selected.getText());
            }
        });
    }
}
