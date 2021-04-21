package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

public class GameActivitySudoku extends AppCompatActivity {



    String gameName;
    String difficulty;
    String clickedBox = "-1";
    List<String> clueIndexes = new ArrayList<>();
    int gridSize = 6;
    int timerInSeconds = 0;
    boolean timerStopped=false;
    boolean undoing=false;
    boolean paused = false;
    boolean gotQuestion = false;
    boolean[] draftModeActive= new boolean[81];

    List<List<String>> operations = new ArrayList<>();
    JSONArray question;
    JSONArray answer;
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
                    ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Sudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<String>())));
                    ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDSudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<Integer>())));
                    Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));

                    assert questions != null;
                    questions.remove(0);

                    assert solvedQuestions != null;
                    assert gameIds != null;
                    Objects.requireNonNull(solvedQuestions.get("Sudoku." + gridSize+"."+difficulty)).add(gameIds.remove(0)+"-"+"0");

                    Log.i("solvedQuestions",solvedQuestions+"");

                    sharedPreferences.edit().putString("Sudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(questions)).apply();
                    sharedPreferences.edit().putString("IDSudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(gameIds)).apply();
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

    public void changeClicked(View view){
        TextView box = (TextView) view;
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        GridLayout numsLayout = findViewById(R.id.numsGL_ga);
        String answerIndex = box.getTag().toString();
        if(!clueIndexes.contains(answerIndex)) {
            if (!clickedBox.equals(answerIndex)) {
                if (!clickedBox.equals("-1")) {
                    gridLayout.findViewWithTag(clickedBox).setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                    if (draftModeActive[Integer.parseInt(clickedBox.substring(0, 1)) * gridSize + Integer.parseInt(clickedBox.substring(1))])
                        ((TextView)gridLayout.findViewWithTag(clickedBox)).setTextColor(getResources().getColor(R.color.draft_grey));
                    else ((TextView)gridLayout.findViewWithTag(clickedBox)).setTextColor(getResources().getColor(R.color.light_red));
                }
                box.setBackground(getResources().getDrawable(R.drawable.stroke_bg_shallow));
                box.setTextColor(getResources().getColor(R.color.f7f5fa));
                clickedBox = answerIndex;
            } else {
                if (!undoing) {
                    box.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                    if (draftModeActive[Integer.parseInt(clickedBox.substring(0, 1)) * gridSize + Integer.parseInt(clickedBox.substring(1))])
                        box.setTextColor(getResources().getColor(R.color.draft_grey));
                    else box.setTextColor(getResources().getColor(R.color.light_red));
                    clickedBox = "-1";
                }
            }
            if (!clickedBox.equals("-1")){
                ImageView draftBtn = findViewById(R.id.draftbutton_ga);
                if (draftModeActive[Integer.parseInt(clickedBox.substring(0, 1)) * gridSize + Integer.parseInt(clickedBox.substring(1))]) {
                    for (int i = 1; i < gridSize + 1; i++) {
                        ((Button) numsLayout.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    }
                    draftBtn.setBackground(getResources().getDrawable(R.drawable.rounded_light_bluegreen_bg));
                }
                else{
                    for (int i = 1; i < gridSize + 1; i++) {
                        ((Button) numsLayout.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                    }
                    draftBtn.setBackground(getResources().getDrawable(R.drawable.nums_gl_bg));
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    public void numClicked(View view){
        Button btn = (Button) view;
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        GridLayout numGrid = findViewById(R.id.numsGL_ga);
        if(!clickedBox.equals("-1")){
            TextView currentBox = gridLayout.findViewWithTag(clickedBox);
            if(currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox,"-1")));
            }
            if(draftModeActive[Integer.parseInt(clickedBox.substring(0,1))*gridSize+Integer.parseInt(clickedBox.substring(1))]){
                if(currentBox.getText().toString().length() == 0){
                    currentBox.setText(btn.getTag().toString());
                    if(gridSize == 9)currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    else currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
                    List<String> newOp = new ArrayList<>(Arrays.asList(clickedBox, btn.getTag().toString()));
                    if(!newOp.equals(operations.get(operations.size() - 1))){
                        operations.add(new ArrayList<>(Arrays.asList(clickedBox, btn.getTag().toString())));
                    }
                }
                else{
                    if(currentBox.getText().toString().contains(btn.getTag().toString())){
                        currentBox.setText(currentBox.getText().toString().replace(" "+btn.getTag().toString(),"").replace(btn.getTag().toString()+" ","").replace(btn.getTag().toString(),""));
                        List<String> newOp = new ArrayList<>(Arrays.asList(clickedBox, currentBox.getText().toString()));
                        if(!newOp.equals(operations.get(operations.size() - 1))){
                            operations.add(new ArrayList<>(Arrays.asList(clickedBox, currentBox.getText().toString())));
                        }
                        if(currentBox.getText().toString().length() == 1){
                            if(gridSize == 9)currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            else currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                            draftModeActive[Integer.parseInt(clickedBox.substring(0,1))*gridSize+Integer.parseInt(clickedBox.substring(1))] = false;
                            for(int i = 1; i<gridSize+1; i++){
                                ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,22);
                            }
                            findViewById(R.id.draftbutton_ga).setBackground(getResources().getDrawable(R.drawable.nums_gl_bg));
                        }
                    }
                    else{
                        if(currentBox.getText().toString().length() <= 10) {
                            currentBox.setText(currentBox.getText().toString()+" "+btn.getTag().toString());
                            List<String> newOp = new ArrayList<>(Arrays.asList(clickedBox, currentBox.getText().toString()));
                            if(!newOp.equals(operations.get(operations.size() - 1))){
                                operations.add(new ArrayList<>(Arrays.asList(clickedBox, currentBox.getText().toString())));
                            }
                        }
                    }
                }
            }
            else{
                currentBox.setText(btn.getTag().toString());
                List<String> newOp = new ArrayList<>(Arrays.asList(clickedBox, btn.getTag().toString()));
                if(!newOp.equals(operations.get(operations.size() - 1))){
                    operations.add(new ArrayList<>(Arrays.asList(clickedBox, btn.getTag().toString())));
                }
            }
            boolean isFull = true;
            for (int i = 0; i<gridSize; i++){
                for (int j = 0; j<gridSize; j++){
                    if (((TextView)gridLayout.findViewWithTag(Integer.toString(j)+i)).getText().toString().equals("")){
                        isFull = false;
                        break;
                    }
                }
            }
            if (isFull) checkAnswer(null);
            Log.i("operations",operations+"");
        }
    }

    public void deleteNum(View view){
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        if(!clickedBox.equals("-1")){
            TextView currentBox = gridLayout.findViewWithTag(clickedBox);
            if(!currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox, "-1")));
                Log.i("operations",operations+"");
                currentBox.setText("");
            }
        }
    }

    public void undoOperation(View view){
        if(operations.size() > 1){
            operations = operations.subList(0,operations.size()-1);
            List<String> tuple = operations.get(operations.size()-1);
            String co = tuple.get(0);
            String num = tuple.get(1);
            Log.i("co/num",co+" / "+num);
            GridLayout gridLayout = findViewById(R.id.gridGL_ga);
            TextView currentBox = gridLayout.findViewWithTag(co);
            if(num.equals("-1")){
                currentBox.setText("");
            }
            else{
                if(num.length()>1){
                    draftModeActive[Integer.parseInt(co.substring(0,1))*gridSize+Integer.parseInt(co.substring(1))]=false;
                    draftClicked(null);
                }
                else{
                    draftModeActive[Integer.parseInt(co.substring(0,1))*gridSize+Integer.parseInt(co.substring(1))]=true;
                    draftClicked(null);
                }
                currentBox.setText(num);
            }
            undoing=true;
            changeClicked(currentBox);
            undoing=false;
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
            GridLayout gridLayout = findViewById(R.id.gridGL_ga);
            clickedBox = "-1";
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    tv.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                    if(!clueIndexes.contains(Integer.toString(j)+i)){
                        tv.setText("");
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
        for(int i = 0; i < gridSize; i++){
            for(int j = 0; j <  gridSize; j++){
                try {
                    if(!((TextView)gridLayout.findViewWithTag(Integer.toString(j)+ i)).getText().equals(((JSONArray)answer.get(i)).get(j).toString())){
                        checking=false;
                    }
                    Log.i("checking",((TextView)gridLayout.findViewWithTag(Integer.toString(j)+i)).getText().toString()+" / "+(((JSONArray)answer.get(i)).get(j).toString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i("check",checking+"  "+answer);
        if(checking){

            SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
            try {
                ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Sudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<String>())));
                ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDSudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<Integer>())));
                Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));

                assert questions != null;
                questions.remove(0);

                assert solvedQuestions != null;
                assert gameIds != null;
                Objects.requireNonNull(solvedQuestions.get("Sudoku." + gridSize+"."+difficulty)).add(gameIds.remove(0)+"-"+timerInSeconds);

                Log.i("solvedQuestions",solvedQuestions+"");

                sharedPreferences.edit().putString("Sudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(questions)).apply();
                sharedPreferences.edit().putString("IDSudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(gameIds)).apply();
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
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    gridLayout.findViewWithTag(Integer.toString(j) + i).setEnabled(false);
                    if (gridSize==9)((TextView) gridLayout.findViewWithTag(Integer.toString(j)+i)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    else ((TextView) gridLayout.findViewWithTag(Integer.toString(j)+i)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                }
            }
            for(int i = 1; i<gridSize+1; i++){
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
                    startActivity(intent);
                    overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                    correctDialog.dismiss();
                }
            });
            correctDialog.show();
        }
    }

    public void draftClicked(View view){
        ImageView draftBtn = findViewById(R.id.draftbutton_ga);
        GridLayout numGrid = findViewById(R.id.numsGL_ga);
        GridLayout questionGrid = findViewById(R.id.gridGL_ga);
        TextView currentClickedBox = questionGrid.findViewWithTag(clickedBox);
        if(!clickedBox.equals("-1")){
            int boxIndex = Integer.parseInt(clickedBox.substring(0,1))*gridSize+Integer.parseInt(clickedBox.substring(1));
            if(!draftModeActive[boxIndex]){
                if(currentClickedBox.getText().toString().length() == 1) {
                    if(gridSize == 9)currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    else currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
                }
                for(int i = 1; i<gridSize+1; i++){
                    ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
                }
                draftModeActive[boxIndex] = true;
                draftBtn.setBackground(getResources().getDrawable(R.drawable.rounded_light_bluegreen_bg));
            }
            else{
                if(currentClickedBox.getText().toString().length() <= 1) {
                    if(gridSize == 9)currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    else currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                    for(int i = 1; i<gridSize+1; i++){
                        ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,22);
                    }
                    draftModeActive[boxIndex] = false;
                    draftBtn.setBackground(getResources().getDrawable(R.drawable.nums_gl_bg));
                }
            }
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
                    questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Sudoku." + gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<String>())));
                    gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDSudoku." + gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<Integer>())));
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
                Log.i("solvedQuestion", Objects.requireNonNull(solvedQuestions.get("Sudoku." + gridSize+"."+difficulty)).toString()+"ss");
                for(int i = 0; i < idArray.length(); i++){
                    if(!gameIds.contains(idArray.getInt(i))&&!Objects.requireNonNull(solvedQuestions.get("Sudoku." + gridSize+"."+difficulty)).toString().contains(idArray.getInt(i)+"-")) {
                        questions.add(gridArrays.getJSONArray(i).toString());
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
                sharedPreferences.edit().putString("Sudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(questions)).apply();
                sharedPreferences.edit().putString("IDSudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(gameIds)).apply();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Log.i("gameIds", ObjectSerializer.deserialize(sharedPreferences.getString("IDSudoku." + gridSize, ObjectSerializer.serialize(new ArrayList<Integer>()))) +"");
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

        answer = (JSONArray) grid.get(1);
        question = (JSONArray) grid.get(0);
        Log.i("question",""+question);
        Log.i("answer",""+answer);
        for (int i = 0; i < question.length(); i++){
            JSONArray row = (JSONArray) question.get(i);
            for (int j = 0; j< row.length(); j++){
                if(!row.get(j).toString().equals("0")) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    tv.setText(row.get(j).toString());
                    tv.setTextColor(getResources().getColor(R.color.near_black_blue));
                    clueIndexes.add(Integer.toString(j)+i);
                }
            }
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
        loadingDialog = new LoadingDialog(GameActivitySudoku.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
        loadingDialog.startLoadingAnimation();
    }

    public void initDraftModeActiveVar(){
        for(int i = 0; i<gridSize*gridSize; i++){
            draftModeActive[i] = false;
        }
    }

    public void clearGrid(){
        operations = new ArrayList<>();
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
    }

    public void mainFunc(){
        findViewById(R.id.clickView).setVisibility(View.GONE);
        GridLayout numsLayout = findViewById(R.id.numsGL_ga);
        TextView undoTV = findViewById(R.id.undoTV_ga);
        TextView deleteTV = findViewById(R.id.deleteTV_ga);
        TextView resetTV = findViewById(R.id.resetTV_game);
        for(int i = 1; i<gridSize+1; i++){
            numsLayout.findViewWithTag(Integer.toString(i)).setEnabled(true);
        }
        undoTV.setEnabled(true);
        deleteTV.setEnabled(true);
        resetTV.setEnabled(true);
        clearGrid();
        initDraftModeActiveVar();
        GetRequest getRequest = new GetRequest();
        //noinspection deprecation
        getRequest.execute("https://akiloyunlariapp.herokuapp.com/Sudoku."+gridSize+"."+difficulty,"fx!Ay:;<p6Q?C8N{");
        loadingDialogFunc();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        gameName = intent.getStringExtra("gameName");
        difficulty = intent.getStringExtra("difficulty");
        assert difficulty != null;
        if(gameName.matches("Sudoku6")){
            setContentView(R.layout.activity_game_sudoku6);
            gridSize=6;
        }
        else if(gameName.matches("Sudoku9")) {
            setContentView(R.layout.activity_game_sudoku9);
            gridSize = 9;
        }
        else{
            setContentView(R.layout.activity_game_sudoku9);
            gridSize=9;
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
            ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Sudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<String>())));
            ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDSudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(new ArrayList<Integer>())));
            Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));

            assert questions != null;
            questions.remove(0);


            Log.i("solvedQuestions",solvedQuestions+"");

            sharedPreferences.edit().putString("Sudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(questions)).apply();
            sharedPreferences.edit().putString("IDSudoku."+gridSize+"."+difficulty, ObjectSerializer.serialize(gameIds)).apply();
            sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize((Serializable) solvedQuestions)).apply();

            assert solvedQuestions != null;
            assert gameIds != null;
            Objects.requireNonNull(solvedQuestions.get("Sudoku." + gridSize+"."+difficulty)).add(gameIds.remove(0)+"-"+"0");

        } catch (IOException e) {
            e.printStackTrace();
        }


        super.onDestroy();
    }
}