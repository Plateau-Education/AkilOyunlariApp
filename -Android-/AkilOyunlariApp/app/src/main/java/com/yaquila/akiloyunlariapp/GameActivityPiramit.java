package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

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

public class GameActivityPiramit extends AppCompatActivity{

    String gameName;
    String difficulty;
    String clickedBox = "-1";
    int gridSize = 3;
    int answerCount = 3;
    int timerInSeconds = 0;
    boolean timerStopped=false;
    boolean undoing=false;
    boolean paused = false;
    boolean gotQuestion = false;
    boolean[] draftModeActive= new boolean[14];

    List<List<String>> operations = new ArrayList<>();
    JSONArray answer;
    LoadingDialog loadingDialog;
    Handler timerHandler;
    Runnable runnable;

//    @Override
//    public void onClick(View view) {
//        if(view.getId() != R.id.backButtonLL_game){
//            LayoutInflater factory = LayoutInflater.from(this);
//            final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
//            final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
//            TextView timerTV = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
//            timerTV.setText(formatTime(timerInSeconds));
//            correctDialog.setView(leaveDialogView);
//            leaveDialogView.findViewById(R.id.correctDialogNext).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //your business logic
////                    Intent intent = new Intent(getApplicationContext(), GameActivitySayiBulmaca.class);
////                    intent.putExtra("gameName",gameName);
////                    intent.putExtra("difficulty",difficulty);
////                    startActivity(intent);
////                    overridePendingTransition(R.anim.enter, R.anim.exit);
//                    mainFunc();
//                    correctDialog.dismiss();
//                }
//            });
//            leaveDialogView.findViewById(R.id.correctDialogGameMenu).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
//                    startActivity(intent);
//                    overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
//                    correctDialog.dismiss();
//                }
//            });
//            correctDialog.show();
//        }
//    }

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
        GridLayout numsLayout = findViewById(R.id.numsGL_ga);
        String answerIndex = (box.getTag().toString()).replace("answer","");
        if (!clickedBox.equals(answerIndex)){
            if (!clickedBox.equals("-1")){
                gridLayout.findViewWithTag("answer"+ clickedBox).setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
                ((TextView)gridLayout.findViewWithTag("answer"+clickedBox)).setTextColor(getResources().getColor(R.color.light_red));

            }
            box.setBackground(getResources().getDrawable(R.drawable.stroke_bg2_shallow));
            box.setTextColor(getResources().getColor(R.color.f7f5fa));
            clickedBox = answerIndex;
        }
        else{
            if(!undoing){
                box.setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
                box.setTextColor(getResources().getColor(R.color.light_red));
                clickedBox = "-1";
            }
        }
        if (!clickedBox.equals("-1")){
            if (draftModeActive[Integer.parseInt(clickedBox)]) {
                for (int i = 1; i < 10; i++) {
                    ((Button) numsLayout.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                }
            }
            else{
                for (int i = 1; i < 10; i++) {
                    ((Button) numsLayout.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                }
            }
        }
    }

    public void numClicked(View view){
        Button btn = (Button) view;
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        if(!clickedBox.equals("-1")){
            TextView currentBox = gridLayout.findViewWithTag("answer"+ clickedBox);
            if(currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox,"-1")));
            }
            if(draftModeActive[Integer.parseInt(clickedBox)]){
                if(currentBox.getText().toString().length() == 0){
                    currentBox.setText(btn.getTag().toString());
                    if(gridSize<=4) currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                    else if(gridSize==5) currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    else currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    ArrayList newOp = new ArrayList<>(Arrays.asList(clickedBox, btn.getTag().toString()));
                    if(!newOp.equals(operations.get(operations.size() - 1))){
                        operations.add(new ArrayList<>(Arrays.asList(clickedBox, btn.getTag().toString())));
                    }
                }
                else{
                    if(currentBox.getText().toString().contains(btn.getTag().toString())){
                        currentBox.setText(currentBox.getText().toString().replace(" "+btn.getTag().toString(),"").replace(btn.getTag().toString()+" ","").replace(btn.getTag().toString(),""));
                        ArrayList newOp = new ArrayList<>(Arrays.asList(clickedBox, currentBox.getText().toString()));
                        if(!newOp.equals(operations.get(operations.size() - 1))){
                            operations.add(new ArrayList<>(Arrays.asList(clickedBox, currentBox.getText().toString())));
                        }
                        if(currentBox.getText().toString().length() == 1){
                            currentBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                            draftModeActive[Integer.parseInt(clickedBox)] = false;
                        }
                    }
                    else{
                        if(currentBox.getText().toString().length() <= 8) {
                            currentBox.setText(currentBox.getText().toString()+" "+btn.getTag().toString());
                            ArrayList newOp = new ArrayList<>(Arrays.asList(clickedBox, currentBox.getText().toString()));
                            if(!newOp.equals(operations.get(operations.size() - 1))){
                                operations.add(new ArrayList<>(Arrays.asList(clickedBox, currentBox.getText().toString())));
                            }
                        }
                    }
                }
            }
            else{
                currentBox.setText(btn.getTag().toString());
                ArrayList newOp = new ArrayList<>(Arrays.asList(clickedBox, btn.getTag().toString()));
                if(!newOp.equals(operations.get(operations.size() - 1))){
                    operations.add(new ArrayList<>(Arrays.asList(clickedBox, btn.getTag().toString())));
                }
            }
            boolean isFull = true;
            for (int i = 0; i<answerCount; i++){
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
        if(!clickedBox.equals("-1")){
            TextView currentBox = gridLayout.findViewWithTag("answer"+ clickedBox);
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
            TextView currentBox = gridLayout.findViewWithTag("answer"+ co);
            if(num.equals("-1")){
                currentBox.setText("");
            }
            else{
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
            for (int i = 0; i < answerCount; i++) {
                TextView currentBox = gridLayout.findViewWithTag("answer" + i);
                currentBox.setText("");
            }
            if(!clickedBox.equals("-1")) {
                gridLayout.findViewWithTag("answer" + clickedBox).setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
            }
            clickedBox = "-1";
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    if(tv!=null)tv.setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void checkAnswer(View view){
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        boolean checking=true;
        for(int i = 0; i < answerCount; i++){
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
            for (int i = 0; i < answerCount; i++) {
                gridLayout.findViewWithTag("answer" + i).setEnabled(false);
            }
            for(int i = 1; i<10; i++){
                numsLayout.findViewWithTag(Integer.toString(i)).setEnabled(false);
            }
            findViewById(R.id.draftbutton_ga).setEnabled(false);
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
        TextView currentClickedBox = questionGrid.findViewWithTag("answer"+clickedBox);
        if(!clickedBox.equals("-1")) {
            if (!draftModeActive[Integer.parseInt(clickedBox)]) {
                if (currentClickedBox.getText().toString().length() == 1) {
                    if(gridSize<=4) currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                    else if(gridSize==5) currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    else currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                }
                for (int i = 1; i < 10; i++) {
                    ((Button) numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                }
                draftModeActive[Integer.parseInt(clickedBox)] = true;
            } else {
                if (currentClickedBox.getText().toString().length() <= 1) {
                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                    for (int i = 1; i < 10; i++) {
                        ((Button) numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                    }
                    draftModeActive[Integer.parseInt(clickedBox)] = false;
                }
            }
        }
//        GridLayout numGrid = (GridLayout) findViewById(R.id.numsGL_ga);
//        GridLayout questionGrid = (GridLayout) findViewById(R.id.gridGL_ga);
//        if(clickedBox != -1){
//            TextView currentClickedBox = (TextView) questionGrid.findViewWithTag("answer"+ clickedBox);
//            if(currentClickedBox.getText().toString().length() == 1){
//                if(currentClickedBox.getTextSize() == 25){
//                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
//                    for(int i = 0; i<10; i++){
//                        ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
//                    }
//                    draftModeActive[clickedBox] = true;
//                }
//                else{
//                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
//                    for(int i = 0; i<10; i++){
//                        ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
//                    }
//                    draftModeActive[clickedBox] = false;
//                }
//            }
//            else if (currentClickedBox.getText().toString().length() == 0) {
//                if (draftModeActive[clickedBox]) {
//                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
//                    for (int i = 0; i < 10; i++) {
//                        ((Button) numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
//                    }
//                    draftModeActive[clickedBox] = false;
//                }
//                else{
//                    currentClickedBox.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
//                    for(int i = 0; i<10; i++){
//                        ((Button)numGrid.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
//                    }
//                    draftModeActive[clickedBox] = true;
//                }
//            }
//            else{
//                draftModeActive[clickedBox] = true;
//            }
//        }
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

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

//            JSONObject jsonObject = null;
//            GridLayout gridLayout = findViewById(R.id.gridGL_ga);
            try {
                org.json.JSONObject jb = new org.json.JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));
//                JSONObject jsonObject = (JSONObject) parser.parse(result);
//                jsonArray = new JSONObject();
//                Log.i("jsonArray",jsonArray.toString());
//                JSONObject jsonObject = (org.json.simple.JSONObject) parser.parse(jsonArray.getString(0));
                JSONArray gridArray = (JSONArray) ((JSONArray)((JSONArray)((JSONArray)jb.get("Info")).get(0)).get(0)).get(0);
                seperateGridAnswer(gridArray);
                timerStopped=false;
                gotQuestion = true;
                timerFunc();
                loadingDialog.dismissDialog();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void seperateGridAnswer(JSONArray grid){
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        try {
            if (gridSize == 3) {
                ((TextView) gridLayout.findViewWithTag("00")).setText(((JSONArray) grid.get(0)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("02")).setText(((JSONArray) grid.get(2)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("22")).setText(((JSONArray) grid.get(2)).get(2).toString());
                answer = new JSONArray(new ArrayList<>(Arrays.asList(((JSONArray) grid.get(1)).get(0).toString(), ((JSONArray) grid.get(1)).get(1).toString(), ((JSONArray) grid.get(2)).get(1).toString())));
            }
            else if (gridSize == 4){
                ((TextView) gridLayout.findViewWithTag("00")).setText(((JSONArray) grid.get(0)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("12")).setText(((JSONArray) grid.get(2)).get(1).toString());
                ((TextView) gridLayout.findViewWithTag("03")).setText(((JSONArray) grid.get(3)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("33")).setText(((JSONArray) grid.get(3)).get(3).toString());
                answer = new JSONArray(new ArrayList<>(Arrays.asList(((JSONArray) grid.get(1)).get(0).toString(), ((JSONArray) grid.get(1)).get(1).toString(), ((JSONArray) grid.get(2)).get(0).toString(), ((JSONArray) grid.get(2)).get(2).toString(), ((JSONArray) grid.get(3)).get(1).toString(), ((JSONArray) grid.get(3)).get(2).toString())));
            }
            else if (gridSize == 5){
                ((TextView) gridLayout.findViewWithTag("00")).setText(((JSONArray) grid.get(0)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("02")).setText(((JSONArray) grid.get(2)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("22")).setText(((JSONArray) grid.get(2)).get(2).toString());
                ((TextView) gridLayout.findViewWithTag("04")).setText(((JSONArray) grid.get(4)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("24")).setText(((JSONArray) grid.get(4)).get(2).toString());
                ((TextView) gridLayout.findViewWithTag("44")).setText(((JSONArray) grid.get(4)).get(4).toString());
                answer = new JSONArray(new ArrayList<>(Arrays.asList(((JSONArray) grid.get(1)).get(0).toString(), ((JSONArray) grid.get(1)).get(1).toString(), ((JSONArray) grid.get(2)).get(1).toString(), ((JSONArray) grid.get(3)).get(0).toString(), ((JSONArray) grid.get(3)).get(1).toString(), ((JSONArray) grid.get(3)).get(2).toString(), ((JSONArray) grid.get(3)).get(3).toString(), ((JSONArray) grid.get(4)).get(1).toString(), ((JSONArray) grid.get(4)).get(3).toString())));
            }
            else{
                ((TextView) gridLayout.findViewWithTag("00")).setText(((JSONArray) grid.get(0)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("02")).setText(((JSONArray) grid.get(2)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("22")).setText(((JSONArray) grid.get(2)).get(2).toString());
                ((TextView) gridLayout.findViewWithTag("14")).setText(((JSONArray) grid.get(4)).get(1).toString());
                ((TextView) gridLayout.findViewWithTag("34")).setText(((JSONArray) grid.get(4)).get(3).toString());
                ((TextView) gridLayout.findViewWithTag("05")).setText(((JSONArray) grid.get(5)).get(0).toString());
                ((TextView) gridLayout.findViewWithTag("55")).setText(((JSONArray) grid.get(5)).get(5).toString());
                answer = new JSONArray(new ArrayList<>(Arrays.asList(((JSONArray) grid.get(1)).get(0).toString(), ((JSONArray) grid.get(1)).get(1).toString(), ((JSONArray) grid.get(2)).get(1).toString(), ((JSONArray) grid.get(3)).get(0).toString(), ((JSONArray) grid.get(3)).get(1).toString(), ((JSONArray) grid.get(3)).get(2).toString(), ((JSONArray) grid.get(3)).get(3).toString(), ((JSONArray) grid.get(4)).get(0).toString(), ((JSONArray) grid.get(4)).get(2).toString(), ((JSONArray) grid.get(4)).get(4).toString(), ((JSONArray) grid.get(5)).get(1).toString(), ((JSONArray) grid.get(5)).get(2).toString(), ((JSONArray) grid.get(5)).get(3).toString(), ((JSONArray) grid.get(5)).get(4).toString())));
            }

        } catch (Exception e){
            e.printStackTrace();
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

    public static String formatTime(int secs) {
        return String.format("%02d:%02d", (secs % 3600) / 60, secs % 60);
    }

    public void loadingDialogFunc(){
        loadingDialog = new LoadingDialog(GameActivityPiramit.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
        loadingDialog.startLoadingAnimation();
    }

    public void initDraftModeActiveVar(){
        for(int i = 0; i<answerCount; i++){
            draftModeActive[i] = false;
        }
    }

    public void clearGrid(){
        operations = new ArrayList<>();
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        for (int i = 0; i < answerCount; i++) {
            TextView currentBox = gridLayout.findViewWithTag("answer" + i);
            currentBox.setText("");
            currentBox.setEnabled(true);
        }
        if(!clickedBox.equals("-1")){
            gridLayout.findViewWithTag("answer" + clickedBox).setBackground(getResources().getDrawable(R.drawable.stroke_bg2));
        }
        clickedBox = "-1";
        timerInSeconds = 0;
        timerStopped=true;
    }

    public void mainFunc(){
        GridLayout numsLayout = findViewById(R.id.numsGL_ga);
        TextView undoTV = findViewById(R.id.undoTV_ga);
        TextView deleteTV = findViewById(R.id.deleteTV_ga);
        TextView resetTV = findViewById(R.id.resetTV_game);
        for(int i = 1; i<10; i++){
            numsLayout.findViewWithTag(Integer.toString(i)).setEnabled(true);
        }
        undoTV.setEnabled(true);
        deleteTV.setEnabled(true);
        resetTV.setEnabled(true);
        clearGrid();
        initDraftModeActiveVar();
        GetRequest getRequest = new GetRequest();
        getRequest.execute("https://akiloyunlariapp.herokuapp.com/Piramit"+gridSize,"fx!Ay:;<p6Q?C8N{");
        loadingDialogFunc();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        gameName = intent.getStringExtra("gameName");
        difficulty = intent.getStringExtra("difficulty");
        assert difficulty != null;
        switch (difficulty) {
            case "Easy":
            case "Kolay":
                setContentView(R.layout.activity_game_piramit3);
                gridSize = 3;
                answerCount = 3;
                break;
            case "Medium":
            case "Orta":
                setContentView(R.layout.activity_game_piramit4);
                gridSize = 4;
                answerCount = 6;
                break;
            case "Hard":
            case "Zor":
                setContentView(R.layout.activity_game_piramit5);
                gridSize = 5;
                answerCount = 9;
                break;
            default:
                setContentView(R.layout.activity_game_piramit6);
                gridSize = 6;
                answerCount = 14;
                break;
        }

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
        if(paused && gotQuestion){
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