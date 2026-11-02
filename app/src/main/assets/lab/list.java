package com.finstudy.app;


import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DemoList implements LabExampleBinding {
    @Override
    public void bind(View demoView) {
        ListView list = demoView.findViewById(R.id.demo_list);
        final TextView result = demoView.findViewById(R.id.demo_list_result);
        String[] widgets = {"TextView", "Button", "SeekBar", "Spinner"};
        list.setAdapter(new ArrayAdapter<String>(demoView.getContext(),
                android.R.layout.simple_list_item_1, widgets));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                result.setText("点击了：" + parent.getItemAtPosition(position));
            }
        });
    }
}
