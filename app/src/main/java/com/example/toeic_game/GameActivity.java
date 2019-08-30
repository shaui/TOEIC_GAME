package com.example.toeic_game;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.TreeMap;

public class GameActivity extends AppCompatActivity {

    private int time, questNum = 0, ansAt = 0, score = 0;
    private boolean isPlayer1, selfIsReady = false, oppoIsReady = false, initialed = false, endGame = false;
    private String[] quest;

    private Thread gameThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(!endGame) {
                getQuest();
                while(!oppoIsReady || !selfIsReady) {
                    try {
                        Thread.sleep(100);
                        Log.i("GameThread", "wait");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                selfIsReady = false;
                oppoIsReady = false;
            }
        }
    });

    private CountDownView headP1, headP2;
    private ScoreBar scoreBar;
    private TextView timeTextView, questTextView;
    private CountDownTimer clock;
    private Button[] ans = new Button[4];

    private DatabaseReference roomRef, selfRef, oppoRef;
    private ValueEventListener oppoScoreListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(initialed && dataSnapshot.getValue(Integer.class) != null) {
                if(isPlayer1) {
                    headP2.pause();
                }
                else {
                    headP1.pause();
                }
                scoreBar.setScore(!isPlayer1, dataSnapshot.getValue(Integer.class) );
                oppoIsReady = true;
            }
            initialed = true;
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };
    private Player self, oppo;

    private TreeMap<String, Integer> randomOptions = new TreeMap<>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return Math.random() > 0.5 ? 1 : -1;
        }
    });
    private Entry<String, Integer>[] optionList = new Entry[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Bundle bundle = getIntent().getExtras();
        try {
            isPlayer1 = bundle.getBoolean("isPlayer1");
            roomRef = FirebaseDatabase.getInstance().getReference().child("room/" + bundle.getString("roomID"));
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
        if(isPlayer1) {
            selfRef = roomRef.child("player1");
            oppoRef = roomRef.child("player2");
        }
        else {
            selfRef = roomRef.child("player2");
            oppoRef = roomRef.child("player1");
        }
        headP1 = findViewById(R.id.p1_head);
        headP2 = findViewById(R.id.p2_head);
        scoreBar = findViewById(R.id.score_bar);
        ans[0] = findViewById(R.id.ans1);
        ans[1] = findViewById(R.id.ans2);
        ans[2] = findViewById(R.id.ans3);
        ans[3] = findViewById(R.id.ans4);
        timeTextView = findViewById(R.id.time);
        questTextView = findViewById(R.id.quest);
        setPlayerData();
        for(int i = 0; i < 4; i++) {
            setAnsButtonEvent(i);
        }
        oppoRef.child("score").addValueEventListener(oppoScoreListener);
        gameThread.start();
    }

    @Override
    protected void onDestroy() {
        selfRef.removeValue();
        endGame = true;
        selfIsReady = true;
        oppoIsReady = true;
        oppoRef.child("score").removeEventListener(oppoScoreListener);
        super.onDestroy();
    }

    private void setAnsButtonEvent(final int num) {
        ans[num].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 有可能出現問題
                if(ansAt == optionList[num].getValue()) {
                    ansAt++;
                    ans[num].setEnabled(false);
                    nextRoundCheck();
                }
                else {
                }
            }
        });
    }

    //我覺得現在可以先不用
    private void setPlayerData() {
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(isPlayer1) {
                    self = dataSnapshot.child("player1").getValue(Player.class);
                    oppo = dataSnapshot.child("player2").getValue(Player.class);
                }
                else {
                    self = dataSnapshot.child("player2").getValue(Player.class);
                    oppo = dataSnapshot.child("player1").getValue(Player.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {};
        });
        Glide.with(GameActivity.this).load(R.drawable.icon_image_head).into(headP1);
        Glide.with(GameActivity.this).load(R.drawable.icon_image_head).into(headP2);
    }

    //回合開始 先抓完題目才準備開始
    private void getQuest() {
        if(questNum > 4) {
            int[] tempScore = scoreBar.getScore();
            if(tempScore[0] > tempScore[1]) {
                ending(true);
            }
            else
                ending(false);
            roomRef.removeValue();
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            this.startActivity(intent);
            this.finish();
            return;
        }
        roomRef.child("/quest/" + questNum++).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                ArrayList<String> tempQuest = new ArrayList<>();
                for(DataSnapshot option : dataSnapshot.getChildren()) {
                    tempQuest.add(option.getKey());
                    randomOptions.put((String) option.getValue(), i++);
                }
                quest = new String[tempQuest.size()];
                quest = tempQuest.toArray(quest);
                Ready();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {};
        });
    }

    private void Ready() {
        time = 3;
        final AnimatorSet animation = (AnimatorSet) AnimatorInflater.loadAnimator(GameActivity.this, R.animator.text_countdown);
        animation.setTarget(timeTextView);
        if(clock != null)
            clock.cancel();
        clock = new CountDownTimer(3100,1000) {
            @Override
            public void onFinish() {
                timeTextView.setScaleX(1.0f);
                timeTextView.setScaleY(1.0f);
                timeTextView.setText("Start!!");
                displayQuest();
                headP1.start(false);
                headP2.start(false);
                clock.cancel();
            }

            @Override
            public void onTick(long millisUntilFinished) {
                timeTextView.setText(String.valueOf(time));
                animation.start();
                time -= 1;
            }
        };
        clock.start();
    }

    //正式開始
    private void displayQuest() {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < 4; i++) {
            builder.append(quest[i]);
            if(i != 3) {
                builder.append("\n");
            }
            optionList[i] = randomOptions.pollFirstEntry();
            ans[i].setText(optionList[i].getKey());
            ans[i].setEnabled(true);
        }
        questTextView.setText(builder.toString());
    }

    //分數處理也在這裡
    private void nextRoundCheck() {
        if(ansAt == 4) {
            ansAt = 0;
            if(isPlayer1)
                headP1.pause();
            else
                headP2.pause();
            score += 10; // 暫時有的分數
            scoreBar.setScore(isPlayer1, score);
            selfRef.child("score").setValue(score);
            selfIsReady = true;
        }
    }

    private void ending(boolean p1Win) {
        oppoRef.child("score").removeEventListener(oppoScoreListener);
        endGame = true;
        selfIsReady = true;
        oppoIsReady = true;
    }

}
