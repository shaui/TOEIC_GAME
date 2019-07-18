package com.example.toeic_game.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.toeic_game.R;
import com.example.toeic_game.util.AutoAdaptImage;
import com.example.toeic_game.util.ToastUtil;

public class StartDialog extends Dialog {

    private Button btn_start;
    private int reqWidth, reqHeight;
    private Context context;
    private int drawable;

    public StartDialog(@NonNull Context context, int drawable) {
        super(context);
        this.context = context;
        this.drawable = drawable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*去除標題欄*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_start_dialog, null, false);
        setBackground(view, drawable);
        setContentView(view);

        setDialogDimension();

        btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showMsg(context, "Game Start");
            }
        });

    }

    private void setDialogDimension(){
        /*用來儲存螢幕(Display)資訊的指標*/
        DisplayMetrics displayMetrics = new DisplayMetrics();

        /*把目前的螢幕資訊放入指標中*/
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
//        int height = displayMetrics.heightPixels;

        /*取得目前window的屬性*/
        WindowManager.LayoutParams wm_lp = getWindow().getAttributes();
        /*改變螢幕寬度*/
        wm_lp.width = (int)(width*0.8);
        wm_lp.height = (int)(width*0.8);
        /*重新設定螢幕屬性*/
        getWindow().setAttributes(wm_lp);
    }

    private void setBackground(final View view, final int drawable){
        view.post(new Runnable() {
            @Override
            public void run() {
                reqWidth = view.getWidth();
                reqHeight = view.getHeight();
                Drawable db = new BitmapDrawable(
                        context.getResources(),
                        AutoAdaptImage.decodeSampledBitmapFromResource(context.getResources(), drawable
                                , reqWidth, reqHeight)
                );
                view.setBackground(db);

            }
        });
    }
}
