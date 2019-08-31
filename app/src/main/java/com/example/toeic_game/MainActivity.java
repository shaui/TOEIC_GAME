package com.example.toeic_game;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.toeic_game.fragment.MainTabFragment;
import com.example.toeic_game.util.AutoAdaptImage;
import com.example.toeic_game.util.ToastUtil;
import com.example.toeic_game.widget.NameDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private List<String> tabNameList;
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

    //menu data
    private Menu nav_menu = null;
    private View nav_header = null;
    private TextView tv_nav_name;

    //firebase
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private boolean isLogin = false;

    //google登入
    private SignInButton google_login_btn;
    private Button anonymously_login_btn;
    private GoogleSignInOptions gso;
    private GoogleSignInClient googleSignInClient;
    private final int GOOGLE_SIGN_IN = 0;
    private FirebaseAuth mAuth;

    //memberData
    private Member member = null;
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
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //NavigationView
        navigationView.setNavigationItemSelectedListener(this);
        //get menu by navigationView
        nav_menu = navigationView.getMenu();

        //firebbase
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        //firebase auth
        mAuth = FirebaseAuth.getInstance();

        /*google登入*/

        //configure Google Sign-In to request the user data.
        //The Options is mean that you con use variable method to get the different user data
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        //login button
        google_login_btn = findViewById(R.id.google_login_btn);
        setGooglePlusButtonText(google_login_btn, "Sign in with google");
        anonymously_login_btn = findViewById(R.id.anonymously_login_btn);
        LoginListener loginListener = new LoginListener();
        google_login_btn.setOnClickListener(loginListener);
        anonymously_login_btn.setOnClickListener(loginListener);
        isLogin = ckeckLoginStatus();

        //取得第0個header,好像可以多個,裡面有可能要修改資料庫，所以放在資料庫變數宣告完的地方
        nav_header = navigationView.getHeaderView(0);
        tv_nav_name = nav_header.findViewById(R.id.tv_name);
        //添加底線
        tv_nav_name.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tv_nav_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //登入才能改名字
                if(isLogin){
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    NameDialog nameDialog = new NameDialog(MainActivity.this, tv_nav_name, currentUser);
                    nameDialog.show();
                }
                else{
                    ToastUtil.showMsg(MainActivity.this, "Please log in to change the name");
                }

            }
        });
    }

    //set the text on the google login button
    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

    //check is a user has already signed in to app, if not null, that has already do it.
    private boolean ckeckLoginStatus(){
        //google account, because use the firebase, doesn't need to use the google account to check.
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        //firebase auth
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            myRef.child("members").addListenerForSingleValueEvent(new FirebaseDataListener());
            return true;
        }
        else{
            Log.i("---Login---", "Not yet Login in");
            return false;
        }
    }

    class LoginListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.google_login_btn:
                    drawerLayout.closeDrawer(GravityCompat.END);
                    //Use the googleSignInClient to get the Intent to deliver data.
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
                    break;
                case R.id.anonymously_login_btn:
                    drawerLayout.closeDrawer(GravityCompat.END);
                    mAuth.signInAnonymously()
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        isLogin = true;
                                        Log.i("---success---", "signInAnonymously:success");
                                        myRef.child("members")
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        updateUI(dataSnapshot);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                    }
                                    else{
                                        Log.i("---fail---", "Login fail");
                                    }
                                }
                            });
                    break;
            }
        }
    }

    class FirebaseDataListener implements ValueEventListener{

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            updateUI(dataSnapshot);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }

    private void updateUI(DataSnapshot dataSnapshot){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            if(dataSnapshot.child(currentUser.getUid()).exists()){
                member = dataSnapshot.child(currentUser.getUid()).getValue(Member.class);
                tv_nav_name.setText(member.getName());
            }
            else{
                member = new Member();
                dataSnapshot.child(currentUser.getUid()).getRef().setValue(member);
                tv_nav_name.setText(member.getName());
            }
            ToastUtil.showMsg(MainActivity.this, "Welcome " + member.getName());
        }
        else{
            Log.i("---Login---", "Doesn't find the user, fail");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        switch (requestCode){
            case GOOGLE_SIGN_IN:
                //use the data to create a GoogleSignInAccount task
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
                break;
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            //use the task to get the account object.It can use to get the user information.
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("---TAG---", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //用accoutn.getIdToken取得ID，再用此ID取得credential
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        //mAuth用credential登入
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        isLogin = true;
                        //檢查資料庫是否有資料，並更新member物件
                        myRef.child("members")
                            .addListenerForSingleValueEvent(new FirebaseDataListener());
                        //修改UI
                        if(nav_menu != null){
                            nav_menu.findItem(R.id.nav_login).setVisible(false);
                        }
                        else{
                            ToastUtil.showMsg(MainActivity.this, "menu is null");
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        ToastUtil.showMsg(MainActivity.this, "Credential fail");
                    }
                }
            });
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
                drawerLayout.closeDrawer(GravityCompat.START);
                drawerLayout.openDrawer(GravityCompat.END);
                break;
            case R.id.nav_logout:
                //firebase's signOut,會緩存帳號，可下次案登入時不用選擇帳號
                FirebaseAuth.getInstance().signOut();
                //真正完全signOut，下次登入要重新選擇帳號
                googleSignInClient.signOut();
                ToastUtil.showMsg(MainActivity.this, "Logout");
                nav_menu.findItem(R.id.nav_login).setVisible(true);
                tv_nav_name.setText("name");
                isLogin = false;
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
