package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GameActivitySayiBulmaca extends AppCompatActivity {

    String gameName;
    String difficulty;
    int clickedBox = -1;
    int gridSize = 3;
    int timerInSeconds = 0;
    boolean timerStopped=false;
    boolean undoing=false;
    boolean paused = false;
    boolean gotQuestion = false;
    boolean[] draftModeActive= new boolean[5];

    List<List<Integer>> operations = new ArrayList<>();
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
            @Override
            public void onClick(View v) {
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

    public void changeClicked(View view){
        TextView box = (TextView) view;
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        int answerIndex = Integer.parseInt((box.getTag().toString()).substring(box.getTag().toString().length()-1));
        if (clickedBox != answerIndex){
            if (clickedBox != -1){
                if (clickedBox == 0){
                    gridLayout.findViewWithTag("answer"+ clickedBox).setBackground(getResources().getDrawable(R.drawable.strokebg_topbottomleft));
                }
                else{
                    gridLayout.findViewWithTag("answer"+ clickedBox).setBackground(getResources().getDrawable(R.drawable.strokebg_topbottom));
                }
            }
            if(answerIndex == 0){
                box.setBackground(getResources().getDrawable(R.drawable.strokebg_topbottomleft_shallow2));
            }
            else{
                box.setBackground(getResources().getDrawable(R.drawable.strokebg_topbottom_shallow2));
            }
            box.setTextColor(getResources().getColor(R.color.f7f5fa));
            clickedBox = answerIndex;
        }
        else{
            if(!undoing){
                if (answerIndex == 0){
                    box.setBackground(getResources().getDrawable(R.drawable.strokebg_topbottomleft));
                }
                else{
                    box.setBackground(getResources().getDrawable(R.drawable.strokebg_topbottom));
                }
                box.setTextColor(getResources().getColor(R.color.light_red));
                clickedBox = -1;
            }
        }
        if(clickedBox != -1 && draftModeActive[clickedBox]){
            for(int i = 0; i<10; i++){
                ((Button)gridLayout.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void numClicked(View view){
        Button btn = (Button) view;
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        if(clickedBox != -1){
            TextView currentBox = gridLayout.findViewWithTag("answer"+ clickedBox);
            if(currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox,-1)));
            }
            if(draftModeActive[clickedBox]){
                currentBox.setText(currentBox.getText().toString()+" "+btn.getTag().toString());
            }
            else{
                currentBox.setText(btn.getTag().toString());
            }
            List<Integer> newOp = new ArrayList<>(Arrays.asList(clickedBox, Integer.parseInt(btn.getTag().toString())));
            if(!newOp.equals(operations.get(operations.size() - 1))){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox, Integer.parseInt(btn.getTag().toString()))));
            }
            boolean isFull = true;
            for (int i = 0; i<gridSize; i++){
                if (((TextView)gridLayout.findViewWithTag("answer"+i)).getText().toString().equals("")){
                    isFull = false;
                    break;
                }
            }
            if (isFull) checkAnswer(null);
            Log.i("operations",operations+"");
        }
    }

    public void deleteNum(View view){
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        if(clickedBox != -1){
            TextView currentBox = gridLayout.findViewWithTag("answer"+ clickedBox);
            if(!currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox, -1)));
                Log.i("operations",operations+"");
                currentBox.setText("");
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void undoOperation(View view){
        if(operations.size() > 1){
            operations = operations.subList(0,operations.size()-1);
            List<Integer> tuple = operations.get(operations.size()-1);
            int co = tuple.get(0);
            int num = tuple.get(1);
            Log.i("co/num",co+" / "+num);
            GridLayout gridLayout = findViewById(R.id.gridGL_ga);
            TextView currentBox = gridLayout.findViewWithTag("answer"+ co);
            if(num == -1){
                currentBox.setText("");
            }
            else{
                currentBox.setText(Integer.toString(num));
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
            for (int i = 0; i < gridSize; i++) {
                TextView currentBox = gridLayout.findViewWithTag("answer" + i);
                currentBox.setText("");
            }
            if(clickedBox!=-1) {
                if (clickedBox == 0) {
                    gridLayout.findViewWithTag("answer" + clickedBox).setBackground(getResources().getDrawable(R.drawable.strokebg_topbottomleft));
                } else {
                    gridLayout.findViewWithTag("answer" + clickedBox).setBackground(getResources().getDrawable(R.drawable.strokebg_topbottom));
                }
            }
            clickedBox = -1;
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    tv.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void notesOnGrid(View view) {
        TextView clickedTV = (TextView) view;
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        String clickedNum = clickedTV.getText().toString();
//        Log.i("debugLog","beforeIfs");
        if (Objects.equals(clickedTV.getBackground().getConstantState(), getResources().getDrawable(R.drawable.stroke_bg).getConstantState())) {
//            Log.i("debugLog","stroke_bg");
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    if (tv.getText().toString().equals(clickedNum)) {
                        tv.setBackground(getResources().getDrawable(R.drawable.stroke_bg_bluegreen));
                    }
                }
            }
        }
        else if (Objects.equals(clickedTV.getBackground().getConstantState(), getResources().getDrawable(R.drawable.stroke_bg_bluegreen).getConstantState())) {
//            Log.i("debugLog","stroke_bg_red");
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    if (tv.getText().toString().equals(clickedNum)) {
                        tv.setBackground(getResources().getDrawable(R.drawable.stroke_bg_red));
                    }
                }
            }
        }
        else if (Objects.equals(clickedTV.getBackground().getConstantState(), getResources().getDrawable(R.drawable.stroke_bg_red).getConstantState())) {
//            Log.i("debugLog","stroke_bg_bluegreen");
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    if (tv.getText().toString().equals(clickedNum)) {
                        tv.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                    }
                }
            }
        }
    }

    public void checkAnswer(View view){
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        boolean checking=true;
        for(int i = 0; i < gridSize; i++){
            try {
                if(!((TextView)gridLayout.findViewWithTag("answer"+ i)).getText().equals(answer.get(i).toString())){
                    checking=false;
                }
                Log.i("checking",((TextView)gridLayout.findViewWithTag("answer"+ i)).getText().toString()+" / "+(answer.get(i).toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("check",checking+"  "+answer);
        if(checking){
            timerStopped=true;
            LayoutInflater factory = LayoutInflater.from(this);
            final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
            final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
            TextView timerTV = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
            timerTV.setText(formatTime(timerInSeconds));
            correctDialog.setView(leaveDialogView);

            GridLayout numsLayout = findViewById(R.id.numsGL_ga);
            TextView undoTV = findViewById(R.id.undoTV_ga);
            TextView deleteTV = findViewById(R.id.deleteTV_ga);
            TextView resetTV = findViewById(R.id.resetTV_game);
            for (int i = 0; i < gridSize; i++) {
                gridLayout.findViewWithTag("answer" + i).setEnabled(false);
                for (int j = 0; j < gridSize; j++) {
                    gridLayout.findViewWithTag(Integer.toString(j) + i).setEnabled(false);
                }
            }
            for(int i = 0; i<10; i++){
                numsLayout.findViewWithTag(Integer.toString(i)).setEnabled(false);
            }
            undoTV.setEnabled(false);
            deleteTV.setEnabled(false);
            resetTV.setEnabled(false);

            leaveDialogView.findViewById(R.id.correctDialogNext).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //your business logic
//                    Intent intent = new Intent(getApplicationContext(), GameActivitySayiBulmaca.class);
//                    intent.putExtra("gameName",gameName);
//                    intent.putExtra("difficulty",difficulty);
//                    startActivity(intent);
//                    overridePendingTransition(R.anim.enter, R.anim.exit);
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
        GridLayout numGrid = findViewById(R.id.numsGL_ga);
        GridLayout questionGrid = findViewById(R.id.gridGL_ga);
        if(clickedBox != -1){
            TextView currentClickedBox = questionGrid.findViewWithTag("answer"+ clickedBox);
            if(currentClickedBox.getText().toString().length() == 1){
                if(currentClickedBox.getTextSize() == 25){
                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                    for(int i = 0; i<10; i++){
                        ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
                    }
                    draftModeActive[clickedBox] = true;
                }
                else{
                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
                    for(int i = 0; i<10; i++){
                        ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                    }
                    draftModeActive[clickedBox] = false;
                }
            }
            else if (currentClickedBox.getText().toString().length() == 0) {
                if (draftModeActive[clickedBox]) {
                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
                    for (int i = 0; i < 10; i++) {
                        ((Button) numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    }
                    draftModeActive[clickedBox] = false;
                }
                else{
                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                    for(int i = 0; i<10; i++){
                        ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
                    }
                    draftModeActive[clickedBox] = true;
                }
            }
            else{
                draftModeActive[clickedBox] = true;
            }
        }
    }

    public class GetRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringBuilder result = new StringBuilder();
                URL reqURL = new URL(strings[0] + "?" + "Info=1&Token=" +strings[1]);
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

        @SuppressLint("SetTextI18n")
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

//            JSONObject jsonObject = null;
            GridLayout gridLayout = findViewById(R.id.gridGL_ga);
            try {
                org.json.JSONObject jb = new org.json.JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));
//                JSONObject jsonObject = (JSONObject) parser.parse(result);
//                jsonArray = new JSONObject();
//                Log.i("jsonArray",jsonArray.toString());
//                JSONObject jsonObject = (org.json.simple.JSONObject) parser.parse(jsonArray.getString(0));
                JSONArray gridArray = (JSONArray) ((JSONArray)((JSONArray)((JSONArray)jb.get("Info")).get(0)).get(0)).get(0);
                answer = (JSONArray) gridArray.get(gridArray.length()-1);
                Log.i("jsonGrid",""+gridArray);
                for (int i = 0; i < gridArray.length()-1; i++){
                    JSONArray row = (JSONArray) gridArray.get(i);
                    for (int j = 0; j< row.length()-1; j++){
                        TextView tv = gridLayout.findViewWithTag(Integer.toString(j)+ i);
                        tv.setText(row.get(j).toString());
                    }
                    JSONArray rguide = (JSONArray)row.get(row.length()-1);
                    TextView rGuideTV = gridLayout.findViewWithTag("g"+i);
                    if(rguide.get(0).toString().equals("0")){
                        rGuideTV.setText(rguide.get(1).toString());
                    }
                    else if(rguide.get(1).toString().equals("0")){
                        rGuideTV.setText("+"+rguide.get(0).toString());
                    }
                    else{
                        rGuideTV.setText("+"+rguide.get(0).toString()+"  "+rguide.get(1).toString());
                    }
                }
                TextView guideanswer = gridLayout.findViewWithTag("answerguide");
                guideanswer.setText("+"+((JSONArray)answer.get(answer.length()-1)).get(0).toString());
                answer.remove(answer.length()-1);
                timerStopped=false;
                gotQuestion=true;
                timerFunc();
                loadingDialog.dismissDialog();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void timerFunc(){
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
        loadingDialog = new LoadingDialog(GameActivitySayiBulmaca.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
        loadingDialog.startLoadingAnimation();
    }

    public void initDraftModeActiveVar(){
        for(int i = 0; i<gridSize; i++){
            draftModeActive[i] = false;
        }
    }

    public void clearGrid(){
        operations = new ArrayList<>();
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        for (int i = 0; i < gridSize; i++) {
            TextView currentBox = gridLayout.findViewWithTag("answer" + i);
            currentBox.setText("");
            currentBox.setEnabled(true);
        }
        if(clickedBox != -1){
            if (clickedBox == 0) {
                gridLayout.findViewWithTag("answer" + clickedBox).setBackground(getResources().getDrawable(R.drawable.strokebg_topbottomleft));
            } else {
                gridLayout.findViewWithTag("answer" + clickedBox).setBackground(getResources().getDrawable(R.drawable.strokebg_topbottom));
            }
        }

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                tv.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                tv.setEnabled(true);
            }
        }
        clickedBox = -1;
        timerInSeconds = 0;
        timerStopped=true;
    }

    public void mainFunc(){
        GridLayout numsLayout = findViewById(R.id.numsGL_ga);
        TextView undoTV = findViewById(R.id.undoTV_ga);
        TextView deleteTV = findViewById(R.id.deleteTV_ga);
        TextView resetTV = findViewById(R.id.resetTV_game);
        for(int i = 0; i<10; i++){
            numsLayout.findViewWithTag(Integer.toString(i)).setEnabled(true);
        }
        undoTV.setEnabled(true);
        deleteTV.setEnabled(true);
        resetTV.setEnabled(true);
        clearGrid();
        initDraftModeActiveVar();
        GetRequest getRequest = new GetRequest();
        getRequest.execute("https://akiloyunlariapp.herokuapp.com/SayiBulmaca"+gridSize,"fx!Ay:;<p6Q?C8N{");
        loadingDialogFunc();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        gameName = intent.getStringExtra("gameName");
        difficulty = intent.getStringExtra("difficulty");
        assert difficulty != null;
        if(difficulty.equals("Easy") || difficulty.equals("Kolay")){
            setContentView(R.layout.activity_game_sayibulmaca3);
            gridSize=3;
        }
        else if(difficulty.equals("Medium") || difficulty.equals("Orta")){
            setContentView(R.layout.activity_game_sayibulmaca4);
            gridSize=4;
        }
        else{
            setContentView(R.layout.activity_game_sayibulmaca5);
            gridSize=5;
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

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        timerInSeconds = 0;
//        timerStopped=true;
//    }
}