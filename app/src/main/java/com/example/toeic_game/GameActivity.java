package com.example.toeic_game;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.toeic_game.widget.CountDownView;
import com.example.toeic_game.widget.MyCountDownTimer;
import com.example.toeic_game.widget.ScoreBar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

public class GameActivity extends AppCompatActivity {

    private int questNum = 0, ansAt = 0, score = 0, AIscore = 0, ansWrong = 0, time = 0;
    private boolean isPlayer1, hasAI, selfIsReady = false, oppoIsReady = false, initialed = false, endGame = false;
    private String[] quest;

    private CountDownView headP1, headP2;
    private ScoreBar scoreBar;
    private TextView timeTextView, questTextView, nameP1, nameP2;
    private Button[] ans = new Button[4];
    private AnimatorSet readyAnim;

    private DatabaseReference roomRef, selfRef, oppoRef;
    private Player self, oppo;

    private TreeMap<String, Integer> randomOptions = new TreeMap<>((String o1, String o2) -> Math.random() > 0.5 ? 1 : -1);
    private Entry<String, Integer>[] optionList = new Entry[4];

    //控制遊戲流程
    private Thread gameThread = new Thread(() -> {
        while(!endGame) {
            getQuest();
            //等待玩家答題完畢
            while(!oppoIsReady || !selfIsReady) {
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            selfIsReady = false;
            oppoIsReady = false;
        }
    });

    //虛擬對手
    private Runnable updateAIScore = () -> {
        double sleepTime = Math.random();
        try {
            Thread.sleep(Math.round(sleepTime * 4000 + 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int AIscoreTemp = (int) Math.round(400 * (1 - sleepTime)) + 500;
        AIscoreTemp -= Math.round(Math.random() * 4) *100;
        AIscore += AIscoreTemp;
        oppoRef.child("score").setValue(AIscore);
    };

    //執行準備動畫，結束後顯示題目
    private MyCountDownTimer readyClock = new MyCountDownTimer(3000,1000) {
        @Override
        public void onFinish() {
            readyAnim.cancel();
            //復原文字大小
            timeTextView.setScaleX(1.0f);
            timeTextView.setScaleY(1.0f);
            timeTextView.setText(R.string.game_readyText);
            displayQuest();
            headP1.start(false);
            headP2.start(false);
            roundTimeClock.start();
            if(hasAI)
                new Thread(updateAIScore).start();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            timeTextView.setText(String.valueOf(millisUntilFinished / 1000));
            /* 動畫在下次執行前要取消掉
            不然在執行完之前 無法再次執行
             不會報錯 可是會看不見 */
            readyAnim.cancel();
            readyAnim.start();
        }
    };

    //5秒過後強制結算分數
    private MyCountDownTimer roundTimeClock = new MyCountDownTimer(5000, 100) {
        @Override
        public void onFinish() {
            for(int i = 0; i < 4; i++)
                ans[i].setEnabled(false);
            nextRound();
        }

        @Override
        public void onTick(long millisLeft) {
            time = (int) millisLeft;
        }
    };

    //更新對手分數
    private ValueEventListener oppoScoreListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(initialed && dataSnapshot.getValue(Integer.class) != null) {
                if(isPlayer1)
                    headP2.pause();
                else
                    headP1.pause();
                scoreBar.setScore(!isPlayer1, dataSnapshot.getValue(Integer.class) );
                oppoIsReady = true;
            }
            initialed = true;
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };


    //確認玩家都在房間內
    private ValueEventListener detectOppoLeavedListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(!dataSnapshot.child("player1").exists() || !dataSnapshot.child("player2").exists())
                ending(isPlayer1);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Bundle bundle = getIntent().getExtras();
        isPlayer1 = bundle.getBoolean("isPlayer1");
        hasAI = bundle.getBoolean("isAI");
        roomRef = FirebaseDatabase.getInstance().getReference().child("room/" + bundle.getString("roomID"));
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
        nameP1 = findViewById(R.id.p1_name);
        nameP2 = findViewById(R.id.p2_name);
        scoreBar = findViewById(R.id.score_bar);
        ans[0] = findViewById(R.id.ans1);
        ans[1] = findViewById(R.id.ans2);
        ans[2] = findViewById(R.id.ans3);
        ans[3] = findViewById(R.id.ans4);
        timeTextView = findViewById(R.id.time);
        readyAnim = (AnimatorSet) AnimatorInflater.loadAnimator(GameActivity.this, R.animator.text_countdown);
        readyAnim.setTarget(timeTextView);
        questTextView = findViewById(R.id.quest);
        setPlayerData();
        for(int i = 0; i < 4; i++) {
            ans[i].setEnabled(false);
            setAnsButtonEvent(i);
        }
        roomRef.addValueEventListener(detectOppoLeavedListener);
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
        roomRef.removeEventListener(detectOppoLeavedListener);
        readyClock.cancel();
        roundTimeClock.cancel();
        super.onDestroy();
    }

    //設置按鈕事件
    private void setAnsButtonEvent(final int num) {
        ans[num].setOnClickListener((View v) -> {
                if(ansAt == optionList[num].getValue()) {
                    ansAt++;
                    ans[num].setEnabled(false);
                    if(ansAt == 4)
                        nextRound();
                }
                else
                    ansWrong++;
        });
    }

    //設定玩家資料
    private void setPlayerData() {
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(isPlayer1) {
                    self = dataSnapshot.child("player1").getValue(Player.class);
                    nameP1.setText(self.getName());
                    oppo = dataSnapshot.child("player2").getValue(Player.class);
                    nameP2.setText(oppo.getName());
                }
                else {
                    self = dataSnapshot.child("player2").getValue(Player.class);
                    nameP2.setText(self.getName());
                    oppo = dataSnapshot.child("player1").getValue(Player.class);
                    nameP1.setText(oppo.getName());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        //設置頭像
        Glide.with(GameActivity.this).load(R.drawable.icon_image_head).into(headP1);
        Glide.with(GameActivity.this).load(R.drawable.icon_image_head).into(headP2);
    }

    //回合開始，先抓完題目才準備開始
    private void getQuest() {
        if(questNum > 4) {
            int[] tempScore = scoreBar.getScore();
            if(tempScore[0] > tempScore[1])
                ending(true);
            else
                ending(false);
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
                readyClock.start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    //顯示題目
    private void displayQuest() {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < ans.length; i++) {
            builder.append(quest[i]);
            //最後一次不換行
            if(i != ans.length - 1)
                builder.append("\n");
            optionList[i] = randomOptions.pollFirstEntry();
            ans[i].setText(optionList[i].getKey());
            ans[i].setEnabled(true);
        }
        questTextView.setText(builder.toString());
    }

    //回合結束，分數上傳
    private void nextRound() {
        roundTimeClock.cancel();
        if(isPlayer1)
            headP1.pause();
        else
            headP2.pause();
        Log.i("P1 remaining time ", String.valueOf(headP1.getRemainingTime()));
        score += countScore();
        scoreBar.setScore(isPlayer1, score);
        selfRef.child("score").setValue(score);
        ansAt = 0;
        selfIsReady = true;
    }

    //計算分數
    private int countScore() {
        int tempScore = 0;
        if(ansWrong > 0)
            tempScore = ansWrong * -100;
        else if(ansAt == 4)
            tempScore = 200;
        if(ansAt == 4)
            tempScore += 500 * time / 5000 + 500;
        if(tempScore < 0)
            tempScore = 0;
        return tempScore;
    }

    private void ending(boolean p1Win) {
        roomRef.removeEventListener(detectOppoLeavedListener);
        oppoRef.child("score").removeEventListener(oppoScoreListener);
        endGame = true;
        selfIsReady = true;
        oppoIsReady = true;
        roomRef.removeValue();
        Handler handler = new Handler(Looper.getMainLooper());
        Animation endingAnimP1, endingAnimP2;
        if(p1Win) {
            handler.post(() -> headP1.bringToFront());
            endingAnimP1 = AnimationUtils.loadAnimation(this, R.anim.ending_win_p1);
            endingAnimP2 = AnimationUtils.loadAnimation(this, R.anim.ending_lose);
        }
        else {
            handler.post(() -> headP2.bringToFront());
            endingAnimP1 = AnimationUtils.loadAnimation(this, R.anim.ending_lose);
            endingAnimP2 = AnimationUtils.loadAnimation(this, R.anim.ending_win_p2);
        }
        handler.post(() -> {
            headP1.pause();
            headP2.pause();
            headP1.startAnimation(endingAnimP1);
            headP2.startAnimation(endingAnimP2);
        });
    }

}
