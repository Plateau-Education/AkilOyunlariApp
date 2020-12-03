package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GameActivityPentomino extends AppCompatActivity {

    String gameName;
    String difficulty;
    String clickedFigure = "-1";
    int gridSizeX = 6;
    int gridSizeY = 4;
    int timerInSeconds = 0;
    int pxHeightX;
    int pxHeightY;
    int cXCoor, cYCoor, cRowSpan, cColumnSpan;
    boolean timerStopped=false;
    boolean paused = false;
    boolean gotQuestion = false;

    List<List<String>> operations = new ArrayList<>();
    List<String> clueIndexes = new ArrayList<>();
    List<String> answer = new ArrayList<>();
    LoadingDialog loadingDialog;
    Handler timerHandler;
    Runnable runnable;
    GridLayout gridLayoutGrid;
    View.OnLongClickListener onLongClickListener;

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
    public void changeClicked(View view){
        TextView box = (TextView) view;
        String answerIndex = box.getTag().toString();
        if(!clueIndexes.contains(answerIndex)) {
            String op;
            if (Objects.equals(box.getBackground().getConstantState(), getResources().getDrawable(R.drawable.stroke_bg).getConstantState())) {
                box.setBackground(getResources().getDrawable(R.drawable.ic_diamond));
                op = "-1";
            }
            else if (Objects.equals(box.getBackground().getConstantState(), getResources().getDrawable(R.drawable.ic_diamond).getConstantState())) {
                box.setBackground(getResources().getDrawable(R.drawable.ic_cross));
                op = "-2";
            }
            else {
                box.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                op = "0";
            }
            clickedFigure = answerIndex;
            List<String> newOp = new ArrayList<>(Arrays.asList(answerIndex, op));
            if(!newOp.equals(operations.get(operations.size() - 1))){
                operations.add(new ArrayList<>(Arrays.asList(answerIndex, op)));
            }
            Log.i("operations",operations+"");
            checkAnswer(null);
        }
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
            clickedFigure = "-1";
            for (int i = 0; i < gridSizeY; i++) {
                for (int j = 0; j < gridSizeX; j++) {
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

    public void reverseFigure(View view){
//        LinearLayout ll1 = findViewById(R.id.figureLLrow1_ga);
//        LinearLayout ll2 = findViewById(R.id.figureLLrow2_ga);
        ImageView clickedIV = gridLayoutGrid.findViewWithTag(clickedFigure);
        if(clickedIV.getScaleX() == -1f) clickedIV.setScaleX(1f);
        else clickedIV.setScaleX(-1f);
    }

    public void rotateFigure(View view){
        ImageView clickedIV = gridLayoutGrid.findViewWithTag(clickedFigure);
        if(clickedIV.getRotation() == 270f) clickedIV.setRotation(0f);
        else clickedIV.setRotation(clickedIV.getRotation()+90f);

        gridLayoutGrid.removeView(clickedIV);
        gridLayoutGrid.addView(clickedIV, new GridLayout.LayoutParams(GridLayout.spec(cYCoor, cColumnSpan, GridLayout.CENTER), GridLayout.spec(cXCoor, cRowSpan, GridLayout.CENTER)));

        clickedIV.getLayoutParams().width = (int) (clickedIV.getHeight());
        clickedIV.getLayoutParams().height = (int) (clickedIV.getWidth());

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkAnswer(View view){
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        boolean checking=true;
//        for(int i = 0; i<gridSizeY; i++){
//            for(int j = 0; j<gridSizeX; j++){
//                String co = Integer.toString(j)+i;
//                if(answer.contains(co) && !Objects.equals(gridLayout.findViewWithTag(co).getBackground().getConstantState(), getResources().getDrawable(R.drawable.ic_diamond).getConstantState())){
//                    checking=false;
//                    break;
//                }
//                else if(!answer.contains(co) && Objects.equals(gridLayout.findViewWithTag(co).getBackground().getConstantState(), getResources().getDrawable(R.drawable.ic_diamond).getConstantState())){
//                    checking=false;
//                    break;
//                }
//            }
//        }
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

    @SuppressLint("StaticFieldLeak")
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
            //noinspection deprecation
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
        final GridLayout gridGL = findViewById(R.id.gridGL_ga);
        Log.i("index",gridGL.findViewWithTag("22").getX() +" / "+ gridGL.findViewWithTag("22").getX());
        JSONArray realGrid = grid.getJSONArray(0);
        JSONArray usedFigures = grid.getJSONArray(1);
        JSONArray blockIndexes = grid.getJSONArray(2);

        gridSizeY = realGrid.length();
        gridSizeX = realGrid.getJSONArray(0).length();

        ConstraintLayout cL = ((ConstraintLayout)findViewById(R.id.gridRelativeLayoutView).getParent());
//        cL.setLayoutParams(new ConstraintLayout.LayoutParams((int) ((gridSizeX*300/7) * getResources().getDisplayMetrics().density),(int) ((gridSizeY*300/7) * getResources().getDisplayMetrics().density)));
        cL.getLayoutParams().width = (int) ((gridSizeX*300/7) * getResources().getDisplayMetrics().density);
        cL.getLayoutParams().height = (int) ((gridSizeY*300/7) * getResources().getDisplayMetrics().density);

        Log.i("width/height",(int) ((gridSizeX*300/7) * getResources().getDisplayMetrics().density)+" / "+(int) ((gridSizeY*300/7) * getResources().getDisplayMetrics().density));
        final RelativeLayout relativeLayout = (RelativeLayout) gridGL.getParent();
        relativeLayout.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        GridLayout gridLayout = (GridLayout) inflater.inflate(this.getResources().getIdentifier("pentomino_"+gridSizeY+gridSizeX, "layout", this.getPackageName()),null);
        relativeLayout.addView(gridLayout);
        relativeLayout.setVisibility(View.VISIBLE);

        Log.i("blockIndexes",blockIndexes+"");
        for(int b = 0; b < blockIndexes.length(); b++){
            JSONArray xy = blockIndexes.getJSONArray(b);
            Log.i("xyblock",xy+"");
            (gridLayout.findViewWithTag(Integer.toString(xy.getInt(1))+xy.getInt(0))).setBackground(getResources().getDrawable(R.color.near_black_blue));
        }
//        ArrayList<String> usedFiguresList = new ArrayList<>();
        for (int i=0;i<usedFigures.length();i++) {
            String figure = Character.toString((char) usedFigures.getInt(i)).toLowerCase();
            ImageView currentIV;
            if (i < 4) {
                currentIV = findViewById(R.id.figureLLrow1_ga).findViewWithTag(Integer.toString(i));
            } else {
                currentIV = findViewById(R.id.figureLLrow2_ga).findViewWithTag(Integer.toString(i));
            }
            currentIV.setImageResource(this.getResources().getIdentifier("ic_pento" + figure, "drawable", this.getPackageName()));
            currentIV.setVisibility(View.VISIBLE);
            currentIV.setOnLongClickListener(onLongClickListener);
            if ("i".contains(figure)) {
                currentIV.getLayoutParams().height = (int) (80 * getResources().getDisplayMetrics().density);
                currentIV.getLayoutParams().width = (int) (18 * getResources().getDisplayMetrics().density);
            } else if ("lny".contains(figure)) {
                currentIV.getLayoutParams().height = (int) (64 * getResources().getDisplayMetrics().density);
                currentIV.getLayoutParams().width = (int) (32 * getResources().getDisplayMetrics().density);
            } else if ("p".contains(figure)) {
                currentIV.getLayoutParams().height = (int) (48 * getResources().getDisplayMetrics().density);
                currentIV.getLayoutParams().width = (int) (32 * getResources().getDisplayMetrics().density);
            } else if ("u".contains(figure)) {
                currentIV.getLayoutParams().height = (int) (32 * getResources().getDisplayMetrics().density);
                currentIV.getLayoutParams().width = (int) (48 * getResources().getDisplayMetrics().density);
            } else {
                currentIV.getLayoutParams().height = (int) (48 * getResources().getDisplayMetrics().density);
                currentIV.getLayoutParams().width = (int) (48 * getResources().getDisplayMetrics().density);
            }
            currentIV.setTag(figure);
            Log.i("currentIVTag",currentIV.getTag().toString());
//            usedFiguresList.add(figure);
        }
        gridLayoutGrid = (GridLayout) relativeLayout.getChildAt(0);

//        for(int i = 0; i < gridSizeY; i++){
//            for(int j = 0; j <  gridSizeX; j++) {
//                String n = ((JSONArray)grid.get(i)).get(j).toString();
//                if(Integer.parseInt(n) > 0){
//                    clueIndexes.add(Integer.toString(j)+i);
//                    ((TextView) gridLayout.findViewWithTag(Integer.toString(j)+i)).setText(n);
//                }
//                else if(n.equals("-1")){
//                    answer.add(Integer.toString(j)+i);
//                }
//            }
//        }
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
        loadingDialog = new LoadingDialog(GameActivityPentomino.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
        loadingDialog.startLoadingAnimation();
    }

    public void clearGrid(){
        operations = new ArrayList<>();
        operations.add(new ArrayList<>(Arrays.asList("00", "0")));
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        for (int i = 0; i < gridSizeY ; i++) {
            for (int j = 0; j < gridSizeX; j++) {
                TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                tv.setText("");
                tv.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                tv.setEnabled(true);
            }
        }
        clickedFigure = "-1";
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
//        clearGrid();
        GetRequest getRequest = new GetRequest();
        //noinspection deprecation
        getRequest.execute("https://akiloyunlariapp.herokuapp.com/Pentomino"+difficulty,"fx!Ay:;<p6Q?C8N{");
        loadingDialogFunc();
    }

    public String[] xyToRowColumn(float x, float y){
        String[] rowColumn = new String[2];
        if(x < 0) x = 0;
        if(y < 0) y = 0;
        float coefX = (float)pxHeightX/gridSizeX;
        float coefY = (float)pxHeightY/gridSizeY;
        rowColumn[0] = Integer.toString((int) Math.floor(x/coefX));
        rowColumn[1] = Integer.toString((int) Math.floor(y/coefY));
        return rowColumn;
    }

    public String[] firstBoxToFigure(String firstBoxAndInfo){
        String figure = firstBoxAndInfo.substring(0,1);
        String boxIndex = firstBoxAndInfo.substring(1,3);
        String inverse = firstBoxAndInfo.substring(3,4);
        String rotation  =  firstBoxAndInfo.substring(4);
        int row = Integer.parseInt(String.valueOf(boxIndex.charAt(0)));
        int column = Integer.parseInt(String.valueOf(boxIndex.charAt(1)));

        String[] boxes = new String[5];
        switch (figure) {
            case "f":
                if(inverse.equals("+")){
                    switch (rotation) {
                        case "0":
                            boxes[0] = Integer.toString(row) + (column + 1);
                            boxes[1] = Integer.toString(row) + (column + 2);
                            boxes[3] = Integer.toString((row + 2)) + (column + 1);
                            boxes[4] = Integer.toString((row + 1)) + column;
                            break;
                        case "90":
                            boxes[0] = Integer.toString(row) + (column + 1);
                            boxes[1] = Integer.toString((row + 1)) + column;
                            boxes[3] = Integer.toString((row + 1)) + (column + 2);
                            boxes[4] = Integer.toString((row + 2)) + (column + 2);
                            break;
                        case "180":
                            boxes[0] = Integer.toString(row) + (column + 1);
                            boxes[1] = Integer.toString((row + 2)) + column;
                            boxes[3] = Integer.toString((row + 1)) + (column + 2);
                            boxes[4] = Integer.toString((row + 2)) + (column + 1);
                            break;
                        case "270":
                            boxes[0] = Integer.toString(row) + column;
                            boxes[1] = Integer.toString((row + 1)) + column;
                            boxes[3] = Integer.toString((row + 1)) + (column + 2);
                            boxes[4] = Integer.toString((row + 2)) + (column + 1);
                            break;
                    }
                }
                else{
                    switch (rotation) {
                        case "0":
                            boxes[0] = Integer.toString(row) + (column + 1);
                            boxes[1] = Integer.toString(row) + column;
                            boxes[3] = Integer.toString((row + 2)) + (column + 1);
                            boxes[4] = Integer.toString((row + 1)) + (column + 2);
                            break;
                        case "90":
                            boxes[0] = Integer.toString((row + 2)) + (column + 1);
                            boxes[1] = Integer.toString((row + 1)) + column;
                            boxes[3] = Integer.toString((row + 1)) + (column + 2);
                            boxes[4] = Integer.toString(row) + (column + 2);
                            break;
                        case "180":
                            boxes[0] = Integer.toString(row) + (column + 1);
                            boxes[1] = Integer.toString(row) + (column + 2);
                            boxes[3] = Integer.toString((row + 1)) + column;
                            boxes[4] = Integer.toString((row + 2)) + (column + 1);
                            break;
                        case "270":
                            boxes[0] = Integer.toString((row + 2)) + column;
                            boxes[1] = Integer.toString((row + 1)) + column;
                            boxes[3] = Integer.toString((row + 1)) + (column + 2);
                            boxes[4] = Integer.toString(row) + (column + 1);
                            break;
                    }
                }
                boxes[2] = Integer.toString((row+1)) + (column+1);
                break;
            case "i":
                switch (rotation) {
                    case "0":
                    case "180":
                        boxes[0] = Integer.toString(row) + column;
                        boxes[1] = Integer.toString((row + 1)) + column;
                        boxes[3] = Integer.toString((row + 3)) + column;
                        boxes[4] = Integer.toString((row + 4)) + column;
                        break;
                    case "90":
                    case "270":
                        boxes[0] = Integer.toString(row) + (column - 2);
                        boxes[1] = Integer.toString(row) + (column - 1);
                        boxes[3] = Integer.toString(row) + (column + 1);
                        boxes[4] = Integer.toString(row) + (column + 2);
                        break;
                    }
                boxes[2] = Integer.toString((row+2)) + column;

                break;
            case "l":

                break;
            case "n":

                break;
            case "p":

                break;
            case "t":

                break;
            case "u":

                break;
            case "v":

                break;
            case "w":

                break;
            case "x":

                break;
            case "y":

                break;
            case "z":

                break;
        }
        return boxes;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        gameName = intent.getStringExtra("gameName");
        difficulty = intent.getStringExtra("difficulty");
        assert difficulty != null;
        if(difficulty.equals("Easy") || difficulty.equals("Kolay")){
            setContentView(R.layout.activity_game_pentomino);
            ((TextView)findViewById(R.id.diffTV_game)).setText(R.string.Easy);
            difficulty = "Easy";
//            gridSize=5;
        }

        else if(difficulty.equals("Medium") || difficulty.equals("Orta")){
            setContentView(R.layout.activity_game_pentomino);
            ((TextView)findViewById(R.id.diffTV_game)).setText(R.string.Medium);
            difficulty = "Medium";
//            gridSize=8;
        }
        else{
            setContentView(R.layout.activity_game_pentomino);
            ((TextView)findViewById(R.id.diffTV_game)).setText(R.string.Hard);
            difficulty = "Hard";
//            gridSize=10;
        }

        onLongClickListener = new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onLongClick(View view) {
//                View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(view);
                if(!view.getParent().equals(gridLayoutGrid)){
                    view.startDragAndDrop(null, new CustomDragShadowBuilder(view, (300f/7)), view,View.DRAG_FLAG_OPAQUE);
                } else {
                    view.startDragAndDrop(null, new CustomDragShadowBuilder(view, 15), view,View.DRAG_FLAG_OPAQUE);
                }

                view.setVisibility(View.INVISIBLE);

                return false;
            }
        };

        mainFunc();


        pxHeightY = (int) ((300/7f)*gridSizeY * getResources().getDisplayMetrics().density);
        pxHeightX = (int) ((300/7f)*gridSizeX * getResources().getDisplayMetrics().density);
        ((ConstraintLayout)findViewById(R.id.gridRelativeLayoutView).getParent()).setOnDragListener(dragListener);
        findViewById(R.id.figureLLrow1_ga).setOnDragListener(dragListener);
        findViewById(R.id.figureLLrow2_ga).setOnDragListener(dragListener);
//        findViewById(R.id.gridGL_ga).setOnDragListener(dragListener);

    }
    public View.OnDragListener dragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View view, final DragEvent dragEvent) {
            switch(dragEvent.getAction()){
                case(DragEvent.ACTION_DRAG_STARTED):
                    return true;
                case(DragEvent.ACTION_DRAG_ENTERED):
                case(DragEvent.ACTION_DRAG_ENDED):
                case(DragEvent.ACTION_DRAG_EXITED):
                    view.invalidate();
                    return true;
                case(DragEvent.ACTION_DRAG_LOCATION):
                    Log.i("x / y",dragEvent.getX()+" / "+dragEvent.getY());
//                    if(!((View)dragEvent.getLocalState()).getParent().equals(view))
                        try{
                            int xCoor = (int) (dragEvent.getX() - (((View) dragEvent.getLocalState()).getWidth() * ((300f / 7) / 15f))/2);
                            int yCoor = (int) (dragEvent.getY() - (((View) dragEvent.getLocalState()).getHeight() * ((300f / 7) / 15f))/2);
                            gridLayoutGrid.findViewWithTag(xyToRowColumn(xCoor,yCoor)[0]+xyToRowColumn(xCoor,yCoor)[1]).setBackground(getResources().getDrawable(R.color.shallow_light_blue_green));
                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    return true;
                case(DragEvent.ACTION_DROP):
//                    ClipData.Item item = dragEvent.getClipData().getItemAt(0);
//                    String dragData = (String) item.getText();
//                    Toast.makeText(MainActivity.this, dragData, Toast.LENGTH_SHORT).show();


                    view.invalidate();

                    final View v = (View) dragEvent.getLocalState();
                    clickedFigure = v.getTag().toString();
                    ViewGroup owner = (ViewGroup) v.getParent();
                    owner.removeView(v);
                    ViewGroup destination = (ViewGroup) view;


//                    v.setLayoutParams(new ConstraintLayout.LayoutParams((int) (v.getWidth()*((300f/7)/15f)), (int) (v.getHeight()*((300f/7)/15f))));
//                    destination.addView(v);


                    int rowSpan = (int) ((v.getHeight()) / (16 * getResources().getDisplayMetrics().density));
                    int columnSpan = (int) ((v.getWidth()) / (16 * getResources().getDisplayMetrics().density));
                    Log.i("spans",rowSpan+" / "+columnSpan);
                    if(!destination.equals(findViewById(R.id.figureLLrow1_ga))) {
                        int xCoor = (int) (dragEvent.getX() - (v.getWidth() * ((300f / 7) / 15f))/2);
                        int yCoor = (int) (dragEvent.getY() - (v.getHeight() * ((300f / 7) / 15f))/2);
                        cXCoor = Integer.parseInt(xyToRowColumn(xCoor, yCoor)[1]) + 1;
                        cYCoor = Integer.parseInt(xyToRowColumn(xCoor, yCoor)[0]) + 1;
                        cRowSpan = rowSpan;
                        cColumnSpan = columnSpan;

//                        gridLayoutGrid.addView(v, new GridLayout.LayoutParams(GridLayout.spec(Integer.parseInt(xyToRowColumn(xCoor, yCoor)[1]) + 1, rowSpan, GridLayout.CENTER), GridLayout.spec(Integer.parseInt(xyToRowColumn(xCoor, yCoor)[0]) + 1, columnSpan, GridLayout.CENTER)));
//                    gridLayoutGrid.addView(v, new GridLayout.LayoutParams(GridLayout.spec(Integer.parseInt(xyToRowColumn(dragEvent.getX(),dragEvent.getY())[0]),5,GridLayout.CENTER),GridLayout.spec(Integer.parseInt(xyToRowColumn(dragEvent.getX(),dragEvent.getY())[1]),1,GridLayout.CENTER)));

                        if(!owner.equals(gridLayoutGrid)) {
                            v.getLayoutParams().width = (int) (v.getWidth() * ((300f / 7) / 15f));
                            v.getLayoutParams().height = (int) (v.getHeight() * ((300f / 7) / 15f));
                        } else{
                            v.getLayoutParams().width = (int) (v.getWidth() * (15 / 15f));
                            v.getLayoutParams().height = (int) (v.getHeight() * (15 / 15f));
                        }
                        v.setVisibility(View.VISIBLE);
                    }
//                    v.setX(dragEvent.getX()-((v.getWidth()/2f) * getResources().getDisplayMetrics().density));
//                    v.setY(dragEvent.getY()-((v.getHeight()/2f) * getResources().getDisplayMetrics().density));
//                    v.setLayoutParams(new ConstraintLayout.LayoutParams((int) (v.getWidth()*((300f/7)/15f)), (int) (v.getHeight()*((300f/7)/15f))));

                    return true;
                default: return false;
            }
        }
    };

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