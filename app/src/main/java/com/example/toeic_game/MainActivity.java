package com.example.toeic_game;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.toeic_game.fragment.MainTabFragment;
import com.example.toeic_game.util.AutoAdaptImage;



import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private  List<String> tabNameList;
    private TabLayout tabLayout;
    private View[] tabItems;
    private List<Fragment> tabFragment;
    private int[] fragment_colors = {
            R.color.colorDarkGreen, R.color.colorDarkYellow, R.color.colorDarkRed};
    private TabFragmentAdapter tabFragmentAdapter;
    private ViewPager viewPager;
    private Button btn_nav;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private int reqWidth, reqHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //declare variable
        tabNameList = new ArrayList<>();
        tabLayout = findViewById(R.id.layout_tab);
        tabItems = getTab_items_views();
        tabFragment = new ArrayList<>();
        tabFragmentAdapter= new TabFragmentAdapter(getSupportFragmentManager(),
                tabFragment, tabNameList);
        viewPager = findViewById(R.id.vp_content);
        btn_nav = findViewById(R.id.btn_nav);
        drawerLayout = findViewById(R.id.layout_dl);
        navigationView = findViewById(R.id.nav_view);

        //add tabNameList
        tabNameList.add("Tab1");
        tabNameList.add("Tab2");
        tabNameList.add("Tab3");

        //set TabLayout
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        /*addOnTabSelectedListener必須在addTab之前，否則一開始onTabSelected不會被回調到*/
        tabLayout.addOnTabSelectedListener(new MyOnTabSelectedListener());
        tabLayout.addTab(tabLayout.newTab(),true);
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());

        //tab Fragment
        for(int i=0; i<tabNameList.size(); i++){
            MainTabFragment mainTabFragment = new MainTabFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("color", fragment_colors[i]);
            mainTabFragment.setArguments(bundle);
            tabFragment.add(mainTabFragment);
        }

        //把viewPager和tablayout關連起來
//        viewPager.setOffscreenPageLimit(1); //預先加載頁面，不包刮當前頁面

        viewPager.setAdapter(tabFragmentAdapter);
        tabLayout.setupWithViewPager(viewPager);

        //客製化Tab，必須在setupWithViewPager之後才有效果(應該吧)
        for(int i=0 ; i<tabItems.length; i++){
            tabLayout.getTabAt(i).setCustomView(tabItems[i]);
        }


        //nav_btn
        setBackground(btn_nav, R.drawable.icon_navigation_bar);
        btn_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });


        //NavigationView
        navigationView.setNavigationItemSelectedListener(this);


    }

    private class MyOnTabSelectedListener implements TabLayout.OnTabSelectedListener{
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            View view1 = tabItems[0].findViewById(R.id.v_underline);
            View view2 = tabItems[1].findViewById(R.id.v_underline);
            View view3 = tabItems[2].findViewById(R.id.v_underline);
            switch (tab.getPosition()){
                case 0:
                    view1.setVisibility(View.VISIBLE);
                    view2.setVisibility(View.INVISIBLE);
                    view3.setVisibility(View.INVISIBLE);
                    break;
                case 1:
                    view2.setVisibility(View.VISIBLE);
                    view1.setVisibility(View.INVISIBLE);
                    view3.setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    view3.setVisibility(View.VISIBLE);
                    view1.setVisibility(View.INVISIBLE);
                    view2.setVisibility(View.INVISIBLE);
                    break;
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }
    private View[] getTab_items_views(){
        View[] tab_items = new View[3];
        int[] tab_backgrounds = {R.drawable.tab_item_1, R.drawable.tab_item_2, R.drawable.tab_item_3};
        String[] tab_names = {"Tab1", "Tab2", "Tab3"};
        for(int i=0; i<tab_items.length; i++){
            tab_items[i] = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_tab_item, null);
            tab_items[i].setBackgroundResource(tab_backgrounds[i]);
            TextView tab_name = tab_items[i].findViewById(R.id.tv_tab_name);
            tab_name.setText(tab_names[i]);
        }
        return tab_items;
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
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()){
            case R.id.nav_login:
                //
                break;
            case R.id.nav_logout:
                //
                break;
            case R.id.nav_character:
                //
                break;
            case R.id.nav_log:
                //
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
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
