package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameActivityPatika extends AppCompatActivity {

    String gameName;
    String difficulty;
    String clickedBox = "-1";
    String previousCoor;
    String[] rowColumn;
    String[][] lineGrid = new String[81][81];
    int gridSize = 5;
    int timerInSeconds = 0;
    int pxHeight = 900;
    boolean timerStopped=false;
    boolean paused = false;
    boolean gotQuestion = false;
    boolean is_moving = false;
    boolean solvedQuestion = false;

    List<String> operations = new ArrayList<>();
    List<String> opsForUndo = new ArrayList<>();
    List<String> clueIndexes = new ArrayList<>();
    LoadingDialog loadingDialog;
    Handler timerHandler;
    Runnable runnable;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;

    List<String> blackList = new ArrayList<>();
    List<String> answerCornerRD = new ArrayList<>();
    List<String> answerCornerRU = new ArrayList<>();
    List<String> answerCornerLD = new ArrayList<>();
    List<String> answerCornerLU = new ArrayList<>();
    List<String> answerEdgeRL = new ArrayList<>();
    List<String> answerEdgeUD = new ArrayList<>();


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
                    ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Patika."+gridSize, ObjectSerializer.serialize(new ArrayList<String>())));
                    ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDPatika."+gridSize, ObjectSerializer.serialize(new ArrayList<Integer>())));
                    Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));

                    assert questions != null;
                    questions.remove(0);

                    assert solvedQuestions != null;
                    assert gameIds != null;
                    Objects.requireNonNull(solvedQuestions.get("Patika." + gridSize)).add(gameIds.remove(0)+"-"+"0");

                    Log.i("solvedQuestions",solvedQuestions+"");

                    sharedPreferences.edit().putString("Patika."+gridSize, ObjectSerializer.serialize(questions)).apply();
                    sharedPreferences.edit().putString("IDPatika."+gridSize, ObjectSerializer.serialize(gameIds)).apply();
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
        if(opsForUndo.size() > 2){
            String op = opsForUndo.get(opsForUndo.size()-1);
            String previousC = op.substring(0,2);
            String currentC = op.substring(2,4);
            int[] firstMP = middlePoint(previousC);
            int[] secondMP = middlePoint(currentC);
            if(op.charAt(4) == '+'){
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                paint.setStrokeWidth((float)pxHeight/60);
                drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1],true);
                removeLine(previousC,currentC);
                for(int i = operations.size()-1; i >= 0; i--){
                    if(operations.get(i).equals(previousC + currentC)){
                        operations.remove(i);
                        break;
                    }
                }
                removeLine(currentC,previousC);
                for(int i = operations.size()-1; i >= 0; i--){
                    if(operations.get(i).equals(currentC + previousC)){
                        operations.remove(i);
                        break;
                    }
                }
                paint.setStrokeWidth((float)pxHeight/75);
                paint.setXfermode(null);

            } else {
                drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1],false);
                addLine(previousC, currentC);
                operations.add(previousC+currentC);
            }
            for(int i = opsForUndo.size()-1; i >= 0; i--){
                if(opsForUndo.get(i).equals(op)){
                    opsForUndo.remove(i);
                    break;
                }
            }
            Log.i("opsforUndo",opsForUndo.toString());
