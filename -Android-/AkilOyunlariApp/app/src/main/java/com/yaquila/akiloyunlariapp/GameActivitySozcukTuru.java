package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.yaquila.akiloyunlariapp.gameutils.SozcukTuruUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GameActivitySozcukTuru extends AppCompatActivity {

    public static String gameName;
    public static String difficulty;
    public static int timerInSeconds = 0;
    public static boolean timerStopped=false;
    public static boolean paused = false;
    public static boolean gotQuestion = false;
    public static boolean solvedQuestion = false;

    public static LoadingDialog loadingDialog;
    public static Handler timerHandler;
    public static Runnable runnable;


    public void wannaLeaveDialog(View view){
        LayoutInflater factory = LayoutInflater.from(this);
        final View leaveDialogView = factory.inflate(R.layout.leave_dialog, null);
        final AlertDialog leaveDialog = new AlertDialog.Builder(this).create();
        leaveDialog.setView(leaveDialogView);

        leaveDialogView.findViewById(R.id.leaveDialogYes).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
                try {
                    ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("SozcukTuru."+difficulty, ObjectSerializer.serialize(new ArrayList<String>())));
                    ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDSozcukTuru."+difficulty, ObjectSerializer.serialize(new ArrayList<Integer>())));
                    Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));

                    assert questions != null;
                    questions.remove(0);

                    assert solvedQuestions != null;
                    assert gameIds != null;
                    Objects.requireNonNull(solvedQuestions.get("SozcukTuru." + difficulty)).add(gameIds.remove(0)+"-"+"0");

                    Log.i("solvedQuestions",solvedQuestions+"");

                    sharedPreferences.edit().putString("SozcukTuru."+difficulty, ObjectSerializer.serialize(questions)).apply();
                    sharedPreferences.edit().putString("IDSozcukTuru."+difficulty, ObjectSerializer.serialize(gameIds)).apply();
                    sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize((Serializable) solvedQuestions)).apply();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
                intent.putExtra("type","single");
                startActivity(intent);
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                timerStopped=true;
                leaveDialog.dismiss();
            }
        });
        leaveDialogView.findViewById(R.id.leaveDialogNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveDialog.dismiss();
            }
        });
        leaveDialog.show();
    }

    public void nextQuestion(View view){
        if(timerStopped){
            LayoutInflater factory = LayoutInflater.from(this);
            final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
            final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
            TextView timerTV = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
            timerTV.setText(formatTime(timerInSeconds));
            correctDialog.setView(leaveDialogView);

            final AppCompatActivity appCompatActivity = this;
            leaveDialogView.findViewById(R.id.correctDialogNext).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainFunc(appCompatActivity);
                    correctDialog.dismiss();
                }
            });
            leaveDialogView.findViewById(R.id.correctDialogGameMenu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
                    intent.putExtra("type","single");
                    startActivity(intent);
                    overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                    correctDialog.dismiss();
                }
            });
            correctDialog.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void undoOperation(View view){
        SozcukTuruUtils.undoOperation();
    }

    public void resetGrid(View view){
        SozcukTuruUtils.resetGrid(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void checkAnswer(final AppCompatActivity context,View view){
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        if(SozcukTuruUtils.checkAnswer()){
            SharedPreferences sharedPreferences = context.getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
            try {
                ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("SozcukTuru."+difficulty, ObjectSerializer.serialize(new ArrayList<String>())));
                ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDSozcukTuru."+difficulty, ObjectSerializer.serialize(new ArrayList<Integer>())));
                Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));

                assert questions != null;
                questions.remove(0);

                assert solvedQuestions != null;
                assert gameIds != null;
                Objects.requireNonNull(solvedQuestions.get("SozcukTuru." + difficulty)).add(gameIds.remove(0)+"-"+timerInSeconds);

                Log.i("solvedQuestions++++",solvedQuestions+"");

                sharedPreferences.edit().putString("SozcukTuru."+difficulty, ObjectSerializer.serialize(questions)).apply();
                sharedPreferences.edit().putString("IDSozcukTuru."+difficulty, ObjectSerializer.serialize(gameIds)).apply();
                sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize((Serializable) solvedQuestions)).apply();

            } catch (IOException e) {
                e.printStackTrace();
            }

            timerStopped = true;
            solvedQuestion = true;
            LayoutInflater factory = LayoutInflater.from(context);
            final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
            final AlertDialog correctDialog = new AlertDialog.Builder(context).create();
            TextView timerTV = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
            timerTV.setText(formatTime(timerInSeconds));
            correctDialog.setView(leaveDialogView);

            context.findViewById(R.id.clickView).setVisibility(View.VISIBLE);
            TextView undoTV = context.findViewById(R.id.undoTV_ga);
            TextView resetTV = context.findViewById(R.id.resetTV_game);
            for (int i = 0; i < SozcukTuruUtils.gridSizeY; i++) {
                for (int j = 0; j < SozcukTuruUtils.gridSizeX; j++) {
                    gridLayout.findViewWithTag(Integer.toString(j) + i).setEnabled(false);
                }
            }
            undoTV.setEnabled(false);
            resetTV.setEnabled(false);

            leaveDialogView.findViewById(R.id.correctDialogNext).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainFunc(context);
                    correctDialog.dismiss();
                }
            });
            leaveDialogView.findViewById(R.id.correctDialogGameMenu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context.getApplicationContext(), GameListActivity.class);
                    intent.putExtra("type","single");
                    context.startActivity(intent);
                    context.overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                    correctDialog.dismiss();
                }
            });
            correctDialog.show();
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    public static class GetRequest extends AsyncTask<String, Void, String> {

        ArrayList<String> questions = new ArrayList<>();
        ArrayList<Integer> gameIds = new ArrayList<>();

        AppCompatActivity ctx;
        SharedPreferences sharedPreferences;

        public GetRequest(AppCompatActivity context){
            ctx = context;
            sharedPreferences = ctx.getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringBuilder result = new StringBuilder();
                SharedPreferences sharedPreferences = ctx.getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
                String id = sharedPreferences.getString("id", "non");
                try {
                    questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("SozcukTuru." + difficulty, ObjectSerializer.serialize(new ArrayList<String>())));
                    gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDSozcukTuru." + difficulty, ObjectSerializer.serialize(new ArrayList<Integer>())));
                }catch (IOException e){
                    e.printStackTrace();
                }
                assert gameIds != null;
                URL reqURL;
                if(gameIds.size() > 10) {
                    reqURL = new URL(strings[0] + "/" + id + "?" + "Info=" + (1) + "&Token=" + strings[1]);
                }else{
                    reqURL = new URL(strings[0] + "/" + id + "?" + "Info=" + (Math.abs(10 - gameIds.size()) + 1) + "&Token=" + strings[1]);
                }

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

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                org.json.JSONObject jb = new org.json.JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));
                JSONArray gridArrays = (JSONArray)jb.get("Info");
                JSONArray idArray = (JSONArray)jb.get("Ids");
                Log.i("idarray", idArray +"  "+idArray.length()+"    ga:"+gridArrays.length());
                Map<String, ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));
                assert solvedQuestions != null;
                Log.i("solvedQuestion", Objects.requireNonNull(solvedQuestions.get("SozcukTuru."+difficulty)) +"ss");
                for(int i = 0; i < idArray.length(); i++){
                    if(!gameIds.contains(idArray.getInt(i))&&!Objects.requireNonNull(solvedQuestions.get("SozcukTuru."+difficulty)).toString().contains(idArray.getInt(i)+"-")) {
                        questions.add(gridArrays.getJSONArray(i).getJSONArray(0).getJSONArray(0).toString());
                        gameIds.add(idArray.getInt(i));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if(questions.size() == 0){
                Intent intent = new Intent(ctx.getApplicationContext(), GameListActivity.class);
                intent.putExtra("type","single");
                intent.putExtra("message","Need internet connection to view " + gameName +" "+ difficulty);
                ctx.startActivity(intent);
                ctx.overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                timerStopped=true;
                loadingDialog.dismissDialog();
            }

            try {
                seperateGridAnswer(new JSONArray(questions.get(0)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                sharedPreferences.edit().putString("SozcukTuru."+difficulty, ObjectSerializer.serialize(questions)).apply();
                sharedPreferences.edit().putString("IDSozcukTuru."+difficulty, ObjectSerializer.serialize(gameIds)).apply();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Log.i("gameIds", ObjectSerializer.deserialize(sharedPreferences.getString("IDSozcukTuru." + difficulty, ObjectSerializer.serialize(new ArrayList<Integer>()))) +"");
            } catch (IOException e) {
                e.printStackTrace();
            }

            timerStopped=false;
            gotQuestion = true;
            solvedQuestion=false;
            timerFunc(ctx);
            loadingDialog.dismissDialog();
        }
    }

    public static void seperateGridAnswer(JSONArray grid) throws JSONException {
        SozcukTuruUtils.seperateGridAnswer(grid);
    }

    public static void timerFunc(AppCompatActivity context){
        timerHandler = new Handler();
        final TextView timerTV = context.findViewById(R.id.timeTV_game);
        runnable = new Runnable() {
            @Override
            public void run() {
                timerInSeconds+=1;
                Log.i("timerInSeconds",timerInSeconds+"");
//                Log.i("SozcukTuruUtils.answer",SozcukTuruUtils.answer);
                timerTV.setText(formatTime(timerInSeconds));
                if(!timerStopped) timerHandler.postDelayed(this,1000);
            }
        };
        timerHandler.post(runnable);

    }

    @SuppressLint("DefaultLocale")
    public static String formatTime(int secs) {
        return String.format("%02d:%02d", (secs % 3600) / 60, secs % 60);
    }

    @SuppressLint("InflateParams")
    public static void loadingDialogFunc(AppCompatActivity context){
        loadingDialog = new LoadingDialog(context, context.getLayoutInflater().inflate(R.layout.loading_dialog,null));
        loadingDialog.startLoadingAnimation();
    }

    public static void clearGrid(){
        SozcukTuruUtils.clearGrid();
        timerInSeconds = 0;
        timerStopped=true;
    }

    public static void mainFunc(AppCompatActivity context){
        context.findViewById(R.id.clickView).setVisibility(View.GONE);
        TextView undoTV = context.findViewById(R.id.undoTV_ga);
        TextView resetTV = context.findViewById(R.id.resetTV_game);
        undoTV.setEnabled(true);
        resetTV.setEnabled(true);
        clearGrid();
        GetRequest getRequest = new GetRequest(context);
        getRequest.execute("https://mind-plateau-api.herokuapp.com/SozcukTuru."+difficulty,"fx!Ay:;<p6Q?C8N{");
        loadingDialogFunc(context);
    }

    public void initSomeVar(){
        SozcukTuruUtils.initSomeVar();
    }

    public static AppCompatActivity appCompatActivity = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appCompatActivity = this;
        SozcukTuruUtils.initVars(this);
        
        Intent intent = getIntent();
        gameName = intent.getStringExtra("gameName");
        difficulty = intent.getStringExtra("difficulty");
        TextView textView;
        String s;
        SpannableString ss1;

        assert difficulty != null;
        if (difficulty.equals(getString(R.string.Easy))) {
            setContentView(R.layout.activity_game_sozcuk_turu34);

            s = getString(R.string.Easy) + " - 3,4,5 " + getString(R.string.letters);
            ss1 = new SpannableString(s);
            ss1.setSpan(new RelativeSizeSpan(0.35f), s.indexOf("-"), s.length(), 0); // set size
            textView = findViewById(R.id.diffTV_game);
            textView.setText(ss1);

            Log.i("diff", "easy");
            difficulty = "Easy";
            SozcukTuruUtils.gridSizeX = 3;
            SozcukTuruUtils.gridSizeY = 4;
        } else if (difficulty.equals(getString(R.string.Medium))) {
            setContentView(R.layout.activity_game_sozcuk_turu35);

            s = getString(R.string.Medium) + " - 4,5,6 " + getString(R.string.letters);
            ss1 = new SpannableString(s);
            ss1.setSpan(new RelativeSizeSpan(0.35f), s.indexOf("-"), s.length(), 0); // set size
            textView = findViewById(R.id.diffTV_game);
            textView.setText(ss1);

            SozcukTuruUtils.gridSizeX = 3;
            SozcukTuruUtils.gridSizeY = 5;
            difficulty = "Medium";
            Log.i("diff", "medium");
        } else if (difficulty.equals(getString(R.string.Hard))) {
            setContentView(R.layout.activity_game_sozcuk_turu45);

            s = getString(R.string.Hard) + " - 2,3,4,5,6 " + getString(R.string.letters);
            ss1 = new SpannableString(s);
            ss1.setSpan(new RelativeSizeSpan(0.35f), s.indexOf("-"), s.length(), 0); // set size
            textView = findViewById(R.id.diffTV_game);
            textView.setText(ss1);

            SozcukTuruUtils.gridSizeX = 4;
            SozcukTuruUtils.gridSizeY = 5;
            difficulty = "Hard";
            Log.i("diff", "medium");
        } else {
            setContentView(R.layout.activity_game_sozcuk_turu55);

            s = getString(R.string.VeryHard) + " - 3,4,5,6,7 " + getString(R.string.letters);
            ss1 = new SpannableString(s);
            ss1.setSpan(new RelativeSizeSpan(0.35f), s.indexOf("-"), s.length(), 0); // set size
            textView = findViewById(R.id.diffTV_game);
            textView.setText(ss1);

            SozcukTuruUtils.gridSizeX = 5;
            SozcukTuruUtils.gridSizeY = 5;
            difficulty = "Hardest";
            Log.i("diff", "hard");
        }

        final GridLayout gridLayout = findViewById(R.id.gridGL_ga);

//        Handler uiHandler = new Handler();
//        uiHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                pxHeight = (gridLayout.getHeight()/306)*300;
//                initSomeVar();
//                Log.i("pxheight",pxHeight+"");
//            }
//        }, 200);
        SozcukTuruUtils.pxHeightY = (int) (300 * getResources().getDisplayMetrics().density);
        SozcukTuruUtils.pxHeightX = (int) ((300*SozcukTuruUtils.gridSizeX/SozcukTuruUtils.gridSizeY) * getResources().getDisplayMetrics().density);
//        Log.i("asd", +"");
        Log.i("pxheightX",SozcukTuruUtils.pxHeightX+"");
        Log.i("pxheightY",SozcukTuruUtils.pxHeightY+"");
        initSomeVar();
        mainFunc(this);

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        wannaLeaveDialog(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused=true;
        timerStopped=true;
        try{
            timerHandler.removeCallbacks(runnable);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        timerInSeconds = pausedTime;
        if(paused&&gotQuestion){
            timerStopped=false;
            paused=false;
            timerFunc(this);
        }
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        try {
            ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("SozcukTuru."+difficulty, ObjectSerializer.serialize(new ArrayList<String>())));
            ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDSozcukTuru."+difficulty, ObjectSerializer.serialize(new ArrayList<Integer>())));
            ArrayList<String> solvedQuestions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new ArrayList<String>())));

            assert questions != null;
            questions.remove(0);

            sharedPreferences.edit().putString("SozcukTuru."+difficulty, ObjectSerializer.serialize(questions)).apply();
            sharedPreferences.edit().putString("IDSozcukTuru."+difficulty, ObjectSerializer.serialize(gameIds)).apply();
            sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize(solvedQuestions)).apply();

            assert solvedQuestions != null;
            assert gameIds != null;
            solvedQuestions.add("SozcukTuru."+difficulty+","+gameIds.remove(0)+"-"+"0");


        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }


}