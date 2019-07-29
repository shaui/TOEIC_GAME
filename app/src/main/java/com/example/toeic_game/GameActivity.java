package com.example.toeic_game;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameActivity extends AppCompatActivity {

    private int time;
    private float textScale = 1;
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
        text.setText("3");
        text.setTextSize(72);
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

    private void Ready() {
        if(clock != null) {
            clock.cancel();
        }
        textScale = 1;
        time = 3;
        text.setText("3");
        clock = new CountDownTimer(3100,40) {

            @Override
            public void onFinish() {
                text.setText("Start!!");
                text.setTextSize(36);
            }

            @Override
            public void onTick(long millisUntilFinished) {
                setText();
            }


        };
        clock.start();
    }

    private void setText() {
        if (textScale > 0) {
            textScale -= 0.04;
            System.out.println("a");
        }
        else {
            textScale = 1;
            if (time > 1) {
                time--;
                text.setText(String.valueOf(time));
            }
        }
        text.setTextSize(textScale * 72);
    }

}
