package com.example.toeic_game.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.toeic_game.R;
import com.example.toeic_game.util.AutoAdaptImage;

public class StartDialog extends Dialog {

    private Context context;
    private Button btn_start;
    private TextView tv_rule;
    private int drawable;
    private int reqWidth, reqHeight;

    public StartDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

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
        setContentView(view);
        setDialogDimension();

        //點擊邊框外面不會消失
        setCanceledOnTouchOutside(true);

        //要放在setContentView下面，不然會找不到(好像)，除非是設定一些屬性
        tv_rule = findViewById(R.id.tv_rule);
        tv_rule.setText("Rule:" + "\n" + "Please fellow the order of the word to click the corresponding Chinese explanation");
        btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatchDialog matchDialog = new MatchDialog(context);
                matchDialog.show();
                dismiss();
            }
        });
    }

    private void onCreateWithPic(){
        /*去除標題欄*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_start_dialog, null, false);
//        ImageView iv_start = view.findViewById(R.id.iv_start);
//        iv_start.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        Glide.with(context).load(drawable).into(iv_start);
        setContentView(view);

        setDialogDimension();

        btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                MatchDialog matchDialog = new MatchDialog(context, StartDialog.this);
//                matchDialog.show();
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
