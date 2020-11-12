package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

public class GameActivityPatika extends AppCompatActivity {

    String gameName;
    String difficulty;
    String clickedBox = "-1";
    String previousCoor;
    String[] rowColumn;
    int gridSize = 6;
    int timerInSeconds = 0;
    boolean timerStopped=false;
    boolean paused = false;
    boolean gotQuestion = false;

    List<List<String>> operations = new ArrayList<>();
    List<String> clueIndexes = new ArrayList<>();
    List<String> answer = new ArrayList<>();
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void undoOperation(View view){
        if(operations.size() > 1){
//            operations = operations.subList(0,operations.size()-1);
            List<String> tuple1 = operations.get(operations.size()-1);
            operations = operations.subList(0,operations.size()-1);
            String co1 = tuple1.get(0);
            String num2 = "0";
            String co2;
            for(int i = operations.size()-1; i>0; i--){
                List<String> tuple2 = operations.get(i);
                co2 = tuple2.get(0);
                if(co1.equals(co2)){
                    num2 = tuple2.get(1);
                    break;
                }
            }
            Log.i("co/num",co1+" / "+num2);
            GridLayout gridLayout = findViewById(R.id.gridGL_ga);
            TextView currentBox = gridLayout.findViewWithTag(co1);
            if(num2.equals("-1")){
                currentBox.setBackground(getResources().getDrawable(R.drawable.ic_diamond));
            }
            else if(num2.equals("-2")){
                currentBox.setBackground(getResources().getDrawable(R.drawable.ic_cross));
            }
            else{
                currentBox.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
            }
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
            operations.add(new ArrayList<>(Arrays.asList("00", "0")));
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
        for(int i = 0; i<gridSize; i++){
            for(int j = 0; j<gridSize; j++){
                String co = Integer.toString(j)+i;
                if(answer.contains(co) && !Objects.equals(gridLayout.findViewWithTag(co).getBackground().getConstantState(), getResources().getDrawable(R.drawable.ic_diamond).getConstantState())){
                    checking=false;
                    break;
                }
                else if(!answer.contains(co) && Objects.equals(gridLayout.findViewWithTag(co).getBackground().getConstantState(), getResources().getDrawable(R.drawable.ic_diamond).getConstantState())){
                    checking=false;
                    break;
                }
            }
        }
//        for(int i = 0; i < gridSize; i++){
//            for(int j = 0; j <  gridSize; j++){
//                try {
//                    if(!((TextView)gridLayout.findViewWithTag(Integer.toString(j)+ i)).getText().equals(((JSONArray)answer.get(i)).get(j).toString())){
//                        checking=false;
//                    }
//                    Log.i("checking",((TextView)gridLayout.findViewWithTag(Integer.toString(j)+i)).getText().toString()+" / "+(((JSONArray)answer.get(i)).get(j).toString()));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        Log.i("check",checking+"  "+answer);
        if(checking){
            timerStopped=true;
            LayoutInflater factory = LayoutInflater.from(this);
            final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
            final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
            TextView timerTV = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
            timerTV.setText(formatTime(timerInSeconds));
            correctDialog.setView(leaveDialogView);

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
                    startActivity(intent);
                    overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                    correctDialog.dismiss();
                }
            });
            correctDialog.show();
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

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                org.json.JSONObject jb = new org.json.JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));
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

    public void seperateGridAnswer(JSONArray grid) throws JSONException {
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        for(int i = 0; i < gridSize; i++){
            for(int j = 0; j <  gridSize; j++) {
                String n = ((JSONArray)grid.get(i)).get(j).toString();
                if(Integer.parseInt(n) > 0){
                    clueIndexes.add(Integer.toString(j)+i);
                    ((TextView) gridLayout.findViewWithTag(Integer.toString(j)+i)).setText(n);
                }
                else if(n.equals("-1")){
                    answer.add(Integer.toString(j)+i);
                }
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
        loadingDialog = new LoadingDialog(GameActivityPatika.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
        loadingDialog.startLoadingAnimation();
    }

    public String[] xyToRowColumn(float x, float y){
        String[] rowColumn = new String[2];
        rowColumn[0] = Integer.toString((int) Math.floor((x/180)));
        rowColumn[1] = Integer.toString((int) Math.floor(y/180));
        return rowColumn;
    }

    public int[] middlePoint(String coor){
        int x = Integer.parseInt(String.valueOf(coor.charAt(0)));
        int y = Integer.parseInt(String.valueOf(coor.charAt(1)));
        int[] middle_point = new int[2];
        middle_point[0] = 180*x+90;
        middle_point[1] = 180*y+90;
        return middle_point;
    }

    public void drawALine(float startX, float startY, float stopX, float stopY){
        ImageView imageView = findViewById(R.id.canvasIV);
//        Log.i("x1,y1,x2,y2",startX+"  "+startY+"  "+stopX+"  "+stopY);
        if(startY-stopY==0){
            if(startX<stopX) canvas.drawLine(startX-6, startY, stopX+6, stopY, paint);
            else canvas.drawLine(startX+6, startY, stopX-6, stopY, paint);
        } else {
            if(startY<stopY) canvas.drawLine(startX, startY-6, stopX, stopY-6, paint);
            else canvas.drawLine(startX, startY+6, stopX, stopY-6, paint);
        }
        imageView.setImageBitmap(bitmap);
    }

    public void clearGrid(){
        operations = new ArrayList<>();
        operations.add(new ArrayList<>(Arrays.asList("00", "0")));
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
        answer = new ArrayList<>();
        timerInSeconds = 0;
        timerStopped=true;
    }

    public void mainFunc(){
        TextView undoTV = findViewById(R.id.undoTV_ga);
        TextView resetTV = findViewById(R.id.resetTV_game);
        undoTV.setEnabled(true);
        resetTV.setEnabled(true);
        clearGrid();
        GetRequest getRequest = new GetRequest();
        getRequest.execute("https://akiloyunlariapp.herokuapp.com/HazineAvi"+gridSize,"fx!Ay:;<p6Q?C8N{");
        loadingDialogFunc();
    }

    public void initSomeVar(){
        bitmap = Bitmap.createBitmap(900, 900, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(getResources().getColor(R.color.transparent));
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.near_black_blue));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(12);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        gameName = intent.getStringExtra("gameName");
        difficulty = intent.getStringExtra("difficulty");
        assert difficulty != null;
        if(difficulty.equals("Easy") || difficulty.equals("Kolay")){
            setContentView(R.layout.activity_game_patika5);
            gridSize=5;
        }
//        else if(difficulty.equals("Medium") || difficulty.equals("Orta")){
//            setContentView(R.layout.activity_game_hazine_avi8);
//            gridSize=8;
//        }
//        else{
//            setContentView(R.layout.activity_game_hazine_avi10);
//            gridSize=10;
//        }

        initSomeVar();

        final GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        gridLayout.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float mx = motionEvent.getX();
                float my = motionEvent.getY();
                Log.i("x / y",mx + " / " + my);
                if(mx >= 0 && mx <= 900 && my >= 0 && my <= 900){
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            rowColumn = xyToRowColumn(mx,my);
                            previousCoor = rowColumn[0] + rowColumn[1];
                            break;
                        case MotionEvent.ACTION_MOVE:
                            rowColumn = xyToRowColumn(mx,my);
                            String currentCoor = rowColumn[0] + rowColumn[1];
                            Log.i("coor",currentCoor);
                            if(!currentCoor.equals(previousCoor)){
                                int[] firstMP = middlePoint(previousCoor);
                                int[] secondMP = middlePoint(currentCoor);
                                drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1]);
                                previousCoor = currentCoor;
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            break;
                    }

//                    String[] rowColumn = xyToRowColumn(mx,my);
//                    String currentCoor = rowColumn[0] + rowColumn[1];
//                    Log.i("coor",currentCoor);
//                    if (Objects.equals(currentBox.getBackground().getConstantState(), getResources().getDrawable(R.drawable.stroke_bg).getConstantState()))
//                        currentBox.setBackground(getResources().getDrawable(R.color.near_black_blue));
                }
                return true;
            }
        });

//        mainFunc();

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