//            operations = operations.subList(0,operations.size()-1);
//            List<String> tuple1 = operations.get(operations.size()-1);
//            operations = operations.subList(0,operations.size()-1);
//            String co1 = tuple1.get(0);
//            String num2 = "0";
//            String co2;
//            for(int i = operations.size()-1; i>0; i--){
//                List<String> tuple2 = operations.get(i);
//                co2 = tuple2.get(0);
//                if(co1.equals(co2)){
//                    num2 = tuple2.get(1);
//                    break;
//                }
//            }
//            Log.i("co/num",co1+" / "+num2);
//            GridLayout gridLayout = findViewById(R.id.gridGL_ga);
//            TextView currentBox = gridLayout.findViewWithTag(co1);
//            if(num2.equals("-1")){
//                currentBox.setBackground(getResources().getDrawable(R.drawable.ic_diamond));
//            }
//            else if(num2.equals("-2")){
//                currentBox.setBackground(getResources().getDrawable(R.drawable.ic_cross));
//            }
//            else{
//                currentBox.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
//            }
        }
    }

    public void resetGrid(View view){
        try {
            final TextView resetTV = (TextView) view;
            resetTV.setTextColor(getResources().getColor(R.color.light_red));
            resetTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            resetTV.setText(R.string.ResetNormal);
            resetTV.postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetTV.setTextColor(getResources().getColorStateList(R.color.reset_selector_tvcolor));
                    resetTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    resetTV.setText(R.string.ResetUnderlined);
                }
            }, 100);

            operations = new ArrayList<>();
            operations.add("--");
            operations.add("--");
            opsForUndo = new ArrayList<>();
            opsForUndo.add("--");
            opsForUndo.add("--");

            bitmap.eraseColor(Color.TRANSPARENT);
            canvas = new Canvas(bitmap);

            for (int i = 0; i < gridSize; i++){
                for(int j = 0; j < gridSize; j++){
                    if(lineGrid[i][j].length() <=2){
                        lineGrid[i][j] = "";
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkAnswer(View view){
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        boolean checking=true;
        for(String s : answerCornerRD){
            int r = Integer.parseInt(String.valueOf(s.charAt(0)));
            int c = Integer.parseInt(String.valueOf(s.charAt(1)));
            if(!lineGrid[r][c].equals("rd") && !lineGrid[r][c].equals("dr")){
                checking = false;
                break;
            }
        }
        if(checking){
            SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
            try {
                ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Patika."+gridSize, ObjectSerializer.serialize(new ArrayList<String>())));
                ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDPatika."+gridSize, ObjectSerializer.serialize(new ArrayList<Integer>())));
                Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));

                assert questions != null;
                questions.remove(0);

                assert solvedQuestions != null;
                assert gameIds != null;
                Objects.requireNonNull(solvedQuestions.get("Patika." + gridSize)).add(gameIds.remove(0)+"-"+timerInSeconds);

                Log.i("solvedQuestions++++",solvedQuestions+"");

                sharedPreferences.edit().putString("Patika."+gridSize, ObjectSerializer.serialize(questions)).apply();
                sharedPreferences.edit().putString("IDPatika."+gridSize, ObjectSerializer.serialize(gameIds)).apply();
                sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize((Serializable) solvedQuestions)).apply();

            } catch (IOException e) {
                e.printStackTrace();
            }


            for(String s : answerCornerRU){
                int r = Integer.parseInt(String.valueOf(s.charAt(0)));
                int c = Integer.parseInt(String.valueOf(s.charAt(1)));
                if(!lineGrid[r][c].equals("ru") && !lineGrid[r][c].equals("ur")){
                    checking = false;
                    break;
                }
            }
        }
        if(checking){
            for(String s : answerCornerLD){
                int r = Integer.parseInt(String.valueOf(s.charAt(0)));
                int c = Integer.parseInt(String.valueOf(s.charAt(1)));
                if(!lineGrid[r][c].equals("ld") && !lineGrid[r][c].equals("dl")){
                    checking = false;
                    break;
                }
            }
        }
        if(checking){
            for(String s : answerCornerLU){
                int r = Integer.parseInt(String.valueOf(s.charAt(0)));
                int c = Integer.parseInt(String.valueOf(s.charAt(1)));
                if(!lineGrid[r][c].equals("lu") && !lineGrid[r][c].equals("ul")){
                    checking = false;
                    break;
                }
            }
        }
        if(checking){
            for(String s : answerEdgeRL){
                int r = Integer.parseInt(String.valueOf(s.charAt(0)));
                int c = Integer.parseInt(String.valueOf(s.charAt(1)));
                if(!lineGrid[r][c].equals("rl") && !lineGrid[r][c].equals("lr")){
                    checking = false;
                    break;
                }
            }
        }
        if(checking){
            for(String s : answerEdgeUD){
                int r = Integer.parseInt(String.valueOf(s.charAt(0)));
                int c = Integer.parseInt(String.valueOf(s.charAt(1)));
                if(!lineGrid[r][c].equals("ud") && !lineGrid[r][c].equals("du")){
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
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
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
                    questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Patika." + gridSize, ObjectSerializer.serialize(new ArrayList<String>())));
                    gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDPatika." + gridSize, ObjectSerializer.serialize(new ArrayList<Integer>())));
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
                Log.i("solvedQuestion", Objects.requireNonNull(solvedQuestions.get("Patika."+gridSize)).toString()+"ss");
                for(int i = 0; i < idArray.length(); i++){
                    if(!gameIds.contains(idArray.getInt(i))&&!Objects.requireNonNull(solvedQuestions.get("Patika."+gridSize)).toString().contains(idArray.getInt(i)+"-")) {
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
                sharedPreferences.edit().putString("Patika."+gridSize, ObjectSerializer.serialize(questions)).apply();
                sharedPreferences.edit().putString("IDPatika."+gridSize, ObjectSerializer.serialize(gameIds)).apply();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Log.i("gameIds", ObjectSerializer.deserialize(sharedPreferences.getString("IDPatika." + gridSize, ObjectSerializer.serialize(new ArrayList<Integer>()))) +"");
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
            blackList.add(cos);
            lineGrid[co.getInt(0)][co.getInt(1)] = "rldu";
        }

        JSONArray rd = (JSONArray) grid.get(1);
        for(int rdi = 0; rdi < rd.length(); rdi++){
            JSONArray co = (JSONArray) rd.get(rdi);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            answerCornerRD.add(cos);
        }

        JSONArray ru = (JSONArray) grid.get(2);
        for(int rui = 0; rui < ru.length(); rui++){
            JSONArray co = (JSONArray) ru.get(rui);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            answerCornerRU.add(cos);
        }

        JSONArray ld = (JSONArray) grid.get(3);
        for(int ldi = 0; ldi < ld.length(); ldi++){
            JSONArray co = (JSONArray) ld.get(ldi);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            answerCornerLD.add(cos);
        }

        JSONArray lu = (JSONArray) grid.get(4);
        for(int lui = 0; lui < lu.length(); lui++){
            JSONArray co = (JSONArray) lu.get(lui);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            answerCornerLU.add(cos);
        }

        JSONArray rl = (JSONArray) grid.get(5);
        for(int rli = 0; rli < rl.length(); rli++){
            JSONArray co = (JSONArray) rl.get(rli);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            answerEdgeRL.add(cos);
        }

        JSONArray ud = (JSONArray) grid.get(6);
        for(int udi = 0; udi < ud.length(); udi++){
            JSONArray co = (JSONArray) ud.get(udi);
            String cos = (co.getInt(0))+(Integer.toString(co.getInt(1)));
            answerEdgeUD.add(cos);
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
        operations = new ArrayList<>();
        operations.add("--");
        operations.add("--");
        opsForUndo = new ArrayList<>();
        opsForUndo.add("--");
        opsForUndo.add("--");

        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                tv.setText("");
                tv.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                tv.setEnabled(true);
            }
        }
        clickedBox = "-1";
        clueIndexes = new ArrayList<>();
        timerInSeconds = 0;
        timerStopped=true;
        try{
            bitmap.eraseColor(Color.TRANSPARENT);
            canvas = new Canvas(bitmap);
        } catch (Exception e){
            e.printStackTrace();
        }

        for (int i = 0; i < gridSize; i++){
            for(int j = 0; j < gridSize; j++){
                lineGrid[i][j] = "";
            }
        }
        blackList = new ArrayList<>();
        answerCornerRD = new ArrayList<>();
        answerCornerRU = new ArrayList<>();
        answerCornerLD = new ArrayList<>();
        answerCornerLU = new ArrayList<>();
        answerEdgeRL = new ArrayList<>();
        answerEdgeUD = new ArrayList<>();

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
        getRequest.execute("https://akiloyunlariapp.herokuapp.com/Patika."+gridSize,"fx!Ay:;<p6Q?C8N{");
        loadingDialogFunc();
    }

    public void initSomeVar(){
        bitmap = Bitmap.createBitmap(pxHeight, pxHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(getResources().getColor(R.color.transparent));
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.near_black_blue));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float)pxHeight/75);
        paint.setAntiAlias(true);

        for (int i = 0; i < gridSize; i++){
            for(int j = 0; j < gridSize; j++){
                lineGrid[j][i] = "";
            }
        }

    }

    public String[] xyToRowColumn(float x, float y){
        String[] rowColumn = new String[2];
        float coef = (float)pxHeight/gridSize;
        rowColumn[0] = Integer.toString((int) Math.floor(x/coef));
        rowColumn[1] = Integer.toString((int) Math.floor(y/coef));
        return rowColumn;
    }

    public int[] middlePoint(String coor){
        int x = Integer.parseInt(String.valueOf(coor.charAt(0)));
        int y = Integer.parseInt(String.valueOf(coor.charAt(1)));
        int[] middle_point = new int[2];
        float coef = (float)pxHeight/gridSize;
        middle_point[0] = (int) (coef*x+coef/2);
        middle_point[1] = (int) (coef*y+coef/2);
        return middle_point;
    }

    public void drawALine(float startX, float startY, float stopX, float stopY, boolean erasing){
        ImageView imageView = findViewById(R.id.canvasIV);
//        Log.i("x1,y1,x2,y2",startX+"  "+startY+"  "+stopX+"  "+stopY);
        if(!erasing) {
            int offset = pxHeight / 150;
            if (startY - stopY == 0) {
                if (startX < stopX)
                    canvas.drawLine(startX - offset, startY, stopX + offset, stopY, paint);
                else canvas.drawLine(startX + offset, startY, stopX - offset, stopY, paint);
            } else {
                if (startY < stopY)
                    canvas.drawLine(startX, startY - offset, stopX, stopY + offset, paint);
                else canvas.drawLine(startX, startY + offset, stopX, stopY - offset, paint);
            }
        }
        else{
            String[] previousC = xyToRowColumn(startX,startY);
            int px = Integer.parseInt(previousC[0]);
            int py = Integer.parseInt(previousC[1]);
            String[] currentC = xyToRowColumn(stopX,stopY);
            int cx = Integer.parseInt(currentC[0]);
            int cy = Integer.parseInt(currentC[1]);
            int offset1 = pxHeight / 120;
            int offset2 = pxHeight / 120;
            if (startY - stopY == 0) {
                if(lineGrid[px][py].length() == 2){
                    offset1 = -pxHeight/140;
                }
                if(lineGrid[cx][cy].length() == 2){
                    offset2 = -pxHeight/140;
                }
                if (startX < stopX)
                    canvas.drawLine(startX - offset1, startY, stopX + offset2, stopY, paint);
                else canvas.drawLine(startX + offset1, startY, stopX - offset2, stopY, paint);
            } else {
                if(lineGrid[px][py].length() == 2){
                    offset1 = -pxHeight/140;
                }
                if(lineGrid[cx][cy].length() == 2){
                    offset2 = -pxHeight/140;
                }
                if (startY < stopY)
                    canvas.drawLine(startX, startY - offset1, stopX, stopY + offset2, paint);
                else canvas.drawLine(startX, startY + offset1, stopX, stopY - offset2, paint);
            }
        }
        imageView.setImageBitmap(bitmap);
    }

    public void addLine(String firstRC, String secondRC){
        int r1 = Integer.parseInt(String.valueOf(firstRC.charAt(0)));
        int c1 = Integer.parseInt(String.valueOf(firstRC.charAt(1)));
        int r2 = Integer.parseInt(String.valueOf(secondRC.charAt(0)));
        int c2 = Integer.parseInt(String.valueOf(secondRC.charAt(1)));
        if(r1 == r2){ // vertical line
            if(c1 < c2){ // first one is above second
                lineGrid[r1][c1] += "d";
                lineGrid[r2][c2] += "u";
            }
            else if(c1 > c2){ // first one is below second
                lineGrid[r1][c1] += "u";
                lineGrid[r2][c2] += "d";
            }
        }
        else if(c1 == c2){ // horizontal line
            if(r1 < r2){ // first one is left of second
                lineGrid[r1][c1] += "r";
                lineGrid[r2][c2] += "l";
            }
            else { // first one is right of second
                lineGrid[r1][c1] += "l";
                lineGrid[r2][c2] += "r";
            }
        }
    }

    public void removeLine(String firstRC, String secondRC){
        int r1 = Integer.parseInt(String.valueOf(firstRC.charAt(0)));
        int c1 = Integer.parseInt(String.valueOf(firstRC.charAt(1)));
        int r2 = Integer.parseInt(String.valueOf(secondRC.charAt(0)));
        int c2 = Integer.parseInt(String.valueOf(secondRC.charAt(1)));
        if(r1 == r2){ // vertical line
            if(c1 < c2){ // first one is above second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("d","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("u","");
            }
            else if(c1 > c2){ // first one is below second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("u","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("d","");
            }
        }
        else if(c1 == c2){ // horizontal line
            if(r1 < r2){ // first one is left of second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("r","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("l","");
            }
            else { // first one is right of second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("l","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("r","");

            }
        }
//        Log.i("eraseModeRemoveLine",r1+" "+c1+" "+lineGrid[r1][c1]+" / "+r2+" "+c2+" "+lineGrid[r2][c2]);
    }

    public boolean isMoreLineCanBeAdded(String coor){
        int r = Integer.parseInt(String.valueOf(coor.charAt(0)));
        int c = Integer.parseInt(String.valueOf(coor.charAt(1)));
        Log.i("rc",r+"  "+c);
        return lineGrid[r][c].length() < 2;
    }

    public boolean lineCanBeDrawn(String currentC, String previousC){
//        Log.i("eraseModeLineCanBeDrawn","---------------------------"+previousC+"--"+currentC);
//        Log.i("eraseModeLineCanBeDrawn","!currentC.equals(previousC)"+ !currentC.equals(previousC));
//        Log.i("eraseModeLineCanBeDrawn","not cross or more than one box"+ ((currentC.charAt(0) == previousC.charAt(0)
//                && Math.abs(Integer.parseInt(String.valueOf(previousC.charAt(1))) - Integer.parseInt(String.valueOf(currentC.charAt(1)))) == 1)
//                ||      (currentC.charAt(1) == previousC.charAt(1)
//                && Math.abs(Integer.parseInt(String.valueOf(previousC.charAt(0))) - Integer.parseInt(String.valueOf(currentC.charAt(0)))) == 1)
//        ));
//        Log.i("eraseModeLineCanBeDrawn","isMoreLineCanBeAdded(previousC)"+ !isMoreLineCanBeAdded(previousC));
//        Log.i("eraseModeLineCanBeDrawn","isMoreLineCanBeAdded(currentC)"+ !isMoreLineCanBeAdded(currentC));
//

        return (!currentC.equals(previousC)
                && (
                (currentC.charAt(0) == previousC.charAt(0)
                        && Math.abs(Integer.parseInt(String.valueOf(previousC.charAt(1))) - Integer.parseInt(String.valueOf(currentC.charAt(1)))) == 1)
                        ||      (currentC.charAt(1) == previousC.charAt(1)
                        && Math.abs(Integer.parseInt(String.valueOf(previousC.charAt(0))) - Integer.parseInt(String.valueOf(currentC.charAt(0)))) == 1)
                    )
                && isMoreLineCanBeAdded(previousC) && isMoreLineCanBeAdded(currentC));
    }

    public boolean isGridFull(){
        boolean isfull = true;
        for(int i = 0; i < gridSize; i++){
            for(int j = 0; j < gridSize; j++){
                if(lineGrid[i][j].length() < 2){
                    isfull=false;
                    break;
                }
            }
        }
        return isfull;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        gameName = intent.getStringExtra("gameName");
        difficulty = intent.getStringExtra("difficulty");
        assert difficulty != null;
        if(difficulty.equals("Easy") || difficulty.equals("Kolay")){
            setContentView(R.layout.activity_game_patika5);
            Log.i("diff","easy");
            gridSize=5;
        }
        else if(difficulty.equals("Medium") || difficulty.equals("Orta")){
            setContentView(R.layout.activity_game_patika7);
            gridSize=7;
            Log.i("diff","medium");
        }
        else{
            setContentView(R.layout.activity_game_patika9);
            gridSize=9;
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
        pxHeight = (int) (300 * getResources().getDisplayMetrics().density);
        initSomeVar();
        gridLayout.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float mx = motionEvent.getX();
                float my = motionEvent.getY();
                Log.i("x / y",mx + " / " + my);
                if(mx >= 0 && mx <= pxHeight && my >= 0 && my <= pxHeight){
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            rowColumn = xyToRowColumn(mx,my);
                            previousCoor = rowColumn[0] + rowColumn[1];
                            is_moving=false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            rowColumn = xyToRowColumn(mx,my);
                            String currentCoor = rowColumn[0] + rowColumn[1];
//                            Log.i("coor",currentCoor);
                            if(lineCanBeDrawn(currentCoor,previousCoor) || (operations.contains(previousCoor+currentCoor) || operations.contains(currentCoor+previousCoor))){
                                is_moving=true;
                                int[] firstMP = middlePoint(previousCoor);
                                int[] secondMP = middlePoint(currentCoor);
                                if((operations.contains(previousCoor+currentCoor) || operations.contains(currentCoor+previousCoor))){
//                                    Log.i("eraseMode","ON");
                                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//                                    paint.setColor(Color.TRANSPARENT);
                                    paint.setStrokeWidth((float)pxHeight/60);
                                    drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1],true);
                                    if(operations.contains(previousCoor+currentCoor)){
                                        removeLine(previousCoor,currentCoor);
                                        operations.remove(previousCoor+currentCoor);
                                    }
                                    else{
                                        removeLine(currentCoor,previousCoor);
                                        operations.remove(currentCoor+previousCoor);
                                    }
                                    opsForUndo.add(previousCoor+currentCoor+"-");
                                    paint.setXfermode(null);
//                                    paint.setColor(getResources().getColor(R.color.near_black_blue));
                                    paint.setStrokeWidth((float)pxHeight/75);
                                }
                                else{
                                    Log.i("eraseMode","OFF  "+currentCoor+ "  "+ previousCoor);
                                    drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1],false);
                                    addLine(previousCoor, currentCoor);
                                    operations.add(previousCoor+currentCoor);
                                    opsForUndo.add(previousCoor+currentCoor+"+");
                                }
                                previousCoor = currentCoor;
                                if(isGridFull()){
                                    checkAnswer(null);
                                }
                                Log.i("LineGrid", Arrays.deepToString(lineGrid));
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
            ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Patika."+gridSize, ObjectSerializer.serialize(new ArrayList<String>())));
            ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDPatika."+gridSize, ObjectSerializer.serialize(new ArrayList<Integer>())));
            ArrayList<String> solvedQuestions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new ArrayList<String>())));

            assert questions != null;
            questions.remove(0);

            sharedPreferences.edit().putString("Patika."+gridSize, ObjectSerializer.serialize(questions)).apply();
            sharedPreferences.edit().putString("IDPatika."+gridSize, ObjectSerializer.serialize(gameIds)).apply();
            sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize(solvedQuestions)).apply();

            assert solvedQuestions != null;
            assert gameIds != null;
            solvedQuestions.add("Patika."+gridSize+","+gameIds.remove(0)+"-"+"0");


        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

}