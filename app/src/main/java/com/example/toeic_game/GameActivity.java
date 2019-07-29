package com.example.toeic_game;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    private int time;
    private CountDownView cdv;
    private TextView text;
    private Button b;
    private CountDownTimer clock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        cdv = findViewById(R.id.p1_head);
        b = findViewById(R.id.ans1);
        text = findViewById(R.id.time);
    }

    @Override
    protected void onStart() {
        super.onStart();
        cdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cdv.start(false);
            }
        });
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ready();
            }
        });
    }

    public void Ready() {
        time = 3;
        final AnimatorSet animation = (AnimatorSet) AnimatorInflater.loadAnimator(GameActivity.this, R.animator.text_countdown);
        animation.setTarget(text);
        if(clock != null)
            clock.cancel();
        clock = new CountDownTimer(3100,1000) {
            @Override
            public void onFinish() {
                text.setScaleX(1.0f);
                text.setScaleY(1.0f);
                text.setText("Start!!");
                clock.cancel();
            }

            @Override
            public void onTick(long millisUntilFinished) {
                text.setText(String.valueOf(time));
                animation.start();
                time -= 1;
            }
        };
        clock.start();
    }

}
