package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class TournamentActivity extends AppCompatActivity {

    String username;
    String code = "0";
    String userType = "participant";
    String organizatorName;
    int currentQ = 0;
    int totalNumberOfQs = 0;
    int currentTotalScore = 0;
    int solveTime = 300;
    long currentTimeInMillis;
    long afterFiveMinMillis;

    Map<String, Integer> gamesMap = new HashMap<>();
    Map<String, Integer> playersMap = new HashMap<>();
    List<List<String>> tournamentProperties = new ArrayList<>();
    List<String> gameOrder = new ArrayList<>();
    JSONObject allQs = new JSONObject();

    ConstraintLayout waitingDialog;
    LinearLayout createOrJoinLL;
    ConstraintLayout tournamentOptionsCL;
    ConstraintLayout participantsLayout;
    LinearLayout scoresLL;


    //Hazine Avi Variables
    String clickedBox = "-1";
    String switchPosition = "diamond";
    int gridSize = 5;
    int timerInSeconds = 0;
    boolean timerStopped=false;
    boolean paused = false;
    boolean gotQuestion = false;

    List<List<String>> operations = new ArrayList<>();
    List<String> clueIndexes = new ArrayList<>();
    List<String> answer = new ArrayList<>();
    Handler timerHandler;
    Runnable runnable;

    public String shownToDatabase(String visibleOrDatabase, String string){
        Map<String,String> visibleToDB = new HashMap<>();
        List<String> shownGameNames = new ArrayList<>(Arrays.asList(
                "Sudoku 6x6 "+getString(R.string.Easy), "Sudoku 6x6 "+getString(R.string.Medium), "Sudoku 6x6 "+getString(R.string.Hard),
                "Sudoku 9x9 "+getString(R.string.Easy), "Sudoku 9x9 "+getString(R.string.Medium), "Sudoku 9x9 "+getString(R.string.Hard),
                "Hazine Avı "+getString(R.string.Easy), "Hazine Avı "+getString(R.string.Medium), "Hazine Avı "+getString(R.string.Hard),
                "Patika "+getString(R.string.Easy), "Patika "+getString(R.string.Medium), "Patika "+getString(R.string.Hard),
                "Sayı Bulmaca "+getString(R.string.Easy), "Sayı Bulmaca "+getString(R.string.Medium), "Sayı Bulmaca "+getString(R.string.Hard),
                "Sözcük Turu "+getString(R.string.Easy), "Sözcük Turu "+getString(R.string.Medium), "Sözcük Turu "+getString(R.string.Hard), "Sözcük Turu "+getString(R.string.VeryHard),
                "Piramit "+getString(R.string.Easy), "Piramit "+getString(R.string.Medium), "Piramit "+getString(R.string.Hard), "Piramit "+getString(R.string.VeryHard)));
        List<String> databaseGameNames = new ArrayList<>(Arrays.asList(
                "Sudoku.6.Easy", "Sudoku.6.Medium", "Sudoku.6.Hard", "Sudoku.9.Easy", "Sudoku.9.Medium", "Sudoku.9.Hard",
                "HazineAvi.5", "HazineAvi.8", "HazineAvi.10", "Patika.5", "Patika.7", "Patika.9",
                "SayiBulmaca.3", "SayiBulmaca.4", "SayiBulmaca.5", "SozcukTuru.Easy", "SozcukTuru.Medium", "SozcukTuru.Hard", "SozcukTuru.Hardest",
                "Piramit.3", "Piramit.4","Piramit.5","Piramit.6"));
        for(int i = 0; i<shownGameNames.size(); i++)
            visibleToDB.put(shownGameNames.get(i),databaseGameNames.get(i));

        if(visibleOrDatabase.equals("shownToDatabase")){
            return visibleToDB.get(string);
        }
        else{
            Map<String, String> dbToVisible = new HashMap<>();
            for(Map.Entry<String, String> entry : visibleToDB.entrySet()){
                dbToVisible.put(entry.getValue(), entry.getKey());
            }
            return dbToVisible.get(string);
        }
    }

    public int gameDiffToLayoutID(String gameDiff){
        List<String> databaseGameNames = new ArrayList<>(Arrays.asList(
                "Sudoku.6.Easy", "Sudoku.6.Medium", "Sudoku.6.Hard", "Sudoku.9.Easy", "Sudoku.9.Medium", "Sudoku.9.Hard",
                "HazineAvi.5", "HazineAvi.8", "HazineAvi.10", "Patika.5", "Patika.7", "Patika.9",
                "SayiBulmaca.3", "SayiBulmaca.4", "SayiBulmaca.5", "SozcukTuru.Easy", "SozcukTuru.Medium", "SozcukTuru.Hard", "SozcukTuru.Hardest",
                "Piramit.3", "Piramit.4","Piramit.5","Piramit.6"));
        List<Integer> layoutIDs = new ArrayList<>(Arrays.asList(
                R.layout.activity_game_sudoku6, R.layout.activity_game_sudoku6, R.layout.activity_game_sudoku6,
                R.layout.activity_game_sudoku9, R.layout.activity_game_sudoku9, R.layout.activity_game_sudoku9,
                R.layout.activity_game_hazine_avi5, R.layout.activity_game_hazine_avi8, R.layout.activity_game_hazine_avi10,
                R.layout.activity_game_patika5, R.layout.activity_game_patika7, R.layout.activity_game_patika9,
                R.layout.activity_game_sayibulmaca3, R.layout.activity_game_sayibulmaca4, R.layout.activity_game_sayibulmaca5,
                R.layout.activity_game_sozcuk_turu34, R.layout.activity_game_sozcuk_turu35, R.layout.activity_game_sozcuk_turu45, R.layout.activity_game_sozcuk_turu55,
                R.layout.activity_game_piramit3, R.layout.activity_game_piramit4, R.layout.activity_game_piramit5 , R.layout.activity_game_piramit6));

        Map<String, Integer> map = new HashMap<>();
        for(int i = 0; i<databaseGameNames.size(); i++)
            map.put(databaseGameNames.get(i),layoutIDs.get(i));
        return map.get(gameDiff);
    }

    public void wannaLeaveDialog(View view){
        LayoutInflater factory = LayoutInflater.from(this);
        final View leaveDialogView = factory.inflate(R.layout.leave_dialog, null);
        final AlertDialog leaveDialog = new AlertDialog.Builder(this).create();
        leaveDialog.setView(leaveDialogView);

        leaveDialogView.findViewById(R.id.leaveDialogYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectSocket();
                Intent intent = new Intent(getApplicationContext(), GameTypesActivity.class);
                if(userType.equals("instructor") || userType.equals("student")){
                    intent = new Intent(getApplicationContext(), MyClassActivity.class);
                }
                startActivity(intent);
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
//                timerStopped=true;
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

//    public void nextQuestion(View view){
//        if(timerStopped){
//            LayoutInflater factory = LayoutInflater.from(this);
//            final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
//            final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
//            TextView timerTV = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
//            timerTV.setVisibility(GONE);
//            correctDialog.setView(leaveDialogView);
//
//            leaveDialogView.findViewById(R.id.correctDialogNext).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    try {
//                        mainFunc();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    correctDialog.dismiss();
//                }
//            });
////            leaveDialogView.findViewById(R.id.correctDialogGameMenu).setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
////                    startActivity(intent);
////                    overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
////                    correctDialog.dismiss();
////                }
////            });
//            leaveDialogView.findViewById(R.id.correctDialogGameMenu).setVisibility(GONE);
//            correctDialog.show();
//        }
//    } // Sonraki soruya geç

    public void goToGameTypes(View view){
        disconnectSocket();
        Intent intent = new Intent(getApplicationContext(), GameTypesActivity.class);
        if(userType.equals("instructor") || userType.equals("student"))
            intent = new Intent(getApplicationContext(), MyClassActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void changeClicked(View view){
        TextView box = (TextView) view;
        String answerIndex = box.getTag().toString();
        if(!clueIndexes.contains(answerIndex)) {
            String op = null;
            if (switchPosition.equals("diamond")) {
                if (Objects.equals(box.getBackground().getConstantState(), getResources().getDrawable(R.drawable.ic_diamond).getConstantState())) {
                    box.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                    op = "0";
                } else {
                    box.setBackground(getResources().getDrawable(R.drawable.ic_diamond));
                    op = "-1";
                }
            }
            else if (switchPosition.equals("cross")) {
                if (Objects.equals(box.getBackground().getConstantState(), getResources().getDrawable(R.drawable.ic_cross).getConstantState())) {
                    box.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                    op = "0";
                } else {
                    box.setBackground(getResources().getDrawable(R.drawable.ic_cross));
                    op = "-2";
                }
            }
            clickedBox = answerIndex;
            List<String> newOp = new ArrayList<>(Arrays.asList(answerIndex, op));
            if(!newOp.equals(operations.get(operations.size() - 1))){
                operations.add(new ArrayList<>(Arrays.asList(answerIndex, op)));
            }
            Log.i("operations",operations+"");
            checkAnswer(null);
        }
    } // Tıklanan kutuya elmas/çarpı koy

    public void changeSwitch(View view){
        ImageView switchTV = (ImageView) view;
        if(switchPosition.equals("diamond")){
            switchTV.setImageResource(R.drawable.ic_cross);
            switchPosition = "cross";
        }
        else if(switchPosition.equals("cross")){
            switchTV.setImageResource(R.drawable.ic_diamond);
            switchPosition = "diamond";
        }
    } // Elmas - çarpı değiştir

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
    } // Son işlemi geri al

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
    } // Tüm işlemleri sıfırla

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
        if(checking) {
            if (currentQ != totalNumberOfQs) {
                timerStopped = true;
                solveTime = timerInSeconds;
//                LayoutInflater factory = LayoutInflater.from(this);
//                final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
//                final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
//                TextView timerTV = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
//                timerTV.setText(formatTime(timerInSeconds));
////                totalSolveTime += timerInSeconds;
//                correctDialog.setView(leaveDialogView);

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

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setContentView(R.layout.activity_tournament);
                        findViewById(R.id.waitingDialogCL).setVisibility(View.VISIBLE);
//                sendScore(score);
                    }
                },300);

//                leaveDialogView.findViewById(R.id.correctDialogNext).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        try {
//                            mainFunc();
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        correctDialog.dismiss();
//                    }
//                });
//            leaveDialogView.findViewById(R.id.correctDialogGameMenu).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
//                    startActivity(intent);
//                    overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
//                    correctDialog.dismiss();
//                }
//            });
//                leaveDialogView.findViewById(R.id.correctDialogGameMenu).setVisibility(GONE);
//                correctDialog.show();
            } else {
                Log.i("socket-qnums","current: "+currentQ+" total: "+totalNumberOfQs);
//                solveTimeList.add(timerInSeconds-solveTimeList.get(1)-solveTimeList.get(0));
//                score = 10 + 30 + 60;
//                if(solveTimeList.get(0)<60) score += (int)(((60f-solveTimeList.get(0))/60f)*10f);
//                if(solveTimeList.get(1)<180) score += (int)(((180f-solveTimeList.get(1))/180f)*30f);
//                if(solveTimeList.get(2)<360) score += (int)(((360f-solveTimeList.get(2))/360f)*60f);
//                Log.i("solveTimeList",solveTimeList.toString());
//                Log.i("score",score+"");
                timerStopped=true;
                solveTime = timerInSeconds;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setContentView(R.layout.activity_tournament);
                        findViewById(R.id.waitingDialogCL).setVisibility(View.VISIBLE);
//                sendScore(score);
                    }
                },300);
            }
        }
    } // Çözümün doğruluğunu kontrol et

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
    } // Çekilen soruyu kullanıcıya göster

    public void timerFunc(){
        currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
        afterFiveMinMillis = currentTimeInMillis + 60000;//TODO 300000
        //noinspection deprecation
        timerHandler = new Handler();
        timerInSeconds = 0;
        final TextView timerTV = this.findViewById(R.id.timeTV_game);
        runnable = new Runnable() {
            @Override
            public void run() {
                timerInSeconds+=1;
                Log.i("timerInSeconds",((afterFiveMinMillis-Calendar.getInstance().getTimeInMillis())/1000)+"");
                timerTV.setText(formatTime((int) (afterFiveMinMillis-Calendar.getInstance().getTimeInMillis())/1000));
//                totalSolveTime = (int)(afterTenMinMillis-Calendar.getInstance().getTimeInMillis())/1000;
                if((afterFiveMinMillis-Calendar.getInstance().getTimeInMillis())>300){
                    timerStopped=false;
                }
                else{
                    timerStopped=true;
//                    if(currentQ==1){
//                        score=0;
//                    } else if(currentQ==2){
//                        score = 10;
//                        if(solveTimeList.get(0)<60) score += (int)(((60f-solveTimeList.get(0))/60f)*10f);
//                    } else if(currentQ==3){
//                        score = (int)(10f + (((60f-solveTimeList.get(0))/60f)*10f) + 30f + (((180f-solveTimeList.get(1))/180f)*30f));
//                        if(solveTimeList.get(0)<60) score += (int)(((60f-solveTimeList.get(0))/60f)*10f);
//                        if(solveTimeList.get(1)<180) score += (int)(((180f-solveTimeList.get(1))/180f)*30f);
//                    }
                    setContentView(R.layout.activity_tournament);
                    findViewById(R.id.waitingDialogCL).setVisibility(View.VISIBLE);
                    findViewById(R.id.participantsLayout).setVisibility(GONE);
                    findViewById(R.id.createOrJoinLL).setVisibility(GONE);
                    findViewById(R.id.tournamentOptionsCL).setVisibility(GONE);

                    int score=0;
                    Log.i("solveTime - timerInSec",solveTime + " - " + timerInSeconds);
                    if(solveTime<timerInSeconds-1){
                        score = 1000 - solveTime;
                    }


                    Log.i("socket-qnums","currentQ: "+currentQ + " score: "+ score);
                    sendScore(score);
                }
                if(!timerStopped){
                    timerHandler.postDelayed(this,1000);
                } else{
                    timerHandler.removeCallbacks(this);
                }

            }
        };
        timerHandler.post(runnable);

    } // Süreyi tut ve göster

    @SuppressLint("DefaultLocale")
    public static String formatTime(int secs) {
        return String.format("%02d:%02d", (secs % 3600) / 60, secs % 60);
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void mainFunc() throws JSONException {
        currentQ += 1;
        Log.i("gameOrder",gameOrder.toString());
        Log.i("JsonArray",allQs.getJSONArray(gameOrder.get(0))+"");
        gridSize = Integer.parseInt(gameOrder.get(0).split("\\.")[1]);
        Log.i("gridSize", gridSize+"");
        setContentView(gameDiffToLayoutID(gameOrder.get(0)));
        clearGrid();
        seperateGridAnswer(allQs.getJSONArray(gameOrder.get(0)).getJSONArray(0).getJSONArray(0).getJSONArray(0));
        allQs.getJSONArray(gameOrder.get(0)).remove(0);
        gameOrder.remove(0);
        timerFunc();

        TextView undoTV = findViewById(R.id.undoTV_ga);
        TextView resetTV = findViewById(R.id.resetTV_game);
        undoTV.setEnabled(true);
        resetTV.setEnabled(true);
        findViewById(R.id.clickView).setVisibility(GONE);
    }

    public void createNewTournament(View view){
        findViewById(R.id.createOrJoinLL).setVisibility(GONE);
        findViewById(R.id.tournamentOptionsCL).setVisibility(View.VISIBLE);
        tournamentProperties = new ArrayList<>();
        if(!userType.equals("instructor")){
            userType = "organizator";
        }

        LinearLayout gameSelectionLL = findViewById(R.id.gameSelectionLL);
        LinearLayout gameSelectionRow = (LinearLayout) gameSelectionLL.getChildAt(0);

        Spinner amountSpinner = gameSelectionRow.findViewById(R.id.amountSpinner);
        Spinner gameSpinner = gameSelectionRow.findViewById(R.id.gameSpinner);
        final Spinner diffSpinner = gameSelectionRow.findViewById(R.id.diffSpinner);
        ArrayAdapter<String> amountAdapter = new ArrayAdapter<>(this, R.layout.spinner_tv, new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5")));
        amountSpinner.setAdapter(amountAdapter);
        ArrayAdapter<String> gameAdapter = new ArrayAdapter<>(this, R.layout.spinner_tv, new ArrayList<>(Arrays.asList("Sudoku 6x6", "Sudoku 9x9", "Hazine Avı", "Patika", "Sayı Bulmaca", "Sözcük Turu", "Piramit")));
        gameSpinner.setAdapter(gameAdapter);
        final ArrayAdapter<String> diffAdapter = new ArrayAdapter<>(this, R.layout.spinner_tv,
                new ArrayList<>(Arrays.asList(getString(R.string.Easy),getString(R.string.Medium),getString(R.string.Hard))));
        diffSpinner.setAdapter(diffAdapter);

        gameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String currentGame = adapterView.getItemAtPosition(i).toString();
                Log.i("currentgame",currentGame);
                if(currentGame.equals("Piramit") || currentGame.equals("Sözcük Turu")){
                    ArrayAdapter<String> changedDiffAdapter = new ArrayAdapter<>(TournamentActivity.this, R.layout.spinner_tv,
                            new ArrayList<>(Arrays.asList(getString(R.string.Easy), getString(R.string.Medium), getString(R.string.Hard), getString(R.string.VeryHard))));
                    diffSpinner.setAdapter(changedDiffAdapter);
                } else {
                    diffSpinner.setAdapter(diffAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void addNewGameSelection(View view){
        LinearLayout gameSelectionLL = findViewById(R.id.gameSelectionLL);
        LinearLayout gameSelectionRow = (LinearLayout) getLayoutInflater().inflate(this.getResources().getIdentifier("game_selection_row_ll", "layout", this.getPackageName()),null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0,0,(int) (7 * getResources().getDisplayMetrics().density));
        gameSelectionRow.setLayoutParams(params);
        gameSelectionLL.addView(gameSelectionRow);
        if(gameSelectionLL.getChildCount() >= 4){
            findViewById(R.id.addbutton).setVisibility(GONE);
        }

        Spinner amountSpinner = gameSelectionRow.findViewById(R.id.amountSpinner);
        Spinner gameSpinner = gameSelectionRow.findViewById(R.id.gameSpinner);
        final Spinner diffSpinner = gameSelectionRow.findViewById(R.id.diffSpinner);
        ArrayAdapter<String> amountAdapter = new ArrayAdapter<>(this, R.layout.spinner_tv, new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5")));
        amountSpinner.setAdapter(amountAdapter);
        ArrayAdapter<String> gameAdapter = new ArrayAdapter<>(this, R.layout.spinner_tv, new ArrayList<>(Arrays.asList("Sudoku 6x6", "Sudoku 9x9", "Hazine Avı", "Patika", "Sayı Bulmaca", "Sözcük Turu", "Piramit")));
        gameSpinner.setAdapter(gameAdapter);
        final ArrayAdapter<String> diffAdapter = new ArrayAdapter<>(this, R.layout.spinner_tv,
                new ArrayList<>(Arrays.asList(getString(R.string.Easy),getString(R.string.Medium),getString(R.string.Hard))));
        diffSpinner.setAdapter(diffAdapter);

        gameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String currentGame = adapterView.getItemAtPosition(i).toString();
                Log.i("currentgame",currentGame);
                if(currentGame.equals("Piramit") || currentGame.equals("Sözcük Turu")){
                    ArrayAdapter<String> changedDiffAdapter = new ArrayAdapter<>(TournamentActivity.this, R.layout.spinner_tv,
                            new ArrayList<>(Arrays.asList(getString(R.string.Easy), getString(R.string.Medium), getString(R.string.Hard), getString(R.string.VeryHard))));
                    diffSpinner.setAdapter(changedDiffAdapter);
                } else {
                    diffSpinner.setAdapter(diffAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void createTheTournament(View view){
        LinearLayout gameSelectionLL = findViewById(R.id.gameSelectionLL);
        for(int i = 0; i<gameSelectionLL.getChildCount(); i++){
            List<String> tournamentProperty = new ArrayList<>();
            LinearLayout gameSelectionRow = (LinearLayout) gameSelectionLL.getChildAt(i);
            tournamentProperty.add(shownToDatabase("shownToDatabase",((Spinner)gameSelectionRow.findViewById(R.id.gameSpinner)).getSelectedItem() +" "+ ((Spinner)gameSelectionRow.findViewById(R.id.diffSpinner)).getSelectedItem()));
            tournamentProperty.add((String) ((Spinner)gameSelectionRow.findViewById(R.id.amountSpinner)).getSelectedItem());

            totalNumberOfQs += Integer.parseInt(tournamentProperty.get(1));
            if(gamesMap.get(tournamentProperty.get(0))==null) {
                gamesMap.put(tournamentProperty.get(0), Integer.parseInt(tournamentProperty.get(1)));
            } else {
                gamesMap.put(tournamentProperty.get(0), gamesMap.get(tournamentProperty.get(0)) + Integer.parseInt(tournamentProperty.get(1)));
            }
        }
        for(int i = 0; i<gamesMap.size(); i++){
            String gameName = (String)gamesMap.keySet().toArray()[i];
            for(int j = 0; j<gamesMap.get(gameName); j++){
                gameOrder.add(gameName);
            }
        }
        Log.i("gamesMap",gamesMap.toString());
        findViewById(R.id.tournamentOptionsCL).setVisibility(GONE);
        findViewById(R.id.waitingDialogCL).setVisibility(VISIBLE);
        connectSocket();
        createRoom();
    }

    public void startTheTournament(View view){
        findViewById(R.id.tournamentOptionsCL).setVisibility(GONE);
        findViewById(R.id.waitingDialogCL).setVisibility(VISIBLE);
        start();

//        setContentView(gameDiffToLayoutID((String) gamesMap.keySet().toArray()[0]));
    }

    public void joinToTournament(View view){
        if(code.equals("0")) {
            EditText codeET = findViewById(R.id.tournCodeEditText);
            code = codeET.getText().toString();
            userType = "participant";
        }

        connectSocket();
        joinToRoom(code);
        findViewById(R.id.createOrJoinLL).setVisibility(GONE);
        findViewById(R.id.waitingDialogCL).setVisibility(VISIBLE);
    }

    public void changeParticipantsInRT(){
        LayoutInflater factory = getLayoutInflater();
        ((LinearLayout)findViewById(R.id.participantsLayout).findViewById(R.id.prtpLL)).removeAllViews();
//        ConstraintLayout organizatorRow= (ConstraintLayout) factory.inflate(this.getResources().getIdentifier("participant_row", "layout", this.getPackageName()),null);
//        ((ImageView)organizatorRow.findViewById(R.id.avatarS1)).setImageResource(R.drawable.ic_teacher_usericon);
//        ((TextView)organizatorRow.findViewById(R.id.usernameTV)).setText(username);
//        ((LinearLayout)participantsLayout.findViewById(R.id.prtpLL)).addView(organizatorRow);
        for(int i = 0; i < playersMap.size(); i++){
            ConstraintLayout participantRow= (ConstraintLayout) factory.inflate(this.getResources().getIdentifier("participant_row", "layout", this.getPackageName()),null);
            ((TextView)participantRow.findViewById(R.id.usernameTV)).setText((String) playersMap.keySet().toArray()[i]);
//            if(type.contains("nstructor")){
//                participantRow.findViewById(R.id.permissionButton).setVisibility(View.VISIBLE);
//                if((Boolean) playersMap.values().toArray()[i]) {
//                    ((ImageView)participantRow.findViewById(R.id.permissionButton)).setImageResource(R.drawable.ic_delete_icon);
//                } else {
//                    ((ImageView)participantRow.findViewById(R.id.permissionButton)).setImageResource(R.drawable.ic_key_icon);
//                }
//            }
            ((LinearLayout)findViewById(R.id.participantsLayout).findViewById(R.id.prtpLL)).addView(participantRow);
        }
    }



    private Socket socket;
    {
        try {
            socket = IO.socket("https://plato-tournament.herokuapp.com");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void connectSocket(){
        socket.connect();
        Log.i("socket","connectSocket");
        socket.on("roomCreated", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        if(!userType.equals("instructor")) {
                            code = Integer.toString((int)args[0]);
                            joinToRoom(code);
                        }
                        findViewById(R.id.participantsLayout).setVisibility(VISIBLE);
                        findViewById(R.id.waitingDialogCL).setVisibility(GONE);
                        ((TextView)findViewById(R.id.participantsLayout).findViewById(R.id.codeTV_to)).setText(getString(R.string.Code)+": "+code);
                        Log.i("codeTV-text", getString(R.string.Code)+": "+code);
                        findViewById(R.id.participantsLayout).findViewById(R.id.startTournamentButton).setVisibility(VISIBLE);
                        changeParticipantsInRT();
                        Log.i("socket-roomCreated",args[0]+".");
                    }
                });
            }
        });
        socket.on("roomNotFound", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("socket-roomNotFound",".");
                        String msg = getString(R.string.InvalidCode);
                        if(userType.equals("instructor") || userType.equals("student")) msg = getString(R.string.TournamentHasntStartedYet);
                        Toast.makeText(TournamentActivity.this, msg, Toast.LENGTH_SHORT).show();
                        disconnectSocket();
                        Intent intent = new Intent(getApplicationContext(), GameTypesActivity.class);
                        if(userType.equals("instructor") || userType.equals("student")){
                            intent = new Intent(getApplicationContext(), MyClassActivity.class);
                        }
                        startActivity(intent);
                        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                    }
                });
            }
        });
        socket.on("players", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject room = (JSONObject) args[0];
                            if(organizatorName == null){
                                organizatorName = room.getJSONObject("organizator").getString("username");
                            }
                            JSONArray players = room.getJSONArray("participants");
                            List<String> plNameList = new ArrayList<>();
                            for(int i = 0; i<players.length(); i++){
                                String plName = players.getJSONObject(i).getString("username");
                                plNameList.add(plName);
                                if(playersMap.get(plName)==null){
                                    playersMap.put(plName,0);
                                }
                            }
                            for(String s : playersMap.keySet()){
                                if(!plNameList.contains(s)){
                                    playersMap.remove(s);
                                }
                            }

                            findViewById(R.id.waitingDialogCL).setVisibility(GONE);
                            findViewById(R.id.participantsLayout).setVisibility(VISIBLE);
                            if(userType.equals("participant")||userType.equals("student")){
                                findViewById(R.id.participantsLayout).findViewById(R.id.startTournamentButton).setVisibility(GONE);
                            }
//                            if(participantsLayout.getVisibility() == VISIBLE){
                            changeParticipantsInRT();
//                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.i("playersMap",playersMap.toString());
                        Log.i("socket-players",args[0]+ ".");



                    }
                });
            }
        });
        socket.on("gameStarted", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.i("socket-gameStarted",".");
                    }
                });
            }
        });
        socket.on("start", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        Log.i("socket-start","Qs: "+args[0]);
                        try {
                            JSONObject obj = new JSONObject((String)args[0]);
                            allQs = obj.getJSONObject("Info");
                            if(userType.equals("participant") || userType.equals("student")) {
                                for (Iterator<String> it = allQs.keys(); it.hasNext(); ) {
                                    String key = it.next();
                                    gamesMap.put(key, ((JSONArray) allQs.get(key)).length());
                                }
                                Log.i("gamesMap",gamesMap.toString());
                                for(int i = 0; i<gamesMap.size(); i++){
                                    String gameName = (String)gamesMap.keySet().toArray()[i];
                                    for(int j = 0; j<gamesMap.get(gameName); j++){
                                        gameOrder.add(gameName);
                                    }
                                }
                            }
//                            currentQ += 1;
//                            setContentView(gameDiffToLayoutID(gameOrder.get(0)));
//                            seperateGridAnswer(allQs.getJSONArray(gameOrder.get(0)).getJSONArray(0).getJSONArray(0).getJSONArray(0));
//                            Log.i("gameOrder",gameOrder.toString());
//                            Log.i("JsonArray",allQs.getJSONArray(gameOrder.get(0))+"");
//                            gameOrder.remove(0);
//                            allQs.getJSONArray(gameOrder.get(0)).remove(0);
//                            timerFunc();
                            mainFunc();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        socket.on("endOfRound", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        Log.i("socket-endOfRound", args[0]+"");
                        try {
//                            JSONArray players = (JSONArray) args[0];
//                            for (int i = 0; i < players.length(); i++) {
//                                JSONObject player = players.getJSONObject(i);
//                                String plName = player.getString("username");
//                                int score = player.getInt("score");
//                                playersMap.put(plName, score);
////                                findViewById(R.id.opponentNamesLL).findViewWithTag("opp"+(i+1));
//                            }
//                            gotScores=true;
                            JSONArray players = (JSONArray) args[0];
                            for(int i = 0; i<players.length(); i++) {
                                JSONObject player = players.getJSONObject(i);
                                String plName = player.getString("username");
                                int plScore = players.getJSONObject(i).getInt("score");
                                LinearLayout horLL = (LinearLayout) getLayoutInflater().inflate(getResources().getIdentifier("scores_hor_ll","layout", getPackageName()),null);
                                ((TextView) horLL.getChildAt(1)).setText(plName); // kullanıcı adı
                                ((TextView) horLL.getChildAt(2)).setText(Integer.toString(plScore));
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                params.setMargins(0,0,0,(int) (10 * getResources().getDisplayMetrics().density));
                                horLL.setLayoutParams(params);
                                ((LinearLayout)findViewById(R.id.scoresLL)).addView(horLL,2+i);
                            }
                            findViewById(R.id.waitingDialogCL).setVisibility(GONE);
                            findViewById(R.id.scoresLL).setVisibility(View.VISIBLE);

                            if(gameOrder.size()==0){
                                findViewById(R.id.correctDialogGameMenu).setVisibility(VISIBLE);
                            } else {
                                new Handler().postDelayed(new Runnable() {
                                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                    @Override
                                    public void run() {
                                        try {
                                            timerStopped = true;
                                            timerHandler.removeCallbacks(runnable);
                                            mainFunc();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
//                sendScore(score);
                                    }
                                },5000);
                            }
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void disconnectSocket(){
        socket.disconnect();
    }

    public void createRoom(){
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("gameTypes", gamesMap);
        if(userType.equals("instructor")) map.put("code", code);

        socket.emit("createRoom", new JSONObject(map));
        Log.i("socket","createRoom - "+map);
    }

    public void joinToRoom(String room_id){
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("room_id", room_id);

        socket.emit("joinToRoom", new JSONObject(map));
        Log.i("socket","joinToRoom - "+map);
    }

    public void start(){
        Log.i("socket","start");
        socket.emit("start");
    }

    public void sendScore(int score){
        Log.i("socket","sendScore");
        socket.emit("sendScore",score);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                endOfRound();
            }
        },3000);
    }

    public void endOfRound(){
        Log.i("socket","endOfRound");
        socket.emit("endOfRound");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament);
        waitingDialog = findViewById(R.id.waitingDialogCL);
        createOrJoinLL = findViewById(R.id.createOrJoinLL);
        tournamentOptionsCL = findViewById(R.id.tournamentOptionsCL);
        participantsLayout = findViewById(R.id.participantsLayout);
        scoresLL = findViewById(R.id.scoresLL);

        waitingDialog.setVisibility(GONE);
        createOrJoinLL.setVisibility(GONE);
        tournamentOptionsCL.setVisibility(GONE);
        participantsLayout.setVisibility(GONE);
        scoresLL.setVisibility(GONE);

        SharedPreferences sp = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        username = sp.getString("username",getString(R.string.Unknown));

        Intent intent = getIntent();
        userType = intent.getStringExtra("userType");
        if(userType==null){
            userType="participant";
        }
        code = intent.getStringExtra("code");
        if(code==null){
            code="0";
        }
        Log.i("intent things", "userType: "+userType+" code: "+code);

        if(userType.equals("student")){
            joinToTournament(null);
        } else if(userType.equals("instructor")){
            createNewTournament(null);
        } else {
            createOrJoinLL.setVisibility(VISIBLE);
        }



    }

    @Override
    public void onBackPressed() {
        wannaLeaveDialog(null);
    }

    @Override
    protected void onDestroy() {
        disconnectSocket();
        super.onDestroy();
    }
}