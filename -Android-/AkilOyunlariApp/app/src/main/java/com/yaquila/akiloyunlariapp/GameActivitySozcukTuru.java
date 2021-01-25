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
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameActivitySozcukTuru extends AppCompatActivity {

    String gameName;
    String difficulty;
    String previousCoor;
    String answer = "";
    String[] rowColumn;
    String[][] lineGrid = new String[81][81];
    int gridSizeX = 5;
    int gridSizeY = 5;
    int timerInSeconds = 0;
    int pxHeightX = 900;
    int pxHeightY = 900;
    boolean timerStopped=false;
    boolean paused = false;
    boolean gotQuestion = false;
    boolean is_moving = false;
    boolean solvedQuestion = false;

    List<String> operations = new ArrayList<>();
    List<String> opsForUndo = new ArrayList<>();
    LoadingDialog loadingDialog;
    Handler timerHandler;
    Runnable runnable;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;


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
                paint.setStrokeWidth((float)pxHeightY/60);
                drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1], true);
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
                paint.setXfermode(null);
                paint.setStrokeWidth((float)pxHeightY/75);

            } else {
                drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1], false);
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

            for (int i = 0; i < gridSizeX; i++){
                for(int j = 0; j < gridSizeY; j++){
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
        for(String s : operations.subList(2,operations.size())){
            String sR = s.substring(2) + s.substring(0,2);
            checking = answer.contains(s) || answer.contains(sR);
            Log.i("answer / s / sR",s + " / " + sR + " / " + answer);
            if(!checking) break;
        }
//        Log.i("answerComparison",opAnswer.toString()+" // "+answer);

        if(checking){

            SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
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
            LayoutInflater factory = LayoutInflater.from(this);
            final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
            final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
            TextView timerTV = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
            timerTV.setText(formatTime(timerInSeconds));
            correctDialog.setView(leaveDialogView);

            findViewById(R.id.clickView).setVisibility(View.VISIBLE);
            TextView undoTV = findViewById(R.id.undoTV_ga);
            TextView resetTV = findViewById(R.id.resetTV_game);
            for (int i = 0; i < gridSizeY; i++) {
                for (int j = 0; j < gridSizeX; j++) {
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
            //noinspection deprecation
            super.onPostExecute(result);

            try {
                org.json.JSONObject jb = new org.json.JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));
                JSONArray gridArrays = (JSONArray)jb.get("Info");
                JSONArray idArray = (JSONArray)jb.get("Ids");
                Log.i("idarray",idArray.toString()+"  "+idArray.length()+"    ga:"+gridArrays.length());
                Map<String, ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));
                assert solvedQuestions != null;
                Log.i("solvedQuestion", Objects.requireNonNull(solvedQuestions.get("SozcukTuru."+difficulty)).toString()+"ss");
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
                Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
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
            timerFunc();
            loadingDialog.dismissDialog();
        }
    }

    public void seperateGridAnswer(JSONArray grid) throws JSONException {
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        StringBuilder answerSB = new StringBuilder();
        answer = answer + ((JSONArray) grid.get(0)).getInt(0)+((JSONArray) grid.get(0)).getInt(1);
        StringBuilder words = new StringBuilder();
        for (int i = 0; i < grid.length(); i++){
            JSONArray box = ((JSONArray) grid.get(i));
            if(i != 0 && i != grid.length()-1)
                answerSB.append(box.getInt(0)).append(box.getInt(1)).append(" ").append(box.getInt(0)).append(box.getInt(1));
//                answer = answer + box.getInt(0)+box.getInt(1) + " " + box.getInt(0)+box.getInt(1);
            TextView currentTV = gridLayout.findViewWithTag(Integer.toString(box.getInt(0))+box.getInt(1));
            currentTV.setText(Character.toString((char)box.getInt(2)));
            words.append((char) box.getInt(2));
        }
        answer += answerSB.toString();
        answer = answer + ((JSONArray) grid.get(grid.length()-1)).getInt(0)+((JSONArray) grid.get(grid.length()-1)).getInt(1);

        Log.i("answerWords", words.toString());

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
//                Log.i("answer",answer);
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
        loadingDialog = new LoadingDialog(GameActivitySozcukTuru.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
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
        for (int i = 0; i < gridSizeY; i++) {
            for (int j = 0; j < gridSizeX; j++) {
                TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                tv.setText("");
                tv.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                tv.setEnabled(true);
            }
        }
        answer = "";
        timerInSeconds = 0;
        timerStopped=true;
        try{
            bitmap.eraseColor(Color.TRANSPARENT);
            canvas = new Canvas(bitmap);
        } catch (Exception e){
            e.printStackTrace();
        }

        for (int i = 0; i < gridSizeX; i++){
            for(int j = 0; j < gridSizeY; j++){
                lineGrid[i][j] = "";
            }
        }
    }

    public void mainFunc(){
        findViewById(R.id.clickView).setVisibility(View.GONE);
        TextView undoTV = findViewById(R.id.undoTV_ga);
        TextView resetTV = findViewById(R.id.resetTV_game);
        undoTV.setEnabled(true);
        resetTV.setEnabled(true);
        clearGrid();
        GetRequest getRequest = new GetRequest();
        //noinspection deprecation
        getRequest.execute("https://akiloyunlariapp.herokuapp.com/SozcukTuru."+difficulty,"fx!Ay:;<p6Q?C8N{");
        loadingDialogFunc();
    }

    public void initSomeVar(){
        bitmap = Bitmap.createBitmap(pxHeightX, pxHeightY, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(getResources().getColor(R.color.transparent));
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.shallow_light_red2));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float)pxHeightY/75);
        paint.setAntiAlias(true);

        for (int i = 0; i < gridSizeX; i++){
            for(int j = 0; j < gridSizeY; j++){
                lineGrid[j][i] = "";
            }
        }

    }

    public String[] xyToRowColumn(float x, float y){
        String[] rowColumn = new String[2];
        float coefX = (float)pxHeightX/gridSizeX;
        float coefY = (float)pxHeightY/gridSizeY;
        if((((x / coefX) - Math.floor(x / coefX)) >= 0.2f) && (((y / coefY) - Math.floor(y / coefY)) >= 0.2f)){
            rowColumn[0] = Integer.toString((int) Math.floor(x/coefX));
            rowColumn[1] = Integer.toString((int) Math.floor(y/coefY));
        }
        else{
            rowColumn[0] = "";
            rowColumn[1] = "";
        }
        return rowColumn;
    }

    public int[] middlePoint(String coor){
        int x = Integer.parseInt(String.valueOf(coor.charAt(0)));
        int y = Integer.parseInt(String.valueOf(coor.charAt(1)));
        int[] middle_point = new int[2];
        float coefX = (float)pxHeightX/gridSizeX;
        float coefY = (float)pxHeightY/gridSizeY;
        middle_point[0] = (int) (coefX*x+coefX/2);
        middle_point[1] = (int) (coefY*y+coefY/2);
        return middle_point;
    }

    public void drawALine(float startX, float startY, float stopX, float stopY, boolean erasing){
        ImageView imageView = findViewById(R.id.canvasIV);
//        Log.i("x1,y1,x2,y2",startX+"  "+startY+"  "+stopX+"  "+stopY);
        if(!erasing) {
            int offset = pxHeightY / 150;
            if (startY - stopY == 0 && startX - stopX != 0) {
                if (startX < stopX)
                    canvas.drawLine(startX - offset, startY, stopX + offset, stopY, paint);
                else canvas.drawLine(startX + offset, startY, stopX - offset, stopY, paint);
            } else if (startX - stopX == 0 && startY - stopY != 0){
                if (startY < stopY)
                    canvas.drawLine(startX, startY - offset, stopX, stopY + offset, paint);
                else canvas.drawLine(startX, startY + offset, stopX, stopY - offset, paint);
            }
            else {
                canvas.drawLine(startX,startY,stopX,stopY,paint);
//                if (startX < stopX) {
//                    if (startY < stopY)
//                        canvas.drawLine(startX-offset, startY - offset, stopX+offset, stopY + offset, paint);
//                    else canvas.drawLine(startX-offset, startY + offset, stopX+offset, stopY - offset, paint);
//                } else {
//                    if (startY < stopY)
//                        canvas.drawLine(startX+offset, startY - offset, stopX-offset, stopY + offset, paint);
//                    else canvas.drawLine(startX+offset, startY + offset, stopX-offset, stopY - offset, paint);
//                }
            }
        }
        else{
            String[] previousC = xyToRowColumn(startX,startY);
            int px = Integer.parseInt(previousC[0]);
            int py = Integer.parseInt(previousC[1]);
            String[] currentC = xyToRowColumn(stopX,stopY);
            int cx = Integer.parseInt(currentC[0]);
            int cy = Integer.parseInt(currentC[1]);

            if (startY - stopY == 0 && startX - stopX != 0) {
                int offset1 = pxHeightY / 120;
                int offset2 = pxHeightY / 120;
                if(lineGrid[px][py].length() == 2){
                    offset1 = -pxHeightY/120;
                }
                if(lineGrid[cx][cy].length() == 2){
                    offset2 = -pxHeightY/120;
                }
                if (startX < stopX)
                    canvas.drawLine(startX - offset1, startY, stopX + offset2, stopY, paint);
                else canvas.drawLine(startX + offset1, startY, stopX - offset2, stopY, paint);
            } else if (startX - stopX == 0 && startY - stopY != 0){
                int offset1 = pxHeightY / 120;
                int offset2 = pxHeightY / 120;
                if(lineGrid[px][py].length() == 2){
                    offset1 = -pxHeightY/120;
                }
                if(lineGrid[cx][cy].length() == 2){
                    offset2 = -pxHeightY/120;
                }
                if (startY < stopY)
                    canvas.drawLine(startX, startY - offset1, stopX, stopY + offset2, paint);
                else canvas.drawLine(startX, startY + offset1, stopX, stopY - offset2, paint);
            } else {
                int offset1 = pxHeightY / 120;
                int offset2 = pxHeightY / 120;
                if(lineGrid[px][py].length() == 2){
                    offset1 = -pxHeightY/120;
                }
                if(lineGrid[cx][cy].length() == 2){
                    offset2 = -pxHeightY/120;
                }

                if (startX < stopX) {
                    if (startY < stopY)
                        canvas.drawLine(startX-offset1, startY - offset1, stopX+offset2, stopY + offset2, paint);
                    else canvas.drawLine(startX-offset1, startY + offset1, stopX+offset2, stopY - offset2, paint);
                } else {
                    if (startY < stopY)
                        canvas.drawLine(startX+offset1, startY - offset1, stopX-offset2, stopY + offset2, paint);
                    else canvas.drawLine(startX + offset1, startY + offset1, stopX-offset2, stopY - offset2, paint);
                }
            }
        }
        imageView.setImageBitmap(bitmap);
    }

    public void addLine(String firstRC, String secondRC){
        int r1 = Integer.parseInt(String.valueOf(firstRC.charAt(0)));
        int c1 = Integer.parseInt(String.valueOf(firstRC.charAt(1)));
        int r2 = Integer.parseInt(String.valueOf(secondRC.charAt(0)));
        int c2 = Integer.parseInt(String.valueOf(secondRC.charAt(1)));
        if(r1 == r2 && c1 != c2){ // vertical line
            if(c1 < c2){ // first one is above second
                lineGrid[r1][c1] += "d";
                lineGrid[r2][c2] += "u";
            }
            else { // first one is below second
                lineGrid[r1][c1] += "u";
                lineGrid[r2][c2] += "d";
            }
        }
        else if(c1 == c2 && r1 != r2){ // horizontal line
            if(r1 < r2){ // first one is left of second
                lineGrid[r1][c1] += "r";
                lineGrid[r2][c2] += "l";
            }
            else { // first one is right of second
                lineGrid[r1][c1] += "l";
                lineGrid[r2][c2] += "r";
            }
        }
        else{
            if(c1 < c2){ // first one is above second
                if(r1 < r2){ // first one is left of second
                    lineGrid[r1][c1] += "s";
                    lineGrid[r2][c2] += "n";
                }
                else { // first one is right of second
                    lineGrid[r1][c1] += "n";
                    lineGrid[r2][c2] += "s";
                }
            }
            else { // first one is below second
                if(r1 < r2){ // first one is left of second
                    lineGrid[r1][c1] += "e";
                    lineGrid[r2][c2] += "w";
                }
                else { // first one is right of second
                    lineGrid[r1][c1] += "w";
                    lineGrid[r2][c2] += "e";
                }
            }
        }
    }

    public void removeLine(String firstRC, String secondRC){
        int r1 = Integer.parseInt(String.valueOf(firstRC.charAt(0)));
        int c1 = Integer.parseInt(String.valueOf(firstRC.charAt(1)));
        int r2 = Integer.parseInt(String.valueOf(secondRC.charAt(0)));
        int c2 = Integer.parseInt(String.valueOf(secondRC.charAt(1)));
        if(r1 == r2 && c1 != c2){ // vertical line
            if(c1 < c2){ // first one is above second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("d","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("u","");
            }
            else { // first one is below second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("u","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("d","");
            }
        }
        else if(c1 == c2 && r1 != r2){ // horizontal line
            if(r1 < r2){ // first one is left of second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("r","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("l","");
            }
            else { // first one is right of second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("l","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("r","");

            }
        }
        else{
            if(c1 < c2){ // first one is above second
                if(r1 < r2){ // first one is left of second
                    lineGrid[r1][c1] = lineGrid[r1][c1].replace("s","");
                    lineGrid[r2][c2] = lineGrid[r2][c2].replace("n","");
                }
                else { // first one is right of second
                    lineGrid[r1][c1] = lineGrid[r1][c1].replace("n","");
                    lineGrid[r2][c2] = lineGrid[r2][c2].replace("s","");
                }
            }
            else { // first one is below second
                if(r1 < r2){ // first one is left of second
                    lineGrid[r1][c1] = lineGrid[r1][c1].replace("e","");
                    lineGrid[r2][c2] = lineGrid[r2][c2].replace("w","");
                }
                else { // first one is right of second
                    lineGrid[r1][c1] = lineGrid[r1][c1].replace("w","");
                    lineGrid[r2][c2] = lineGrid[r2][c2].replace("e","");
                }
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
                && !currentC.equals("")
                && !previousC.equals("")
                && (
                (Math.abs(Integer.parseInt(String.valueOf(previousC.charAt(1))) - Integer.parseInt(String.valueOf(currentC.charAt(1)))) <= 1)

                        && (Math.abs(Integer.parseInt(String.valueOf(previousC.charAt(0))) - Integer.parseInt(String.valueOf(currentC.charAt(0)))) <= 1)
        )
                && isMoreLineCanBeAdded(previousC) && isMoreLineCanBeAdded(currentC));
    }

    public boolean isGridFull(){
        boolean isfull = true;
        int notfullCount = 0;
        for(int i = 0; i < gridSizeX; i++){
            if(!isfull) break;
            for(int j = 0; j < gridSizeY; j++){
                if(lineGrid[i][j].length() < 2){
                    if(lineGrid[i][j].length() <= 1){
                        notfullCount += 1;
                    }
                    if(notfullCount > 2){
                        isfull=false;
                        break;
                    }
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
        TextView textView;
        String s;
        SpannableString ss1;

        assert difficulty != null;
        switch (difficulty) {
            case "Easy":
            case "Kolay":
                setContentView(R.layout.activity_game_sozcuk_turu34);

                s= getString(R.string.Easy) + " - 3,4,5 "+getString(R.string.letters);
                ss1=  new SpannableString(s);
                ss1.setSpan(new RelativeSizeSpan(0.35f), s.indexOf("-"),s.length(), 0); // set size
                textView = findViewById(R.id.diffTV_game);
                textView.setText(ss1);

                Log.i("diff", "easy");
                difficulty = "Easy";
                gridSizeX = 3;
                gridSizeY = 4;
                break;
            case "Medium":
            case "Orta":
                setContentView(R.layout.activity_game_sozcuk_turu35);


                s= getString(R.string.Medium) + " - 4,5,6 "+getString(R.string.letters);
                ss1=  new SpannableString(s);
                ss1.setSpan(new RelativeSizeSpan(0.35f), s.indexOf("-"),s.length(), 0); // set size
                textView = findViewById(R.id.diffTV_game);
                textView.setText(ss1);

                gridSizeX = 3;
                gridSizeY = 5;
                difficulty = "Medium";
                Log.i("diff", "medium");
                break;
            case "Hard":
            case "Zor":
                setContentView(R.layout.activity_game_sozcuk_turu45);

                s= getString(R.string.Hard) + " - 2,3,4,5,6 "+getString(R.string.letters);
                ss1=  new SpannableString(s);
                ss1.setSpan(new RelativeSizeSpan(0.35f), s.indexOf("-"),s.length(), 0); // set size
                textView = findViewById(R.id.diffTV_game);
                textView.setText(ss1);

                gridSizeX = 4;
                gridSizeY = 5;
                difficulty = "Hard";
                Log.i("diff", "medium");
                break;
            default:
                setContentView(R.layout.activity_game_sozcuk_turu55);

                s= getString(R.string.VeryHard) + " - 3,4,5,6,7 "+getString(R.string.letters);
                ss1=  new SpannableString(s);
                ss1.setSpan(new RelativeSizeSpan(0.35f), s.indexOf("-"),s.length(), 0); // set size
                textView = findViewById(R.id.diffTV_game);
                textView.setText(ss1);

                gridSizeX = 5;
                gridSizeY = 5;
                difficulty = "Hardest";
                Log.i("diff", "hard");
                break;
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
        pxHeightY = (int) (300 * getResources().getDisplayMetrics().density);
        pxHeightX = (int) ((300*gridSizeX/gridSizeY) * getResources().getDisplayMetrics().density);
//        Log.i("asd", +"");
        Log.i("pxheightX",pxHeightX+"");
        Log.i("pxheightY",pxHeightY+"");
        initSomeVar();
        gridLayout.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float mx = motionEvent.getX();
                float my = motionEvent.getY();
//                Log.i("x / y",mx + " / " + my);
                if(mx >= 0 && mx <= pxHeightX && my >= 0 && my <= pxHeightY){
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
//                                    paint.setColor(getResources().getColor(R.color.f7f5fa));
                                    paint.setStrokeWidth((float)pxHeightY/60);
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
                                    paint.setStrokeWidth((float)pxHeightY/75);
                                }
                                else{
                                    Log.i("eraseMode","OFF  "+currentCoor+ "  "+ previousCoor);
                                    drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1],false);
                                    addLine(previousCoor, currentCoor);
                                    operations.add(previousCoor+currentCoor);
                                    opsForUndo.add(previousCoor+currentCoor+"+");
                                }
                                previousCoor = currentCoor;
                                Log.i("operations",operations+"      /      "+answer);
                                if(isGridFull()){
                                    Log.i("isGridFull","grid is full");
                                    checkAnswer(null);
                                }
//                                Log.i("LineGrid", Arrays.deepToString(lineGrid));
                            }
                            break;
                        case MotionEvent.ACTION_UP:
//                            if(!is_moving){
//                                Log.i("Pressed","Pressed");
//                            }
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
            ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("SozcukTuru."+difficulty, ObjectSerializer.serialize(new ArrayList<String>())));
            ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDSozcukTuru."+difficulty, ObjectSerializer.serialize(new ArrayList<Integer>())));
            ArrayList<String> solvedQuestions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new ArrayList<String>())));

            assert questions != null;
            questions.remove(0);

            assert solvedQuestions != null;
            assert gameIds != null;
            solvedQuestions.add("SozcukTuru."+difficulty+","+gameIds.remove(0)+"-"+"0");

            sharedPreferences.edit().putString("SozcukTuru."+difficulty, ObjectSerializer.serialize(questions)).apply();
            sharedPreferences.edit().putString("IDSozcukTuru."+difficulty, ObjectSerializer.serialize(gameIds)).apply();
            sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize(solvedQuestions)).apply();


        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}