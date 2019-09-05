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

import androidx.annotation.NonNull;

import com.example.toeic_game.R;
import com.example.toeic_game.util.ToastUtil;
import com.google.firebase.storage.UploadTask;

public class LoadingDialog extends Dialog {


    private Context context;
    private Button btn_cancel;
    private UploadTask uploadTask;

    public LoadingDialog(@NonNull Context context, UploadTask uploadTask) {
        super(context);
        this.context = context;
        this.uploadTask = uploadTask;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*去除標題欄*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_loading_dialog, null, false);
        view.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
        setContentView(view);
        setDialogDimension(0.7, 0.7);

        //點擊邊框外面不會消失
        setCanceledOnTouchOutside(false);

        btn_cancel = findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadTask.cancel();
                ToastUtil.showMsg(context, "Cancel");
                dismiss();
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
