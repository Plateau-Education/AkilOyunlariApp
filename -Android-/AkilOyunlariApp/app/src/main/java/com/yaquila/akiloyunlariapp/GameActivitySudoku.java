package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.yaquila.akiloyunlariapp.gameutils.SudokuUtils;

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

public class GameActivitySudoku extends AppCompatActivity {

    String gameName;
    String difficulty;
    int timerInSeconds = 0;
    boolean timerStopped=false;
    boolean paused = false;
    boolean gotQuestion = false;

    LoadingDialog loadingDialog;
    Handler timerHandler;
    Runnable runnable;

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
                    ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Sudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<String>())));
                    ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDSudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<Integer>())));
                    Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));

                    assert questions != null;
                    questions.remove(0);

                    assert solvedQuestions != null;
                    assert gameIds != null;
                    Objects.requireNonNull(solvedQuestions.get("Sudoku." + SudokuUtils.gridSize+"."+difficulty)).add(gameIds.remove(0)+"-"+"0");

                    Log.i("solvedQuestions",solvedQuestions+"");

                    sharedPreferences.edit().putString("Sudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(questions)).apply();
                    sharedPreferences.edit().putString("IDSudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(gameIds)).apply();
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

            leaveDialogView.findViewById(R.id.correctDialogNext).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainFunc();
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

    public void changeClicked(View view){
        SudokuUtils.changeClicked(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    public void numClicked(View view){
        if(!SudokuUtils.clickedBox.equals("-1") && SudokuUtils.numClicked(view)) checkAnswer(null);
    }

    public void deleteNum(View view){
        SudokuUtils.deleteNum();
    }

    public void undoOperation(View view){
        SudokuUtils.undoOperation();
    }

    public void resetGrid(View view){
        SudokuUtils.resetGrid(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkAnswer(View view){
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        if(SudokuUtils.checkAnswer()){
            SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
            try {
                ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Sudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<String>())));
                ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDSudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<Integer>())));
                Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));

                assert questions != null;
                questions.remove(0);

                assert solvedQuestions != null;
                assert gameIds != null;
                Objects.requireNonNull(solvedQuestions.get("Sudoku." + SudokuUtils.gridSize+"."+difficulty)).add(gameIds.remove(0)+"-"+timerInSeconds);

                Log.i("solvedQuestions",solvedQuestions+"");

                sharedPreferences.edit().putString("Sudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(questions)).apply();
                sharedPreferences.edit().putString("IDSudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(gameIds)).apply();
                sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize((Serializable) solvedQuestions)).apply();

            } catch (IOException e) {
                e.printStackTrace();
            }

            timerStopped=true;
            LayoutInflater factory = LayoutInflater.from(this);
            final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
            final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
            TextView timerTV = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
            timerTV.setText(formatTime(timerInSeconds));
            correctDialog.setView(leaveDialogView);

            findViewById(R.id.clickView).setVisibility(View.VISIBLE);
            GridLayout numsLayout = findViewById(R.id.numsGL_ga);
            TextView undoTV = findViewById(R.id.undoTV_ga);
            TextView deleteTV = findViewById(R.id.deleteTV_ga);
            TextView resetTV = findViewById(R.id.resetTV_game);
            for (int i = 0; i < SudokuUtils.gridSize; i++) {
                for (int j = 0; j < SudokuUtils.gridSize; j++) {
                    gridLayout.findViewWithTag(Integer.toString(j) + i).setEnabled(false);
                    if (SudokuUtils.gridSize==9)((TextView) gridLayout.findViewWithTag(Integer.toString(j)+i)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    else ((TextView) gridLayout.findViewWithTag(Integer.toString(j)+i)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                }
            }
            for(int i = 1; i<SudokuUtils.gridSize+1; i++){
                numsLayout.findViewWithTag(Integer.toString(i)).setEnabled(false);
            }
            undoTV.setEnabled(false);
            deleteTV.setEnabled(false);
            resetTV.setEnabled(false);

            leaveDialogView.findViewById(R.id.correctDialogNext).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainFunc();
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

    public void draftClicked(View view){
        SudokuUtils.draftClicked(view);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    public class GetRequest extends AsyncTask<String, Void, String> {

        ArrayList<String> questions = new ArrayList<>();
        ArrayList<Integer> gameIds = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringBuilder result = new StringBuilder();
                SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
                String id = sharedPreferences.getString("id", "non");
                try {
                    questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Sudoku." + SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<String>())));
                    gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDSudoku." + SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<Integer>())));
                }catch (IOException e){
                    e.printStackTrace();
                }
                assert gameIds != null;
                URL reqURL;
                if(gameIds.size() > 10) {
                    reqURL = new URL(strings[0] + "/" + id + "?" + "Info=" + (1) + "&Req=True&Token=" + strings[1]);
                }else{
                    reqURL = new URL(strings[0] + "/" + id + "?" + "Info=" + (Math.abs(10 - gameIds.size()) + 1) + "&Req=True&Token=" + strings[1]);
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
            //noinspection deprecation
            super.onPostExecute(result);

//            JSONObject jsonObject = null;
            try {
                org.json.JSONObject jb = new org.json.JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));
                JSONArray gridArrays = (JSONArray)jb.get("Info");
                JSONArray idArray = (JSONArray)jb.get("Ids");
                Log.i("idarray",idArray.toString()+"  "+idArray.length()+"    ga:"+gridArrays.length());
                Map<String, ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));
                assert solvedQuestions != null;
                Log.i("solvedQuestion", Objects.requireNonNull(solvedQuestions.get("Sudoku." + SudokuUtils.gridSize+"."+difficulty)).toString()+"ss");
                for(int i = 0; i < idArray.length(); i++){
                    if(!gameIds.contains(idArray.getInt(i))&&!Objects.requireNonNull(solvedQuestions.get("Sudoku." + SudokuUtils.gridSize+"."+difficulty)).toString().contains(idArray.getInt(i)+"-")) {
                        questions.add(gridArrays.getJSONArray(i).toString());
                        gameIds.add(idArray.getInt(i));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if(questions.size() == 0){
                Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
                intent.putExtra("type","single");
                intent.putExtra("message","Need internet connection to view " + gameName +" "+ difficulty);
                startActivity(intent);
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                timerStopped=true;
                loadingDialog.dismissDialog();
            }

            try {
                seperateGridAnswer(new JSONArray(questions.get(0)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                sharedPreferences.edit().putString("Sudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(questions)).apply();
                sharedPreferences.edit().putString("IDSudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(gameIds)).apply();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Log.i("gameIds", ObjectSerializer.deserialize(sharedPreferences.getString("IDSudoku." + SudokuUtils.gridSize, ObjectSerializer.serialize(new ArrayList<Integer>()))) +"");
            } catch (IOException e) {
                e.printStackTrace();
            }

            timerStopped=false;
            gotQuestion = true;
            timerFunc();
            loadingDialog.dismissDialog();
        }
    }

    public void seperateGridAnswer(JSONArray grid) throws JSONException {
        SudokuUtils.seperateGridAnswer(grid);
    }

    public void timerFunc(){
        //noinspection deprecation
        timerHandler = new Handler();
        final TextView timerTV = findViewById(R.id.timeTV_game);
        runnable = new Runnable() {
            @Override
            public void run() {
                timerInSeconds+=1;
                Log.i("timerInSeconds",timerInSeconds+"");
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
    public void loadingDialogFunc(){
        loadingDialog = new LoadingDialog(GameActivitySudoku.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
        loadingDialog.startLoadingAnimation();
    }

    public void clearGrid(){
        SudokuUtils.clearGrid();
        timerInSeconds = 0;
        timerStopped=true;
    }

    public void mainFunc(){
        findViewById(R.id.clickView).setVisibility(View.GONE);
        GridLayout numsLayout = findViewById(R.id.numsGL_ga);
        TextView undoTV = findViewById(R.id.undoTV_ga);
        TextView deleteTV = findViewById(R.id.deleteTV_ga);
        TextView resetTV = findViewById(R.id.resetTV_game);
        for(int i = 1; i<SudokuUtils.gridSize+1; i++){
            numsLayout.findViewWithTag(Integer.toString(i)).setEnabled(true);
        }
        undoTV.setEnabled(true);
        deleteTV.setEnabled(true);
        resetTV.setEnabled(true);
        clearGrid();
        SudokuUtils.initDraftModeActiveVar();
        GetRequest getRequest = new GetRequest();
        //noinspection deprecation
        getRequest.execute("https://akiloyunlariapp.herokuapp.com/Sudoku."+SudokuUtils.gridSize+"."+difficulty,"fx!Ay:;<p6Q?C8N{");
        loadingDialogFunc();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SudokuUtils.initVars(this);

        Intent intent = getIntent();
        gameName = intent.getStringExtra("gameName");
        difficulty = intent.getStringExtra("difficulty");
        assert difficulty != null;
        if(gameName.matches("Sudoku6")){
            setContentView(R.layout.activity_game_sudoku6);
            SudokuUtils.gridSize=6;
        }
        else if(gameName.matches("Sudoku9")) {
            setContentView(R.layout.activity_game_sudoku9);
            SudokuUtils.gridSize = 9;
        }
        else{
            setContentView(R.layout.activity_game_sudoku9);
            SudokuUtils.gridSize=9;
        }

        if(difficulty.equals("Easy") || difficulty.equals("Kolay")) {
            ((TextView)findViewById(R.id.diffTV_game)).setText(R.string.Easy);
            difficulty = "Easy";
        }
        else if(difficulty.equals("Medium") || difficulty.equals("Orta")) {
            ((TextView)findViewById(R.id.diffTV_game)).setText(R.string.Medium);
            difficulty = "Medium";
        }
        else {
            ((TextView)findViewById(R.id.diffTV_game)).setText(R.string.Hard);
            difficulty = "Hard";
        }

        mainFunc();

//        if (gameName != null && difficulty != null){
//            Toast.makeText(this, gameName+" / "+difficulty, Toast.LENGTH_SHORT).show();
//        }

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
            timerFunc();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onDestroy() {
        SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        try {
            ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Sudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<String>())));
            ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDSudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<Integer>())));
            Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));

            assert questions != null;
            questions.remove(0);


            Log.i("solvedQuestions",solvedQuestions+"");

            sharedPreferences.edit().putString("Sudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(questions)).apply();
            sharedPreferences.edit().putString("IDSudoku."+SudokuUtils.gridSize+"."+difficulty, ObjectSerializer.serialize(gameIds)).apply();
            sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize((Serializable) solvedQuestions)).apply();

            assert solvedQuestions != null;
            assert gameIds != null;
            Objects.requireNonNull(solvedQuestions.get("Sudoku." + SudokuUtils.gridSize+"."+difficulty)).add(gameIds.remove(0)+"-"+"0");

        } catch (IOException e) {
            e.printStackTrace();
        }


        super.onDestroy();
    }
}