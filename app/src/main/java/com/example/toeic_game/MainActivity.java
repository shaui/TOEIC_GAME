package com.example.toeic_game;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.toeic_game.util.AutoAdaptImage;
import com.example.toeic_game.widget.StartDialog;
import com.makeramen.roundedimageview.RoundedImageView;

public class MainActivity extends AppCompatActivity {

    private RoundedImageView riv_game_1, riv_game_2, riv_game_3,
                            riv_game_4, riv_game_5, riv_image_head;
    private Button btn_navigation_bar;
    private int reqWidth, reqHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        riv_game_1 = findViewById(R.id.riv_gmae_1);
        riv_game_2 = findViewById(R.id.riv_gmae_2);
        riv_game_3 = findViewById(R.id.riv_gmae_3);
        riv_game_4 = findViewById(R.id.riv_gmae_4);
        riv_game_5 = findViewById(R.id.riv_gmae_5);
        riv_image_head = findViewById(R.id.riv_img_head);
        btn_navigation_bar = findViewById(R.id.btn_navigation_bar);
        //setImages
        setImage(riv_game_1, R.drawable.bg_game_1);
        setImage(riv_game_2, R.drawable.bg_game_2);
        setImage(riv_game_3, R.drawable.bg_game_3);
        setImage(riv_game_4, R.drawable.bg_game_4);
        setImage(riv_game_5, R.drawable.bg_game_5);
        setImage(riv_image_head, R.drawable.icon_image_head);
        setBackground(btn_navigation_bar, R.drawable.icon_navigation_bar);

        //setListener
        StartOnclick startOnclick = new StartOnclick();
        riv_game_1.setOnClickListener(startOnclick);
        riv_game_2.setOnClickListener(startOnclick);
        riv_game_3.setOnClickListener(startOnclick);
        riv_game_4.setOnClickListener(startOnclick);
        riv_game_5.setOnClickListener(startOnclick);

