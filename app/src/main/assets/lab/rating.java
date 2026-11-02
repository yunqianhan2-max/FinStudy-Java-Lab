package com.finstudy.app;


import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

public class DemoRating implements LabExampleBinding {
    @Override
    public void bind(View demoView) {
        final TextView result = demoView.findViewById(R.id.demo_rating_result);
        RatingBar bar = demoView.findViewById(R.id.demo_rating_bar);
        bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                result.setText("评分：" + rating + " / 5");
            }
        });
    }
}
