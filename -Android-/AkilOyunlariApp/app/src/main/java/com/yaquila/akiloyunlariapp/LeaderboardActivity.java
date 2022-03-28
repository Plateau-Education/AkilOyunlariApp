package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, View.OnTouchListener {

    private GestureDetector mGestureDetector;
    LinearLayout tab1,tab2,tab3,tab4,tab5,tab6;
    ScrollView leaderboard_tab_sl1,leaderboard_tab_sl2,leaderboard_tab_sl3,leaderboard_tab_sl4,leaderboard_tab_sl5,leaderboard_tab_sl6;

    TabHost tabHost;

    LoadingDialog loadingDialog;


    public void goToMainMenu(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    public class GetRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringBuilder result = new StringBuilder();
                URL reqURL;
                reqURL = new URL(strings[0] + "/leaderboard/board" + "?Token=" + strings[1]);

                HttpURLConnection connection = (HttpURLConnection) reqURL.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.connect();
                InputStream in;
                int status = connection.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK)  {
                    in = connection.getErrorStream();
                }
                else  {
                    in = connection.getInputStream();
                }
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {

                    char current = (char) data;
                    result.append(current);
                    data = reader.read();
                }
                Log.i("result", result.toString());
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jb = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));

                LayoutInflater inflater = getLayoutInflater();
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

//                leaderboard_tab_sl1.setOnTouchListener(LeaderboardActivity.this);
//                leaderboard_tab_sl2.setOnTouchListener(LeaderboardActivity.this);
//                leaderboard_tab_sl3.setOnTouchListener(LeaderboardActivity.this);
//                leaderboard_tab_sl4.setOnTouchListener(LeaderboardActivity.this);
//                leaderboard_tab_sl5.setOnTouchListener(LeaderboardActivity.this);
//                leaderboard_tab_sl6.setOnTouchListener(LeaderboardActivity.this);

                mGestureDetector = new GestureDetector(LeaderboardActivity.this,LeaderboardActivity.this);


                SharedPreferences sP = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
                String username = sP.getString("username","-");
                String userRank;
                List<String> games = new ArrayList<>(Arrays.asList("Sudoku", "HazineAvi", "Patika", "SayiBulmaca", "SozcukTuru", "Piramit"));
                List<ScrollView> slList = new ArrayList<>(Arrays.asList(leaderboard_tab_sl1,leaderboard_tab_sl2,leaderboard_tab_sl3,leaderboard_tab_sl4,leaderboard_tab_sl5,leaderboard_tab_sl6));
                for(int g = 0; g<games.size(); g++){
                    String game = games.get(g);
                    JSONArray usernameAndScores = jb.getJSONArray(game);
                    ScrollView sl = slList.get(g);
                    for(int i = 0; i<usernameAndScores.length(); i++) {
                        if (i < 10) {
                            LinearLayout horLL = sl.findViewWithTag("LL" + (i + 1));
                            ((TextView) horLL.getChildAt(1)).setText(
                                    usernameAndScores.getJSONArray(i).getString(0)); // kullanıcı adı
                            ((TextView) horLL.getChildAt(2)).setText(
                                    usernameAndScores.getJSONArray(i).getString(1)); // skoru
                        }


                        if (usernameAndScores.getJSONArray(i).getString(0).equals(username)) {
                            userRank = Integer.toString((i + 1));
                            LinearLayout horLL = sl.findViewWithTag("LLYou");
                            ((TextView) horLL.getChildAt(0)).setText(userRank);//score
                            ((TextView) horLL.getChildAt(2)).setText(usernameAndScores.getJSONArray(i).getString(1));//score
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            loadingDialog.dismissDialog();
        }
    }

    @SuppressLint("InflateParams")
    public void loadingDialogFunc(){
        loadingDialog = new LoadingDialog(LeaderboardActivity.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
        loadingDialog.startLoadingAnimation();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        GetRequest getRequest = new GetRequest();
        getRequest.execute("https://mind-plateau-api.herokuapp.com/","fx!Ay:;<p6Q?C8N{");
        loadingDialogFunc();

        final LayoutInflater inflater = getLayoutInflater();
        leaderboard_tab_sl1= (ScrollView) inflater.inflate(
                this.getResources().getIdentifier(
                        "leaderboard_tab_sl",
                        "layout", this.getPackageName())
                ,null);

        tab1 = findViewById(R.id.tab1);
        tab2 = findViewById(R.id.tab2);
        tab3 = findViewById(R.id.tab3);
        tab4 = findViewById(R.id.tab4);
        tab5 = findViewById(R.id.tab5);
        tab6 = findViewById(R.id.tab6);

        tab1.addView(leaderboard_tab_sl1);


        tabHost = findViewById(R.id.tabhost);
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
        spec.setIndicator(getString(R.string.HazineAvı));
        tabHost.addTab(spec);

        //tab3
        spec = tabHost.newTabSpec("tab3");
        spec.setContent(R.id.tab3);
        spec.setIndicator(getString(R.string.Patika));
        tabHost.addTab(spec);

        //tab4
        spec = tabHost.newTabSpec("tab4");
        spec.setContent(R.id.tab4);
        spec.setIndicator(getString(R.string.SayıBulmaca));
        tabHost.addTab(spec);

        //tab5
        spec = tabHost.newTabSpec("tab5");
        spec.setContent(R.id.tab5);
        spec.setIndicator(getString(R.string.SözcükTuru));
        tabHost.addTab(spec);

        //tab6
        spec = tabHost.newTabSpec("tab6");
        spec.setContent(R.id.tab6);
        spec.setIndicator(getString(R.string.Piramit));
        tabHost.addTab(spec);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                List<ScrollView> slList = new ArrayList<>(Arrays.asList(
                        leaderboard_tab_sl1, leaderboard_tab_sl2,leaderboard_tab_sl3,
                        leaderboard_tab_sl4,leaderboard_tab_sl5,leaderboard_tab_sl6));
                ScrollView currentsl = slList.get(tabHost.getCurrentTab());
                currentsl.setScrollY(0);
            }
        });

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @SuppressWarnings("deprecation")
    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
