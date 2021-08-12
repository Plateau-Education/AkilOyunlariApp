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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.yaquila.akiloyunlariapp.gameutils.PatikaUtils;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GameActivityPatika extends AppCompatActivity {

    String gameName;
    String difficulty;
    int timerInSeconds = 0;
    boolean timerStopped=false;
    boolean paused = false;
    boolean gotQuestion = false;
    boolean is_moving = false;
    boolean solvedQuestion = false;

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
                    ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Patika."+PatikaUtils.gridSize, ObjectSerializer.serialize(new ArrayList<String>())));
                    ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDPatika."+PatikaUtils.gridSize, ObjectSerializer.serialize(new ArrayList<Integer>())));
                    Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));

                    assert questions != null;
                    questions.remove(0);

                    assert solvedQuestions != null;
                    assert gameIds != null;
                    Objects.requireNonNull(solvedQuestions.get("Patika." + PatikaUtils.gridSize)).add(gameIds.remove(0)+"-"+"0");

                    Log.i("solvedQuestions",solvedQuestions+"");

                    sharedPreferences.edit().putString("Patika."+PatikaUtils.gridSize, ObjectSerializer.serialize(questions)).apply();
                    sharedPreferences.edit().putString("IDPatika."+PatikaUtils.gridSize, ObjectSerializer.serialize(gameIds)).apply();
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void undoOperation(View view){
        PatikaUtils.undoOperation((ImageView) findViewById(R.id.canvasIV));
    }

    public void resetGrid(View view){
        PatikaUtils.resetGrid(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkAnswer(View view){
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        boolean checking=true;
        for(String s : PatikaUtils.answerCornerRD){
            int r = Integer.parseInt(String.valueOf(s.charAt(0)));
            int c = Integer.parseInt(String.valueOf(s.charAt(1)));
            if(!PatikaUtils.lineGrid[r][c].equals("rd") && !PatikaUtils.lineGrid[r][c].equals("dr")){
                checking = false;
                break;
            }
        }
        if(checking){
            SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
            try {
                ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Patika."+PatikaUtils.gridSize, ObjectSerializer.serialize(new ArrayList<String>())));
                ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDPatika."+PatikaUtils.gridSize, ObjectSerializer.serialize(new ArrayList<Integer>())));
                Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));

                assert questions != null;
                questions.remove(0);

                assert solvedQuestions != null;
                assert gameIds != null;
                Objects.requireNonNull(solvedQuestions.get("Patika." + PatikaUtils.gridSize)).add(gameIds.remove(0)+"-"+timerInSeconds);

                Log.i("solvedQuestions++++",solvedQuestions+"");

                sharedPreferences.edit().putString("Patika."+PatikaUtils.gridSize, ObjectSerializer.serialize(questions)).apply();
                sharedPreferences.edit().putString("IDPatika."+PatikaUtils.gridSize, ObjectSerializer.serialize(gameIds)).apply();
                sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize((Serializable) solvedQuestions)).apply();

            } catch (IOException e) {
                e.printStackTrace();
            }


            for(String s : PatikaUtils.answerCornerRU){
                int r = Integer.parseInt(String.valueOf(s.charAt(0)));
                int c = Integer.parseInt(String.valueOf(s.charAt(1)));
                if(!PatikaUtils.lineGrid[r][c].equals("ru") && !PatikaUtils.lineGrid[r][c].equals("ur")){
                    checking = false;
                    break;
                }
            }
        }
        if(checking){
            for(String s : PatikaUtils.answerCornerLD){
                int r = Integer.parseInt(String.valueOf(s.charAt(0)));
                int c = Integer.parseInt(String.valueOf(s.charAt(1)));
                if(!PatikaUtils.lineGrid[r][c].equals("ld") && !PatikaUtils.lineGrid[r][c].equals("dl")){
                    checking = false;
                    break;
                }
            }
        }
        if(checking){
            for(String s : PatikaUtils.answerCornerLU){
                int r = Integer.parseInt(String.valueOf(s.charAt(0)));
                int c = Integer.parseInt(String.valueOf(s.charAt(1)));
                if(!PatikaUtils.lineGrid[r][c].equals("lu") && !PatikaUtils.lineGrid[r][c].equals("ul")){
                    checking = false;
                    break;
                }
            }
        }
        if(checking){
            for(String s : PatikaUtils.answerEdgeRL){
                int r = Integer.parseInt(String.valueOf(s.charAt(0)));
                int c = Integer.parseInt(String.valueOf(s.charAt(1)));
                if(!PatikaUtils.lineGrid[r][c].equals("rl") && !PatikaUtils.lineGrid[r][c].equals("lr")){
                    checking = false;
                    break;
                }
            }
        }
        if(checking){
            for(String s : PatikaUtils.answerEdgeUD){
                int r = Integer.parseInt(String.valueOf(s.charAt(0)));
                int c = Integer.parseInt(String.valueOf(s.charAt(1)));
                if(!PatikaUtils.lineGrid[r][c].equals("ud") && !PatikaUtils.lineGrid[r][c].equals("du")){
                    checking = false;
                    break;
                }
            }
        }
        if(checking){
            timerStopped = true;
            solvedQuestion = true;
            LayoutInflater factory = LayoutInflater.from(this);
            final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
            final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
            TextView timerTV = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
            timerTV.setText(formatTime(timerInSeconds));
            correctDialog.setView(leaveDialogView);

            findViewById(R.id.clickView).setVisibility(View.VISIBLE);
            TextView undoTV = findViewById(R.id.undoTV_ga);
            TextView resetTV = findViewById(R.id.resetTV_game);
            for (int i = 0; i < PatikaUtils.gridSize; i++) {
                for (int j = 0; j < PatikaUtils.gridSize; j++) {
                    gridLayout.findViewWithTag(Integer.toString(j) + i).setEnabled(false);
                }
            }
            undoTV.setEnabled(false);
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
                    questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Patika." + PatikaUtils.gridSize, ObjectSerializer.serialize(new ArrayList<String>())));
                    gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDPatika." + PatikaUtils.gridSize, ObjectSerializer.serialize(new ArrayList<Integer>())));
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
            //noinspection deprecation
            super.onPostExecute(result);

            try {
                org.json.JSONObject jb = new org.json.JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));
                JSONArray gridArrays = (JSONArray)jb.get("Info");
                JSONArray idArray = (JSONArray)jb.get("Ids");
                Log.i("idarray",idArray.toString()+"  "+idArray.length()+"    ga:"+gridArrays.length());
                Map<String, ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));
                assert solvedQuestions != null;
                Log.i("solvedQuestion", Objects.requireNonNull(solvedQuestions.get("Patika."+PatikaUtils.gridSize)).toString()+"ss");
                for(int i = 0; i < idArray.length(); i++){
                    if(!gameIds.contains(idArray.getInt(i))&&!Objects.requireNonNull(solvedQuestions.get("Patika."+PatikaUtils.gridSize)).toString().contains(idArray.getInt(i)+"-")) {
                        questions.add(gridArrays.getJSONArray(i).getJSONArray(0).getJSONArray(0).toString());
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
                sharedPreferences.edit().putString("Patika."+PatikaUtils.gridSize, ObjectSerializer.serialize(questions)).apply();
                sharedPreferences.edit().putString("IDPatika."+PatikaUtils.gridSize, ObjectSerializer.serialize(gameIds)).apply();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Log.i("gameIds", ObjectSerializer.deserialize(sharedPreferences.getString("IDPatika." + PatikaUtils.gridSize, ObjectSerializer.serialize(new ArrayList<Integer>()))) +"");
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
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);

        JSONArray bl = (JSONArray) grid.get(0);
        for(int bli = 0; bli < bl.length(); bli++){
            JSONArray co = (JSONArray) bl.get(bli);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            gridLayout.findViewWithTag(cos).setBackground(getResources().getDrawable(R.color.near_black_blue));
            PatikaUtils.blackList.add(cos);
            PatikaUtils.lineGrid[co.getInt(0)][co.getInt(1)] = "rldu";
        }

        JSONArray rd = (JSONArray) grid.get(1);
        for(int rdi = 0; rdi < rd.length(); rdi++){
            JSONArray co = (JSONArray) rd.get(rdi);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            PatikaUtils.answerCornerRD.add(cos);
        }

        JSONArray ru = (JSONArray) grid.get(2);
        for(int rui = 0; rui < ru.length(); rui++){
            JSONArray co = (JSONArray) ru.get(rui);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            PatikaUtils.answerCornerRU.add(cos);
        }

        JSONArray ld = (JSONArray) grid.get(3);
        for(int ldi = 0; ldi < ld.length(); ldi++){
            JSONArray co = (JSONArray) ld.get(ldi);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            PatikaUtils.answerCornerLD.add(cos);
        }

        JSONArray lu = (JSONArray) grid.get(4);
        for(int lui = 0; lui < lu.length(); lui++){
            JSONArray co = (JSONArray) lu.get(lui);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            PatikaUtils.answerCornerLU.add(cos);
        }

        JSONArray rl = (JSONArray) grid.get(5);
        for(int rli = 0; rli < rl.length(); rli++){
            JSONArray co = (JSONArray) rl.get(rli);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            PatikaUtils.answerEdgeRL.add(cos);
        }

        JSONArray ud = (JSONArray) grid.get(6);
        for(int udi = 0; udi < ud.length(); udi++){
            JSONArray co = (JSONArray) ud.get(udi);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            PatikaUtils.answerEdgeUD.add(cos);
        }
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
        loadingDialog = new LoadingDialog(GameActivityPatika.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
        loadingDialog.startLoadingAnimation();
    }

    public void clearGrid(){
        PatikaUtils.clearGrid();
        timerInSeconds = 0;
        timerStopped=true;
    }

    public void mainFunc(){
        TextView undoTV = findViewById(R.id.undoTV_ga);
        TextView resetTV = findViewById(R.id.resetTV_game);
        undoTV.setEnabled(true);
        resetTV.setEnabled(true);
        findViewById(R.id.clickView).setVisibility(View.GONE);
        clearGrid();
        GetRequest getRequest = new GetRequest();
        //noinspection deprecation
        getRequest.execute("https://akiloyunlariapp.herokuapp.com/Patika."+PatikaUtils.gridSize,"fx!Ay:;<p6Q?C8N{");
        loadingDialogFunc();
    }

    public void initSomeVar(){
        PatikaUtils.initSomeVar();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PatikaUtils.initVars(this);

        Intent intent = getIntent();
        gameName = intent.getStringExtra("gameName");
        difficulty = intent.getStringExtra("difficulty");
        assert difficulty != null;
        if(difficulty.equals("Easy") || difficulty.equals("Kolay")){
            setContentView(R.layout.activity_game_patika5);
            Log.i("diff","easy");
            PatikaUtils.gridSize=5;
        }
        else if(difficulty.equals("Medium") || difficulty.equals("Orta")){
            setContentView(R.layout.activity_game_patika7);
            PatikaUtils.gridSize=7;
            Log.i("diff","medium");
        }
        else{
            setContentView(R.layout.activity_game_patika9);
            PatikaUtils.gridSize=9;
            Log.i("diff","hard");
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
        PatikaUtils.pxHeight = (int) (300 * getResources().getDisplayMetrics().density);
        initSomeVar();
        gridLayout.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float mx = motionEvent.getX();
                float my = motionEvent.getY();
                Log.i("x / y",mx + " / " + my);
                if(mx >= 0 && mx <= PatikaUtils.pxHeight && my >= 0 && my <= PatikaUtils.pxHeight){
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            PatikaUtils.rowColumn = PatikaUtils.xyToRowColumn(mx,my);
                            PatikaUtils.previousCoor = PatikaUtils.rowColumn[0] + PatikaUtils.rowColumn[1];
                            is_moving=false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            PatikaUtils.rowColumn = PatikaUtils.xyToRowColumn(mx,my);
                            String currentCoor = PatikaUtils.rowColumn[0] + PatikaUtils.rowColumn[1];
//                            Log.i("coor",currentCoor);
                            if(PatikaUtils.lineCanBeDrawn(currentCoor,PatikaUtils.previousCoor) || (PatikaUtils.operations.contains(PatikaUtils.previousCoor+currentCoor) || PatikaUtils.operations.contains(currentCoor+PatikaUtils.previousCoor))){
                                is_moving=true;
                                int[] firstMP = PatikaUtils.middlePoint(PatikaUtils.previousCoor);
                                int[] secondMP = PatikaUtils.middlePoint(currentCoor);
                                if((PatikaUtils.operations.contains(PatikaUtils.previousCoor+currentCoor) || PatikaUtils.operations.contains(currentCoor+PatikaUtils.previousCoor))){
//                                    Log.i("eraseMode","ON");
                                    PatikaUtils.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//                                    paint.setColor(Color.TRANSPARENT);
                                    PatikaUtils.paint.setStrokeWidth((float)PatikaUtils.pxHeight/60);
                                    PatikaUtils.drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1],true,(ImageView) findViewById(R.id.canvasIV));
                                    if(PatikaUtils.operations.contains(PatikaUtils.previousCoor+currentCoor)){
                                        PatikaUtils.removeLine(PatikaUtils.previousCoor,currentCoor);
                                        PatikaUtils.operations.remove(PatikaUtils.previousCoor+currentCoor);
                                    }
                                    else{
                                        PatikaUtils.removeLine(currentCoor,PatikaUtils.previousCoor);
                                        PatikaUtils.operations.remove(currentCoor+PatikaUtils.previousCoor);
                                    }
                                    PatikaUtils.opsForUndo.add(PatikaUtils.previousCoor+currentCoor+"-");
                                    PatikaUtils.paint.setXfermode(null);
//                                    paint.setColor(getResources().getColor(R.color.near_black_blue));
                                    PatikaUtils.paint.setStrokeWidth((float)PatikaUtils.pxHeight/75);
                                }
                                else{
                                    Log.i("eraseMode","OFF  "+currentCoor+ "  "+ PatikaUtils.previousCoor);
                                    PatikaUtils.drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1],false,(ImageView) findViewById(R.id.canvasIV));
                                    PatikaUtils.addLine(PatikaUtils.previousCoor, currentCoor);
                                    PatikaUtils.operations.add(PatikaUtils.previousCoor+currentCoor);
                                    PatikaUtils.opsForUndo.add(PatikaUtils.previousCoor+currentCoor+"+");
                                }
                                PatikaUtils.previousCoor = currentCoor;
                                if(PatikaUtils.isGridFull()){
                                    checkAnswer(null);
                                }
                                Log.i("PatikaUtils.lineGrid", Arrays.deepToString(PatikaUtils.lineGrid));
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if(!is_moving){
                                Log.i("Pressed","Pressed");
                            }
//                            if(isGridFull()){
//                                checkAnswer(null);
//                            }
                            break;
                    }
                }

                return true;
            }
        });

        mainFunc();

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

    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        try {
            ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Patika."+PatikaUtils.gridSize, ObjectSerializer.serialize(new ArrayList<String>())));
            ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDPatika."+PatikaUtils.gridSize, ObjectSerializer.serialize(new ArrayList<Integer>())));
            ArrayList<String> solvedQuestions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new ArrayList<String>())));

            assert questions != null;
            questions.remove(0);

            sharedPreferences.edit().putString("Patika."+PatikaUtils.gridSize, ObjectSerializer.serialize(questions)).apply();
            sharedPreferences.edit().putString("IDPatika."+PatikaUtils.gridSize, ObjectSerializer.serialize(gameIds)).apply();
            sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize(solvedQuestions)).apply();

            assert solvedQuestions != null;
            assert gameIds != null;
            solvedQuestions.add("Patika."+PatikaUtils.gridSize+","+gameIds.remove(0)+"-"+"0");


        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

}