package com.finstudy.app;


import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class DemoGrid implements LabExampleBinding {
    @Override
    public void bind(View demoView) {
        GridView grid = demoView.findViewById(R.id.demo_grid);
        final TextView result = demoView.findViewById(R.id.demo_grid_result);
        String[] items = {"布局", "按钮", "文本", "图片", "列表", "事件"};
        grid.setAdapter(new ArrayAdapter<String>(demoView.getContext(),
                android.R.layout.simple_list_item_1, items));
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                result.setText("点击了：" + parent.getItemAtPosition(position));
            }
        });
    }
}
