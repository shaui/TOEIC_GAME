package com.example.toeic_game.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.toeic_game.Member;
import com.example.toeic_game.R;
import com.example.toeic_game.util.ToastUtil;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NameDialog extends Dialog {

    //firebase
    private DatabaseReference myRef;

    private int reqWidth, reqHeight;
    private Context context;
    private EditText et_name;
    private Button btn_alert;
    private TextView tv_nav_name;
    private FirebaseUser currentUser;

    public NameDialog(@NonNull Context context, TextView tv_nav_name, FirebaseUser currentUser) {
        super(context);
        this.context = context;
        this.tv_nav_name = tv_nav_name;
        this.currentUser = currentUser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*去除標題欄*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_name_dialog, null, false);
        view.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
        setContentView(view);
        setDialogDimension(0.7, 0.4);

        myRef = FirebaseDatabase.getInstance().getReference();

        et_name = findViewById(R.id.et_name);
        btn_alert = findViewById(R.id.btn_alert);
        btn_alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //trim把左右2邊的空個都去除
                if(et_name.getText() != null){
                    final String name = et_name.getText().toString().trim();
                    if(name.isEmpty()){
                        ToastUtil.showMsg(context, "please input your name");
                    }
                    else {
                        tv_nav_name.setText(name);
                        if(currentUser != null){
                            myRef.child("members").child(currentUser.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            //先取得原本的資料
                                            Member member = dataSnapshot.getValue(Member.class);
                                            //改名後在丟上去
                                            member.setName(name);
                                            dataSnapshot.getRef().setValue(member);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                        dismiss();
                    }
                }
                else {
                    ToastUtil.showMsg(context, "please input your name");
                }
            }
        });
    }

    private void setDialogDimension(double w_ratio, double h_ratio){
        /*用來儲存螢幕(Display)資訊的指標*/
        DisplayMetrics displayMetrics = new DisplayMetrics();

        /*把目前的螢幕資訊放入指標中*/
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
//        int height = displayMetrics.heightPixels;

        /*取得目前window的屬性*/
        WindowManager.LayoutParams wm_lp = getWindow().getAttributes();
        /*改變螢幕寬度*/
        wm_lp.width = (int)(width * w_ratio);
        wm_lp.height = (int)(width * h_ratio);
        /*重新設定螢幕屬性*/
        getWindow().setAttributes(wm_lp);
    }
}
