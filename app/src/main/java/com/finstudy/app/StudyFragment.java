package com.finstudy.app;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/** 课堂 Fragment 形式：继承、重写 onCreateView、inflate 独立 XML。
 * 使用平台 Fragment 以避免引入 Kotlin 依赖；现代生产项目应选 AndroidX Fragment。
 */
@SuppressWarnings("deprecation")
public class StudyFragment extends Fragment {
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = inflater.inflate(R.layout.fragment_study, container, false);
        String kind = getArguments() == null ? "guide" : getArguments().getString("kind", "guide");
        TextView title = view.findViewById(R.id.tv_fragment_title);
        TextView body = view.findViewById(R.id.tv_fragment_body);
        if ("check".equals(kind)) {
            title.setText("自查清单 Fragment");
            body.setText("1. 目标 Activity 是否在清单声明？\n2. Bundle 的键是否一致？\n3. 返回前是否 setResult？\n4. Fragment 是否由宿主管理器添加？");
        } else {
            title.setText("学习步骤 Fragment");
            body.setText("1. 输入目标并打开另一个页面。\n2. 在子页面填写回复并返回。\n3. 切换这两个局部页面。\n4. 查看生命周期记录及 Logcat。");
        }
        return view;
    }
}
