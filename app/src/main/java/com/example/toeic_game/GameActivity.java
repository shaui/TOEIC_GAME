package com.example.toeic_game;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

public class GameActivity extends AppCompatActivity {

    private int questNum = 0, ansAt = 0, ansWrong = 0, time = 0;
    private boolean isPlayer1, hasAI, initialed = false, endGame = false;
    private String[] quest;

    private CountDownView headP1, headP2;
    private ScoreBar scoreBar;
    private TextView timeTextView, questTextView, nameP1, nameP2;
    private Button[] ans = new Button[4];
    private AnimatorSet readyAnim;

    private DatabaseReference roomRef, selfRef, oppoRef;
    private Player self, oppo, player1, player2;

    private TreeMap<String, Integer> randomOptions = new TreeMap<>((String o1, String o2) -> Math.random() > 0.5 ? 1 : -1);
    private Entry<String, Integer>[] optionList = new Entry[4];

    //控制遊戲流程
    private Thread gameThread = new Thread(() -> {
        while(!endGame) {
            getQuest();
            //等待玩家答題完畢
            while(!self.isReady() || !oppo.isReady()) {
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    //虛擬對手
    private Runnable updateAIScore = () -> {
        oppo.setReady(false);
        oppoRef.child("ready").setValue(false);
        double sleepTime = Math.random();
        try {
            Thread.sleep(Math.round(sleepTime * 4000 + 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int AIscoreTemp = (int) Math.round(400 * (1 - sleepTime)) + 500;
        AIscoreTemp -= Math.round(Math.random() * 4) *100;
        oppo.addScore(AIscoreTemp);
        oppo.setReady(true);
        oppoRef.setValue(oppo);
    };

    //執行準備動畫，結束後顯示題目
    private MyCountDownTimer readyClock = new MyCountDownTimer(3000,1000) {
        @Override
        public void onFinish() {
            readyAnim.cancel();
            timeTextView.setScaleX(1);
            timeTextView.setScaleY(1);
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
            for(Button b : ans) {
                b.setAlpha(0.5f);
                b.setEnabled(false);
            }
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
            oppo = dataSnapshot.getValue(Player.class);
            if(initialed && oppo.isReady()) {
                if(isPlayer1)
                    headP2.pause();
                else
                    headP1.pause();
                scoreBar.setScore(!isPlayer1, oppo.getScore());
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
        setComponent();
        readyAnim = (AnimatorSet) AnimatorInflater.loadAnimator(GameActivity.this, R.animator.text_countdown);
        readyAnim.setTarget(timeTextView);
        for(int i = 0; i < 4; i++)
            setAnsButtonEvent(i);
        roomRef.addValueEventListener(detectOppoLeavedListener);
        oppoRef.addValueEventListener(oppoScoreListener);
        setPlayerData();
    }

    @Override
    protected void onDestroy() {
        if(hasAI)
            roomRef.removeValue();
        else
            selfRef.removeValue();
        endGame = true;
        self.setReady(true);
        oppo.setReady(true);
        oppoRef.removeEventListener(oppoScoreListener);
        roomRef.removeEventListener(detectOppoLeavedListener);
        readyClock.cancel();
        roundTimeClock.cancel();
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //遊戲結束時點擊螢幕跳回主畫面
        if(endGame) {
            finish();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setComponent() {
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
        questTextView = findViewById(R.id.quest);
    }

    //設置按鈕事件
    private void setAnsButtonEvent(int num) {
        final Button b = ans[num];
        b.setAlpha(0.5f);
        b.setEnabled(false);
        b.setOnClickListener((View v) -> {
                if(ansAt == optionList[num].getValue()) {
                    ansAt++;
                    b.setEnabled(false);
                    b.setAlpha(0.5f);
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
                player1 = dataSnapshot.child("player1").getValue(Player.class);
                player2 = dataSnapshot.child("player2").getValue(Player.class);
                if(isPlayer1) {
                    self = player1;
                    oppo = player2;
                }
                else {
                    self = player2;
                    oppo = player1;
                }
                nameP1.setText(player1.getName());
                nameP2.setText(player2.getName());
                //設置頭像
                Glide.with(GameActivity.this).load(R.drawable.icon_image_head).into(headP1);
                Glide.with(GameActivity.this).load(R.drawable.icon_image_head).into(headP2);
                gameThread.start();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    //回合開始，先抓完題目才準備開始
    private void getQuest() {
        self.setReady(false);
        selfRef.child("ready").setValue(false);
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
            ans[i].setAlpha(1.0f);
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
        for(Button b : ans)
            b.setText("");
        questTextView.setText("");
        Log.i("P1 remaining time ", String.valueOf(headP1.getRemainingTime()));
        self.addScore(countScore());
        self.setReady(true);
        scoreBar.setScore(isPlayer1, self.getScore());
        selfRef.setValue(self);
        ansAt = 0;
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
        oppoRef.removeEventListener(oppoScoreListener);
        roomRef.removeEventListener(detectOppoLeavedListener);
        roomRef.removeValue();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            headP1.pause();
            headP2.pause();
            headP1.setRingVisible(false);
            headP2.setRingVisible(false);
        });
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
            headP1.startAnimation(endingAnimP1);
            headP2.startAnimation(endingAnimP2);
        });
        //等待動畫結束
        while(true) {
            if(endingAnimP1.hasEnded() && endingAnimP2.hasEnded()) {
                endGame = true;
                break;
            }
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
