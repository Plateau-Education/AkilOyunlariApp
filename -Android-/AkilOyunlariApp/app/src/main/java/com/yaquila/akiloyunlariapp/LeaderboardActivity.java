package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class LeaderboardActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, View.OnTouchListener {

    private GestureDetector mGestureDetector;
    LinearLayout tab1,tab2,tab3,tab4,tab5,tab6;
    ScrollView leaderboard_tab_sl1,leaderboard_tab_sl2,leaderboard_tab_sl3,leaderboard_tab_sl4,leaderboard_tab_sl5,leaderboard_tab_sl6;


    @SuppressWarnings("deprecation")
    TabHost tabHost;


    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);


        final LayoutInflater inflater = getLayoutInflater();
        leaderboard_tab_sl1= (ScrollView) inflater.inflate(this.getResources().getIdentifier("leaderboard_tab_sl", "layout", this.getPackageName()),null);

        tab1 = (LinearLayout) findViewById(R.id.tab1);
        tab2 = (LinearLayout) findViewById(R.id.tab2);
        tab3 = (LinearLayout) findViewById(R.id.tab3);
        tab4 = (LinearLayout) findViewById(R.id.tab4);
        tab5 = (LinearLayout) findViewById(R.id.tab5);
        tab6 = (LinearLayout) findViewById(R.id.tab6);

        tab1.addView(leaderboard_tab_sl1);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                leaderboard_tab_sl2 = (ScrollView) inflater.inflate(getApplicationContext().getResources().getIdentifier("leaderboard_tab_sl", "layout", getApplicationContext().getPackageName()),null);
                leaderboard_tab_sl3 = (ScrollView) inflater.inflate(getApplicationContext().getResources().getIdentifier("leaderboard_tab_sl", "layout", getApplicationContext().getPackageName()),null);
                leaderboard_tab_sl4 = (ScrollView) inflater.inflate(getApplicationContext().getResources().getIdentifier("leaderboard_tab_sl", "layout", getApplicationContext().getPackageName()),null);
                leaderboard_tab_sl5 = (ScrollView) inflater.inflate(getApplicationContext().getResources().getIdentifier("leaderboard_tab_sl", "layout", getApplicationContext().getPackageName()),null);
                leaderboard_tab_sl6 = (ScrollView) inflater.inflate(getApplicationContext().getResources().getIdentifier("leaderboard_tab_sl", "layout", getApplicationContext().getPackageName()),null);

                tab2.addView(leaderboard_tab_sl2);
                tab3.addView(leaderboard_tab_sl3);
                tab4.addView(leaderboard_tab_sl4);
                tab5.addView(leaderboard_tab_sl5);
                tab6.addView(leaderboard_tab_sl6);

                leaderboard_tab_sl1.setOnTouchListener(LeaderboardActivity.this);
                leaderboard_tab_sl2.setOnTouchListener(LeaderboardActivity.this);
                leaderboard_tab_sl3.setOnTouchListener(LeaderboardActivity.this);
                leaderboard_tab_sl4.setOnTouchListener(LeaderboardActivity.this);
                leaderboard_tab_sl5.setOnTouchListener(LeaderboardActivity.this);
                leaderboard_tab_sl6.setOnTouchListener(LeaderboardActivity.this);

                mGestureDetector = new GestureDetector(LeaderboardActivity.this,LeaderboardActivity.this);


            }
        },1000);


        tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setOnTouchListener(this);
        tabHost.setup();

        //tab1
        TabHost.TabSpec spec = tabHost.newTabSpec("tab1");
        spec.setContent(R.id.tab1);


//        TextView sudokuTV= (TextView) inflater.inflate(this.getResources().getIdentifier("sudoku_tab_tv", "layout", this.getPackageName()),null);

        spec.setIndicator("Sudoku");
        tabHost.addTab(spec);



        //tab2
        spec = tabHost.newTabSpec("tab2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Hazine Avı");
        tabHost.addTab(spec);

        //tab3
        spec = tabHost.newTabSpec("tab3");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Patika");
        tabHost.addTab(spec);

        //tab4
        spec = tabHost.newTabSpec("tab4");
        spec.setContent(R.id.tab4);
        spec.setIndicator("Sayı Bulmaca");
        tabHost.addTab(spec);

        //tab5
        spec = tabHost.newTabSpec("tab5");
        spec.setContent(R.id.tab5);
        spec.setIndicator("Sözcük Türü");
        tabHost.addTab(spec);

        //tab6
        spec = tabHost.newTabSpec("tab6");
        spec.setContent(R.id.tab6);
        spec.setIndicator("Piramit");
        tabHost.addTab(spec);


    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if(new ArrayList<>(Arrays.asList(leaderboard_tab_sl1.getId(),leaderboard_tab_sl2.getId(),leaderboard_tab_sl3.getId(),leaderboard_tab_sl4.getId(),leaderboard_tab_sl5.getId(),leaderboard_tab_sl6.getId())).contains(view.getId())){
            mGestureDetector.onTouchEvent(motionEvent);
            return true;
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        Log.i("onFling",v+"  "+v1);

        if(Math.abs(v1)<6000){
            int current = tabHost.getCurrentTab();
            if(v > 0){
                if(current!=0){
                    tabHost.setCurrentTab(current-1);
                    ((HorizontalScrollView)tabHost.getTabWidget().getParent()).scrollBy(-tabHost.getTabWidget().getWidth()/tabHost.getTabWidget().getTabCount(),0);
                }
            } else {
                if(current!=tabHost.getTabWidget().getTabCount()-1){
                    tabHost.setCurrentTab(current+1);
                    ((HorizontalScrollView)tabHost.getTabWidget().getParent()).scrollBy(tabHost.getTabWidget().getWidth()/tabHost.getTabWidget().getTabCount(),0);
                }
            }
        }
//        tabHost.getCurrentTab();

        return true;
    }
}