//        List<ScrollView> slList = new ArrayList<>(Arrays.asList(leaderboard_tab_sl1,leaderboard_tab_sl2,leaderboard_tab_sl3,leaderboard_tab_sl4,leaderboard_tab_sl5,leaderboard_tab_sl6));
//        ScrollView currentsl = slList.get(tabHost.getCurrentTab());
//        if(v1 > 10 && Math.abs(v)<40)
////            currentsl.fullScroll(View.FOCUS_DOWN);
//            currentsl.scrollBy(0,(int)v1);
//        if(v1 < 10 && Math.abs(v)<40)
////            currentsl.fullScroll(View.FOCUS_UP);
//            currentsl.scrollBy(0,(int)v1);
//        Log.i("v1 onscroll",v1+" / "+v);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

//    @SuppressWarnings("deprecation")
    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
//        Log.i("onFling",v+"  "+v1);
//
//        List<ScrollView> slList = new ArrayList<>(Arrays.asList(leaderboard_tab_sl1,leaderboard_tab_sl2,leaderboard_tab_sl3,leaderboard_tab_sl4,leaderboard_tab_sl5,leaderboard_tab_sl6));
//        ScrollView currentsl = slList.get(tabHost.getCurrentTab());
//
//        if(Math.abs(v1)<5000&&Math.abs(v)>4000){
//            int current = tabHost.getCurrentTab();
//            if(v > 0){
//                if(current!=0){
//                    tabHost.setCurrentTab(current-1);
//
//                    currentsl.setScrollY(0);
//                    ((HorizontalScrollView)tabHost.getTabWidget().getParent()).scrollBy(-tabHost.getTabWidget().getWidth()/tabHost.getTabWidget().getTabCount(),0);
//                }
//            } else {
//                if(current!=tabHost.getTabWidget().getTabCount()-1){
//                    tabHost.setCurrentTab(current+1);
//                    ((HorizontalScrollView)tabHost.getTabWidget().getParent()).scrollBy(tabHost.getTabWidget().getWidth()/tabHost.getTabWidget().getTabCount(),0);
//                }
//            }
//        }
//
//        else{
//            if(v1 > 10 && Math.abs(v)<40)
////            currentsl.fullScroll(View.FOCUS_DOWN);
//                currentsl.scrollBy(0,(int)v1);
//            if(v1 < 10 && Math.abs(v)<40)
////            currentsl.fullScroll(View.FOCUS_UP);
//                currentsl.scrollBy(0,(int)v1);
//        }
////        tabHost.getCurrentTab();

        return true;
    }
}