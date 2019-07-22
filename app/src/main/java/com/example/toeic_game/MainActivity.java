package com.example.toeic_game;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.toeic_game.fragment.MainTabFragment;
import com.example.toeic_game.util.AutoAdaptImage;



import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Button btn_nav;
    private DrawerLayout drawerLayout;

    private int reqWidth, reqHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TabLayout
        TabLayout tabLayout = findViewById(R.id.layout_tab);
        List<String> tabNameList = new ArrayList<>();
        tabNameList.add("Tab1");
        tabNameList.add("Tab2");
        tabNameList.add("Tab3");
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());

        List<Fragment> tabFragment = new ArrayList<>();
        for(int i=0; i<tabNameList.size(); i++){
            tabFragment.add(new MainTabFragment());
        }

        TabFragmentAdapter tabFragmentAdapter = new TabFragmentAdapter(getSupportFragmentManager(),
                tabFragment, tabNameList);
        ViewPager viewPager = findViewById(R.id.vp_content);
        //把viewPager和tablayout關連起來
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(tabFragmentAdapter);
        tabLayout.setupWithViewPager(viewPager);

        //nav_btn
        btn_nav = findViewById(R.id.btn_nav);
        setBackground(btn_nav, R.drawable.icon_navigation_bar);
        btn_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });

        //drawLayout
        drawerLayout = findViewById(R.id.layout_dl);

    }

    private void setBackground(final View view, final int drawable){
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }


    class TabFragmentAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragment;
        private List<String> title;

        public TabFragmentAdapter(FragmentManager fm, List<Fragment> fragment, List<String> title) {
            super(fm);
            this.fragment = fragment;
            this.title = title;
        }

        @Override
        public Fragment getItem(int i) {
            return fragment.get(i);
        }

        @Override
        public int getCount() {
            return fragment.size();
        }

        /*Adapter會自動設置title*/
        @Override
        public String getPageTitle(int i) {
            return title.get(i);
        }


    }
}
