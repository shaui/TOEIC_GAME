package com.example.toeic_game.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.toeic_game.GameActivity;
import com.example.toeic_game.Player;
import com.example.toeic_game.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MatchDialog extends Dialog {

    private Button btn_cancel;
    private TextView tv_match_player;
    private Context context;
    private StartDialog startDialog;

    //firebase usage parameter
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private DatabaseReference tempRoomRef = null;
    private Player player_self;
    private String player_self_location;
    private boolean isPlayer1 = false;
    private String roomID;
    private boolean isCancel = false;

    public MatchDialog(@NonNull Context context, StartDialog startDialog) {
        super(context);
        this.context = context;
        this.startDialog = startDialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*去除標題欄*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_match_dialog, null, false);
        setContentView(view);
        setDialogDimension();

        //點擊邊框外面不會消失
        setCanceledOnTouchOutside(false);

        //要放在setContentView下面，不然會找不到(好像)，除非是設定一些屬性
        tv_match_player = findViewById(R.id.tv_match_player);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCancel = true;
                tempRoomRef.removeValue();
                dismiss();
            }
        });
        matchPlayer();
    }

    private void setDialogDimension(){
        /*用來儲存螢幕(Display)資訊的指標*/
        DisplayMetrics displayMetrics = new DisplayMetrics();

        /*把目前的螢幕資訊放入指標中*/
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
//        int height = displayMetrics.heightPixels;

        //設背景為圓框,好像會讓dialog變大
        getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_rectangle);

        /*取得目前window的屬性*/
        WindowManager.LayoutParams wm_lp = getWindow().getAttributes();

        /*改變螢幕寬度*/
        wm_lp.width = (int)(width*0.7);
        wm_lp.height = (int)(width*0.7);
        /*重新設定螢幕屬性*/
        getWindow().setAttributes(wm_lp);

    }

    private void matchPlayer(){
        // get database instance
        database = FirebaseDatabase.getInstance();
        // reference to the node, but doesn't choose which node now.
        myRef = database.getReference();
        player_self = new Player("player_self");
        myRef.child("room").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isSetPlayer = false;
                for(DataSnapshot room_ids: dataSnapshot.getChildren()){
                    //如果沒有player2, 不能用else不然找到有player2就會跑去else
                    if(!room_ids.child("player2").exists()){
                        tempRoomRef = room_ids.getRef();
                        player_self_location = tempRoomRef.child("player2").getKey();
                        tempRoomRef.child("player2").setValue(player_self);

                        isSetPlayer = true;
                        break;
                    }
                }
                //如果沒有加入任何房間，自己開房
                if(!isSetPlayer){
                    tempRoomRef = myRef.child("room").push();
                    player_self_location = tempRoomRef.child("player1").getKey();
                    tempRoomRef.child("player1").setValue(player_self);
                }

                //Set the parameter that will be pass
                if(player_self_location.equals("player1")){
                    isPlayer1 = true;
                }
                else{
                    isPlayer1 = false;
                }
                roomID = tempRoomRef.getKey();

                //搜尋玩家
                tempRoomRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        //當player2不存在
                        if (!dataSnapshot.child("quest").exists()){
                            Log.i("---search---", "search player2");
                        }
                        else {
                            //先關閉之前開的startDialog
                            if(startDialog.isShowing()){
                                startDialog.dismiss();
                            }
                            //關閉matchingDialog
                            dismiss();
                            //先刪除Listener,再跳轉
                            tempRoomRef.removeEventListener(this);
                            Intent intent = new Intent(context, GameActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("roomID", roomID);
                            bundle.putBoolean("isPlayer1", isPlayer1);
                            intent.putExtras(bundle);
                            context.startActivity(intent);
                        }
                        if(dataSnapshot.child("player2").exists()){
                            Log.i("---player1---", dataSnapshot.child("player1").getValue(Player.class).getName());
                            Log.i("---player2---", dataSnapshot.child("player2").getValue(Player.class).getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
