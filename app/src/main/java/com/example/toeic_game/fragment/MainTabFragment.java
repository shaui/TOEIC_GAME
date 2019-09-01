package com.example.toeic_game.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.toeic_game.R;
import com.example.toeic_game.widget.StartDialog;
import com.makeramen.roundedimageview.RoundedImageView;

public class MainTabFragment extends Fragment {

    private View tab_view;
    private RoundedImageView riv_game_1, riv_game_2, riv_game_3,
            riv_game_4, riv_game_5;
    private NestedScrollView nestedScrollView;
    private int color;

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        Bundle bundle = getArguments();
        this.color = bundle.getInt("color");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        tab_view = inflater.inflate(R.layout.layout_tab_item, container, false);
        Log.d("Fragment","---onCreateView---");
        return tab_view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("Fragment","---onActivityCreated---");
        setTabContent();
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

    private void setTabContent(){
        riv_game_1 = tab_view.findViewById(R.id.riv_gmae_1);
        riv_game_2 = tab_view.findViewById(R.id.riv_gmae_2);
        riv_game_3 = tab_view.findViewById(R.id.riv_gmae_3);
        riv_game_4 = tab_view.findViewById(R.id.riv_gmae_4);
        riv_game_5 = tab_view.findViewById(R.id.riv_gmae_5);
        nestedScrollView = tab_view.findViewById(R.id.sv_layout_content);

        nestedScrollView.setBackgroundColor(ContextCompat.getColor(context, color));

        //setImages
        Glide.with(context).load(R.drawable.bg_game_1).into(riv_game_1);
        Glide.with(context).load(R.drawable.bg_game_2).into(riv_game_2);
        Glide.with(context).load(R.drawable.bg_game_3).into(riv_game_3);
        Glide.with(context).load(R.drawable.bg_game_4).into(riv_game_4);
        Glide.with(context).load(R.drawable.bg_game_5).into(riv_game_5);

        //setListener
        StartOnclick startOnclick = new StartOnclick();
        riv_game_1.setOnClickListener(startOnclick);
        riv_game_2.setOnClickListener(startOnclick);
        riv_game_3.setOnClickListener(startOnclick);
        riv_game_4.setOnClickListener(startOnclick);
        riv_game_5.setOnClickListener(startOnclick);
    }


}