        //setListener
        btn_navigation_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    class StartOnclick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){

                case R.id.riv_gmae_1:
                    StartDialog startDialog1 = new StartDialog(MainActivity.this, R.drawable.bg_start_game1);
                    startDialog1.show();
                    break;
                case R.id.riv_gmae_2:
                    StartDialog startDialog2 = new StartDialog(MainActivity.this, R.drawable.bg_start_game2);
                    startDialog2.show();
                    break;
                case R.id.riv_gmae_3:
                    StartDialog startDialog3 = new StartDialog(MainActivity.this, R.drawable.bg_start_game3);
                    startDialog3.show();
                    break;
                case R.id.riv_gmae_4:
                    StartDialog startDialog4 = new StartDialog(MainActivity.this, R.drawable.bg_start_game4);
                    startDialog4.show();
                    break;
                case R.id.riv_gmae_5:
                    StartDialog startDialog5 = new StartDialog(MainActivity.this, R.drawable.bg_start_game5);
                    startDialog5.show();
                    break;
            }
        }
    }


 /***************** method 1 *****************/
 /*but use the final, so the view cannot change*/
    public void setImage(final View view, final int drawable){
        //post是為了在繪製前取得width, height
        view.post(new Runnable() {
            @Override
            public void run() {
                reqWidth = view.getWidth();
                reqHeight = view.getHeight();
                ((ImageView) view).setImageBitmap(AutoAdaptImage.decodeSampledBitmapFromResource(getResources(), drawable,
                        reqWidth, reqHeight));
                ((ImageView) view).setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        });
    }

    public void setBackground(final View view, final int drawable){
        view.post(new Runnable() {
            @Override
            public void run() {
                reqWidth = view.getWidth();
                reqHeight = view.getHeight();
                Drawable db = new BitmapDrawable(
                        getResources(),
                        AutoAdaptImage.decodeSampledBitmapFromResource(getResources(), drawable
                                , reqWidth, reqHeight)
                );
                view.setBackground(db);

            }
        });
    }
/***************** method 2 *****************/
//    public void setImages(){
//        /*用post是為了在繪製圖片前，先取得元件的width, height*/
//        riv_game_1.post(new Runnable() {
//            @Override
//            public void run() {
//                reqWidth = riv_game_1.getWidth();
//                reqHeight = riv_game_1.getHeight();
//                riv_game_1.setImageBitmap(AutoAdaptImage.decodeSampledBitmapFromResource(getResources(), R.drawable.bg_game_1,
//                        reqWidth, reqHeight));
//                riv_game_1.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            }
//        });
//
//        riv_game_2.post(new Runnable() {
//            @Override
//            public void run() {
//                reqWidth = riv_game_2.getWidth();
//                reqHeight = riv_game_2.getHeight();
//                riv_game_2.setImageBitmap(AutoAdaptImage.decodeSampledBitmapFromResource(getResources(), R.drawable.bg_game_2,
//                        reqWidth, reqHeight));
//                riv_game_2.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            }
//        });
//
//        riv_game_3.post(new Runnable() {
//            @Override
//            public void run() {
//                reqWidth = riv_game_3.getWidth();
//                reqHeight = riv_game_3.getHeight();
//                riv_game_3.setImageBitmap(AutoAdaptImage.decodeSampledBitmapFromResource(getResources(), R.drawable.bg_game_3,
//                        reqWidth, reqHeight));
//                riv_game_3.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            }
//        });
//
//        riv_game_4.post(new Runnable() {
//            @Override
//            public void run() {
//                reqWidth = riv_game_4.getWidth();
//                reqHeight = riv_game_4.getHeight();
//                riv_game_4.setImageBitmap(AutoAdaptImage.decodeSampledBitmapFromResource(getResources(), R.drawable.bg_game_4,
//                        reqWidth, reqHeight));
//                riv_game_4.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            }
//        });
//
//        riv_game_5.post(new Runnable() {
//            @Override
//            public void run() {
//                reqWidth = riv_game_5.getWidth();
//                reqHeight = riv_game_5.getHeight();
//                riv_game_5.setImageBitmap(AutoAdaptImage.decodeSampledBitmapFromResource(getResources(), R.drawable.bg_game_5,
//                        reqWidth, reqHeight));
//                riv_game_5.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            }
//        });
//
//        riv_image_head.post(new Runnable() {
//            @Override
//            public void run() {
//                reqWidth = riv_image_head.getWidth();
//                reqHeight = riv_image_head.getHeight();
//                riv_image_head.setImageBitmap(AutoAdaptImage.decodeSampledBitmapFromResource(getResources(), R.drawable.icon_image_head,
//                        reqWidth, reqHeight));
//                riv_image_head.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            }
//        });
//
//        btn_navigation_bar.post(new Runnable() {
//            @Override
//            public void run() {
//                reqWidth = btn_navigation_bar.getWidth();
//                reqHeight = btn_navigation_bar.getHeight();
//                Drawable drawable = new BitmapDrawable(
//                        getResources(),
//                        AutoAdaptImage.decodeSampledBitmapFromResource(getResources(), R.drawable.icon_navigation_bar
//                                , reqWidth, reqHeight)
//                );
//                btn_navigation_bar.setBackground(drawable);
//            }
//        });
//    }

/***************** method 3 *****************/
    //    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//
//        setImage(riv_game_1, R.drawable.bg_game_1);
//        setImage(riv_game_2, R.drawable.bg_game_2);
//        setImage(riv_game_3, R.drawable.bg_game_3);
//        setImage(riv_game_4, R.drawable.bg_game_4);
//        setImage(riv_game_5, R.drawable.bg_game_5);
//        setImage(riv_image_head, R.drawable.icon_image_head);
//        setBackground(btn_navigation_bar, R.drawable.icon_navigation_bar);
//
//    }
//
//    public void setImage(View view, int drawable){
//        reqWidth = view.getWidth();
//        reqHeight = view.getHeight();
//        ( (ImageView) view ).setImageBitmap(AutoAdaptImage.decodeSampledBitmapFromResource(getResources(), drawable,
//                reqWidth, reqHeight));
//        ( (ImageView) view ).setScaleType(ImageView.ScaleType.CENTER_CROP);
//    }
//
//    public void setBackground(View view, int drawable){
//        reqWidth = view.getWidth();
//        reqHeight = view.getHeight();
//        Drawable db = new BitmapDrawable(
//                getResources(),
//                AutoAdaptImage.decodeSampledBitmapFromResource(getResources(), drawable
//                        , reqWidth, reqHeight)
//        );
//        view.setBackground(db);
//    }

}
