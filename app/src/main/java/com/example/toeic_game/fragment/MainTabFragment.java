package com.example.toeic_game.fragment;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.toeic_game.R;
import com.example.toeic_game.util.AutoAdaptImage;
import com.example.toeic_game.widget.StartDialog;
import com.makeramen.roundedimageview.RoundedImageView;

public class MainTabFragment extends Fragment {

    private View tab_view;
    private RoundedImageView riv_game_1, riv_game_2, riv_game_3,
            riv_game_4, riv_game_5;
    private int reqWidth, reqHeight;

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        tab_view = inflater.inflate(R.layout.layout_tab_item, container, false);
        return tab_view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        riv_game_1 = tab_view.findViewById(R.id.riv_gmae_1);
        riv_game_2 = tab_view.findViewById(R.id.riv_gmae_2);
        riv_game_3 = tab_view.findViewById(R.id.riv_gmae_3);
        riv_game_4 = tab_view.findViewById(R.id.riv_gmae_4);
        riv_game_5 = tab_view.findViewById(R.id.riv_gmae_5);
        //setImages
        setImage(riv_game_1, R.drawable.bg_game_1);
        setImage(riv_game_2, R.drawable.bg_game_2);
        setImage(riv_game_3, R.drawable.bg_game_3);
        setImage(riv_game_4, R.drawable.bg_game_4);
        setImage(riv_game_5, R.drawable.bg_game_5);

        //setListener
        StartOnclick startOnclick = new StartOnclick();
        riv_game_1.setOnClickListener(startOnclick);
        riv_game_2.setOnClickListener(startOnclick);
        riv_game_3.setOnClickListener(startOnclick);
        riv_game_4.setOnClickListener(startOnclick);
        riv_game_5.setOnClickListener(startOnclick);

    }

    class StartOnclick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){

                case R.id.riv_gmae_1:
                    StartDialog startDialog1 = new StartDialog(context, R.drawable.bg_start_game1);
                    startDialog1.show();
                    break;
                case R.id.riv_gmae_2:
                    StartDialog startDialog2 = new StartDialog(context, R.drawable.bg_start_game2);
                    startDialog2.show();
                    break;
                case R.id.riv_gmae_3:
                    StartDialog startDialog3 = new StartDialog(context, R.drawable.bg_start_game3);
                    startDialog3.show();
                    break;
                case R.id.riv_gmae_4:
                    StartDialog startDialog4 = new StartDialog(context, R.drawable.bg_start_game4);
                    startDialog4.show();
                    break;
                case R.id.riv_gmae_5:
                    StartDialog startDialog5 = new StartDialog(context, R.drawable.bg_start_game5);
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
}
