package com.example.toeic_game;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class GameActivity extends AppCompatActivity {
    private CountDownView cdv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        cdv = findViewById(R.id.iv_head);
    }

    @Override
    protected void onStart() {
        super.onStart();
        cdv.setBackgroundResource(R.drawable.icon_image_head);
        cdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cdv.start(false);
            }
        });
    }
}
