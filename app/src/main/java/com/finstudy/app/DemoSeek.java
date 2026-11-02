package com.finstudy.app;


import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class DemoSeek implements LabExampleBinding {
    @Override
    public void bind(View demoView) {
        final ImageView image = demoView.findViewById(R.id.demo_seek_image);
        final TextView result = demoView.findViewById(R.id.demo_seek_result);
        SeekBar bar = demoView.findViewById(R.id.demo_seek_bar);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                image.setAlpha(progress / 100f);
                result.setText("透明度：" + progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }
}
