package com.example.toeic_game;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.toeic_game.fragment.MainTabFragment;
import com.example.toeic_game.util.AutoAdaptImage;
import com.example.toeic_game.util.ToastUtil;
import com.example.toeic_game.widget.LoadingDialog;
import com.example.toeic_game.widget.NameDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;
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
    private TextView nav_tv_name;
    private RoundedImageView nav_riv_image_head;

    //監聽網路狀態
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    public static boolean isConnected = false;

    //firebase
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private boolean isLogin = false;
    public static FirebaseUser currentUser;

    //firebase-storage
    private final int CAMERA_REQUEST = 1;
    int permission;
    private final int REQUEST_EXTERNEL_PERMISSION = 2;

    //google登入
    private SignInButton google_login_btn;
    private Button anonymously_login_btn;
    private GoogleSignInOptions gso;
    private GoogleSignInClient googleSignInClient;
    public static GoogleSignInAccount googleAccount;
    private final int GOOGLE_SIGN_IN = 0;
    private FirebaseAuth mAuth;

    //memberData
    public static Member member = new Member("Tester");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isConnected = isNetworkAvailable();
        Log.d("---network---", String.valueOf(isConnected));
        networkCallback = setNetWorkDetector();

        declareVar();
        setBasicUI();

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
                .requestProfile()
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

        //check login status
        isLogin = checkLoginStatus();

        /*NavigationView*/

        //set the ItemListener
        navigationView.setNavigationItemSelectedListener(this);
        //get menu by navigationView
        nav_menu = navigationView.getMenu();
        //取得第0個header,好像可以多個,裡面有可能要修改資料庫，所以放在資料庫變數宣告完的地方
        nav_header = navigationView.getHeaderView(0);

        //get some item to use
        nav_tv_name = nav_header.findViewById(R.id.tv_name);
        nav_riv_image_head = nav_header.findViewById(R.id.riv_image_head);

        //set the image head
        Glide.with(this).load(R.drawable.icon_image_head).into(nav_riv_image_head);
        //add the underline
        nav_tv_name.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        //listener for changing the name
        nav_tv_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected){
                    if(isLogin){
                        NameDialog nameDialog = new NameDialog(MainActivity.this, nav_tv_name);
                        nameDialog.show();
                    } else{
                        ToastUtil.showMsg(MainActivity.this, "Please log in to change the name");
                    }
                } else{
                    ToastUtil.showMsg(MainActivity.this, "Please confirm if your network is connected.");
                }

            }
        });

        //請求圖片讀取權限
        permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //Listener for changing the image head
        nav_riv_image_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(GravityCompat.START);
                //API23以上需要做這個檢查
                if(isConnected){
                    if(isLogin){
                        if(permission != PackageManager.PERMISSION_GRANTED){
                            //未取得權限，向使用者要求允許權限
                            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    permissions, REQUEST_EXTERNEL_PERMISSION );
                        } else{
                            //已有權限，可進行檔案存取
                            getLocalImage();
                        }
                    } else{
                        ToastUtil.showMsg(MainActivity.this, "Fail, please check if you have logged in");
                    }
                } else{
                    ToastUtil.showMsg(MainActivity.this, "Please confirm if your network is connected.");
                }
            }
        });
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null){
            if(networkInfo.isConnected()){
                return true;
            }
            else return false;
        }
        return false;
    }

    private ConnectivityManager.NetworkCallback setNetWorkDetector(){
        //return the network information

        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                ToastUtil.showMsg(MainActivity.this,"Network onAvailable");
                Log.i("---network---", "onAvailable");
                isConnected = true;
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                ToastUtil.showMsg(MainActivity.this, "Network onLost");
                Log.i("---network---", "onLost");
                isConnected = false;
            }
        };
        connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        //register the networkCallback
        connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(), networkCallback);


        return networkCallback;
    }

    private void declareVar(){
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
    }

    private void setBasicUI(){
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
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_EXTERNEL_PERMISSION:
                getLocalImage();
                break;
        }
    }

    private void getLocalImage(){
        //Intent.ACTION_GET_CONTENT，系統就會幫使用者找到裝置內合適的App來取得指定MIME類型的內容
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //設定MIME類型
        intent.setType("image/*");
        //只能選取local端的檔案
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        Intent chooseAPPIntent = Intent.createChooser(intent, "Choose the App to use");
        startActivityForResult(chooseAPPIntent, CAMERA_REQUEST);
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
    private boolean checkLoginStatus(){
        //google account, because use the firebase, doesn't need to use the google account to check.
        googleAccount = GoogleSignIn.getLastSignedInAccount(this);

        //firebase auth
        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            myRef.child("members").addListenerForSingleValueEvent(new FirebaseDataListener());
            return true;
        }
        else{
            Log.i("---Login---", "Not yet Login in");
            return false;
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

    class LoginListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.google_login_btn:
                    drawerLayout.closeDrawer(GravityCompat.END);
                    //Use the googleSignInClient to get the Intent to deliver data.
                    if(isConnected){
                        Intent signInIntent = googleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
                    }else {
                        ToastUtil.showMsg(MainActivity.this, "Please confirm if your network is connected.");
                    }

                    break;
                case R.id.anonymously_login_btn:
                    drawerLayout.closeDrawer(GravityCompat.END);
                    if(isConnected){
                        mAuth.signInAnonymously()
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            setLoginStatus();
                                            Log.i("---success---", "signInAnonymously:success");
                                            myRef.child("members")
                                                    .addListenerForSingleValueEvent(new FirebaseDataListener());
                                        }
                                        else{
                                            Log.i("---fail---", "Login fail");
                                        }
                                    }
                                });
                    }else {
                        ToastUtil.showMsg(MainActivity.this, "Please confirm if your network is connected.");
                    }

                    break;
            }
        }
    }

    //set the login status, get the currentUser
    public void setLoginStatus(){
        isLogin = true;
        nav_menu.findItem(R.id.nav_login).setVisible(false);
        //get the currentUser
        currentUser = mAuth.getCurrentUser();
    }

    //get the member, update the UI
    private void updateUI(DataSnapshot dataSnapshot){
        if(currentUser != null){
            //如果已經有資料
            if(dataSnapshot.child(currentUser.getUid()).exists()){
                member = dataSnapshot.child(currentUser.getUid()).getValue(Member.class);
                nav_tv_name.setText(member.getName());
                //如果已經設置自己的頭像
                if(!member.getImgURL().isEmpty()){
                    Glide.with(this).load(member.getImgURL()).into(nav_riv_image_head);
                }
                else {
                    //使用默認頭像
                    Glide.with(this).load(R.drawable.icon_image_head).into(nav_riv_image_head);
                }
            }
            //初始化資料
            else{
                if(googleAccount != null){
                    member = new Member(googleAccount.getDisplayName());
                }
                else{
                    member = new Member("Guest");
                }
                dataSnapshot.child(currentUser.getUid()).getRef().setValue(member);
                nav_tv_name.setText(member.getName());
                //使用默認頭像
                Glide.with(this).load(R.drawable.icon_image_head).into(nav_riv_image_head);
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
            case CAMERA_REQUEST:
                if(data != null){
                    String path = handleImageOnKitKat(data);
                    Uri file = Uri.fromFile(new File(path));
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference tempRef = storageRef.child("images/"+file.getLastPathSegment());
                    //開始上傳
                    UploadTask uploadTask = tempRef.putFile(file);
                    LoadingDialog loadingDialog = new LoadingDialog(this, uploadTask);
                    loadingDialog.show();

                    //先upload結束,才執行getDownloadUrl()
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {

                            }else{

                            }
                            // Continue with the task to get the download URL
                            return tempRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            //如果上傳成功
                            if (uploadTask.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                String imgURL = downloadUri.toString();
                                myRef.child("members").child(currentUser.getUid()).child("imgURL").setValue(imgURL);
                                Glide.with(MainActivity.this).load(imgURL).into(nav_riv_image_head);
                                if(loadingDialog.isShowing()){
                                    loadingDialog.dismiss();
                                }
                                ToastUtil.showMsg(MainActivity.this, "Success");
                            }
                            else if (uploadTask.isCanceled()) {
                                if(loadingDialog.isShowing()){
                                    loadingDialog.dismiss();
                                }
                            }
                            else {
                                if(loadingDialog.isShowing()){
                                    loadingDialog.dismiss();
                                }
                                ToastUtil.showMsg(MainActivity.this, "Fail");
                                // Handle failures
                                // ...
                            }

                        }
                    });
                }
                break;
        }
    }

    //處理一些版本不同可能遇到的問題，主要在於Uri返回可能不同，造成不同結果。
    //https://www.itread01.com/content/1550456105.html
    private String handleImageOnKitKat(Intent data) {
        String imagePath = null;
        //URI(uniform resource identifier)用來指定一個資源
        Uri uri = data.getData();

        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                //Log.d(TAG, uri.toString());
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                //Log.d(TAG, uri.toString());
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
            return imagePath;
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //Log.d(TAG, "content: " + uri.toString());
            imagePath = getImagePath(uri, null);
            return imagePath;
        }
        return imagePath;
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }

            cursor.close();
        }
        return path;
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            //use the task to get the account object.It can use to get the user information.
            googleAccount = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(googleAccount);
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
                        setLoginStatus();
                        myRef.child("members")
                            .addListenerForSingleValueEvent(new FirebaseDataListener());
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
                //firebase's signOut,會緩存帳號，可下次登入時不用選擇帳號
                FirebaseAuth.getInstance().signOut();
                //真正完全signOut，下次登入要重新選擇帳號
                googleSignInClient.signOut();
                ToastUtil.showMsg(MainActivity.this, "Logout");
                resetData();
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

    private void resetData(){
        nav_menu.findItem(R.id.nav_login).setVisible(true);
        nav_tv_name.setText("name");
        isLogin = false;
        member = new Member("Tester");
        currentUser = null;
        googleAccount = null;
        Glide.with(this).load(R.drawable.icon_image_head).into(nav_riv_image_head);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }
}
