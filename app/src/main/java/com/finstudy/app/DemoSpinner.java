package com.finstudy.app;


import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class DemoSpinner implements LabExampleBinding {
    @Override
    public void bind(View demoView) {
        Spinner spinner = demoView.findViewById(R.id.demo_spinner);
        final TextView result = demoView.findViewById(R.id.demo_spinner_result);
        String[] topics = {"Java 基础", "XML 布局", "Android 控件"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(demoView.getContext(),
                android.R.layout.simple_spinner_item, topics);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                result.setText("已选择：" + parent.getItemAtPosition(position));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
}
