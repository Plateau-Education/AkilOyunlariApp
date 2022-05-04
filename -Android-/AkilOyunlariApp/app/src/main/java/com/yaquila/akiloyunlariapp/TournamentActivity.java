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

import com.yaquila.akiloyunlariapp.gameutils.HazineAviUtils;
import com.yaquila.akiloyunlariapp.gameutils.PatikaUtils;
import com.yaquila.akiloyunlariapp.gameutils.PiramitUtils;
import com.yaquila.akiloyunlariapp.gameutils.SayiBulmacaUtils;
import com.yaquila.akiloyunlariapp.gameutils.SozcukTuruUtils;
import com.yaquila.akiloyunlariapp.gameutils.SudokuUtils;

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

    AppCompatActivity mAppCompatActivity;
    String username;
    String code = "0";
    String userType = "participant";
    String organizatorName;
    String currentGameName;
    int currentQ = 0;
    int totalNumberOfQs = 0;
    int currentTotalScore = 0;
    int solveTime = 360;
    int secondsToGo = 60;
    long currentTimeInMillis;
    long afterFiveMinMillis;
    boolean solvedTheQuestion = false;
    boolean scoresShown = false;

    Map<String, Integer> gamesMap = new HashMap<>();
    Map<String, Integer> playersMap = new HashMap<>();
    Map<String, Class<?>> utilsMap = new HashMap<>();
    List<List<String>> tournamentProperties = new ArrayList<>();
    List<String> gameOrder = new ArrayList<>();
    JSONObject allQs = new JSONObject();

    int timerInSeconds = 0;
    boolean timerStopped=false;
    Handler timerHandler;
    Runnable runnable;

    public String shownToDatabase(String visibleOrDatabase, String string){
        Map<String,String> visibleToDB = new HashMap<>();
        List<String> shownGameNames = new ArrayList<>(Arrays.asList(
                getString(R.string.Sudoku) + " 6x6 "+getString(R.string.Easy), getString(R.string.Sudoku) + " 6x6 "+getString(R.string.Medium), getString(R.string.Sudoku) + " 6x6 "+getString(R.string.Hard),
                getString(R.string.Sudoku) + " 9x9 "+getString(R.string.Easy), getString(R.string.Sudoku) + " 9x9 "+getString(R.string.Medium), getString(R.string.Sudoku) + " 9x9 "+getString(R.string.Hard),
                getString(R.string.HazineAvı)+" "+getString(R.string.Easy), getString(R.string.HazineAvı)+" "+getString(R.string.Medium), getString(R.string.HazineAvı)+" "+getString(R.string.Hard),
                getString(R.string.Patika)+" "+getString(R.string.Easy), getString(R.string.Patika)+" "+getString(R.string.Medium), getString(R.string.Patika)+" "+getString(R.string.Hard),
                getString(R.string.SayıBulmaca)+" "+getString(R.string.Easy), getString(R.string.SayıBulmaca)+" "+getString(R.string.Medium), getString(R.string.SayıBulmaca)+" "+getString(R.string.Hard),
                getString(R.string.SözcükTuru)+" "+getString(R.string.Easy), getString(R.string.SözcükTuru)+" "+getString(R.string.Medium), getString(R.string.SözcükTuru)+" "+getString(R.string.Hard), getString(R.string.SözcükTuru)+" "+getString(R.string.VeryHard),
                getString(R.string.Piramit)+" "+getString(R.string.Easy), getString(R.string.Piramit)+" "+getString(R.string.Medium), getString(R.string.Piramit)+" "+getString(R.string.Hard), getString(R.string.Piramit)+" "+getString(R.string.VeryHard)));
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

    public void goToGameTypes(View view){
        disconnectSocket();
        Intent intent = new Intent(getApplicationContext(), GameTypesActivity.class);
        if(userType.equals("instructor") || userType.equals("student"))
            intent = new Intent(getApplicationContext(), MyClassActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void changeClicked(View view) {
        TextView box = (TextView) view;
        String answerIndex = box.getTag().toString();
        try {
            utilsMap.get(currentGameName).getDeclaredMethod("changeClicked", View.class).invoke(null,view);
            if(!utilsMap.get(currentGameName).getDeclaredField("clueIndexes").get(null).toString().contains(answerIndex)) checkAnswer(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // Tıklanan kutuya elmas/çarpı koy

    public void changeSwitch(View view) {
        HazineAviUtils.changeSwitch(view);
    } // Elmas - çarpı değiştir

    public void undoOperation(View view) {
        try {
            utilsMap.get(currentGameName).getDeclaredMethod("undoOperation", null).invoke(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // Son işlemi geri al

    public void resetGrid(View view) {
        try {
            utilsMap.get(currentGameName).getDeclaredMethod("resetGrid", View.class).invoke(null,view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // Tüm işlemleri sıfırla


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkAnswer(View view) throws NoSuchFieldException, IllegalAccessException {
        GridLayout gridLayout = findViewById(R.id.gridGL_grid);
        boolean checking = false;
        try {
            checking = (boolean) utilsMap.get(currentGameName).getDeclaredMethod("checkAnswer", null).invoke(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(checking) {
            solvedTheQuestion = true;
            if (currentQ != totalNumberOfQs) {
                timerStopped = true;
                solveTime = (int) (secondsToGo - ((afterFiveMinMillis-Calendar.getInstance().getTimeInMillis())/1000));
                Log.i("solveTime - notTheLastQ", solveTime+"");
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
                for (int i = 0; i < Integer.parseInt(utilsMap.get(currentGameName).getDeclaredField("gridSize").get(null).toString()); i++) {
                    for (int j = 0; j < Integer.parseInt(utilsMap.get(currentGameName).getDeclaredField("gridSize").get(null).toString()); j++) {
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

            } else {
                Log.i("socket-qnums","current: "+currentQ+" total: "+totalNumberOfQs);
                timerStopped=true;
                solveTime = (int) (secondsToGo - ((afterFiveMinMillis-Calendar.getInstance().getTimeInMillis())/1000));
                Log.i("solveTime - theLastQ", solveTime+"");
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

    public void seperateGridAnswer(JSONArray grid){
        try {
            utilsMap.get(currentGameName).getDeclaredMethod("seperateGridAnswer", JSONArray.class).invoke(null,grid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // Çekilen soruyu kullanıcıya göster

    public void timerFunc(final int secondsToGo){
        currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
        afterFiveMinMillis = currentTimeInMillis + secondsToGo*1000;
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
                if((afterFiveMinMillis-Calendar.getInstance().getTimeInMillis())>300){
                    timerStopped=false;
                }
                else{
                    timerStopped=true;
                    setContentView(R.layout.activity_tournament);
                    ((TextView)findViewById(R.id.waitingDialogCL).findViewById(R.id.loadingTextView2)).setText(R.string.WaitingForScores);
                    findViewById(R.id.waitingDialogCL).setVisibility(View.VISIBLE);
                    findViewById(R.id.participantsLayout).setVisibility(GONE);
                    findViewById(R.id.createOrJoinLL).setVisibility(GONE);
                    findViewById(R.id.tournamentOptionsCL).setVisibility(GONE);

                    int score=0;
                    Log.i("solveTime - timerInSec",solveTime + " - " + timerInSeconds);
                    if(solvedTheQuestion){
                        score = 1000 - solveTime * (600/secondsToGo);
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
        try {
            utilsMap.get(currentGameName).getDeclaredMethod("clearGrid", null).invoke(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        timerInSeconds = 0;
        timerStopped=true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void mainFunc() throws JSONException {
        try {
            utilsMap.get(currentGameName).getDeclaredMethod("initVars", AppCompatActivity.class).invoke(null,mAppCompatActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentQ += 1;
        Log.i("gameOrder",gameOrder.toString());
        Log.i("JsonArray",allQs.getJSONArray(gameOrder.get(0))+"");
        if(gameOrder.get(0).contains("HazineAvi")){
            HazineAviUtils.gridSize = Integer.parseInt(gameOrder.get(0).split("\\.")[1]);
            Log.i("gridSize",HazineAviUtils.gridSize+"");
            if(HazineAviUtils.gridSize == 5) secondsToGo = 60;
            else if(HazineAviUtils.gridSize == 8) secondsToGo = 180;
            else if(HazineAviUtils.gridSize == 10) secondsToGo = 360;
        }
        setContentView(gameDiffToLayoutID(gameOrder.get(0)));
        clearGrid();
        solvedTheQuestion=false;
        seperateGridAnswer(allQs.getJSONArray(gameOrder.get(0)).getJSONArray(0).getJSONArray(0).getJSONArray(0));
        allQs.getJSONArray(gameOrder.get(0)).remove(0);
        gameOrder.remove(0);
        timerFunc(secondsToGo);

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
        ArrayAdapter<String> gameAdapter = new ArrayAdapter<>(this, R.layout.spinner_tv, new ArrayList<>(Arrays.asList(getString(R.string.HazineAvı))));
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
        ArrayAdapter<String> gameAdapter = new ArrayAdapter<>(this, R.layout.spinner_tv, new ArrayList<>(Arrays.asList(getString(R.string.HazineAvı))));
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
        String[] s = shownToDatabase("databaseToShown",gameOrder.get(0)).split(" ");
        currentGameName = s[0] + " " + s[1];
        try {
            utilsMap.get(currentGameName).getDeclaredMethod("initVars", AppCompatActivity.class).invoke(null,mAppCompatActivity);
        } catch (Exception e) {
            e.printStackTrace();
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
        try {
            ((TextView) findViewById(R.id.waitingDialogCL).findViewById(R.id.loadingTextView2)).setText(R.string.loading);
        } catch(Exception e){
            e.printStackTrace();
        }
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
        try {
            ((TextView) findViewById(R.id.waitingDialogCL).findViewById(R.id.loadingTextView2)).setText(R.string.loading);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void  changeParticipantsInRT(){
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
            socket = IO.socket("https://plato-all-in-one.herokuapp.com");
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
                        if(!scoresShown) findViewById(R.id.participantsLayout).setVisibility(VISIBLE);
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
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        try {
                            JSONObject room = (JSONObject) args[0];
                            if(organizatorName == null){
                                organizatorName = room.getJSONObject("organizator").getString("username");
                                ((TextView)findViewById(R.id.participantsLayout).findViewById(R.id.codeTV_to)).setText(getString(R.string.Code)+": "+code);
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
                            if(!scoresShown) findViewById(R.id.participantsLayout).setVisibility(VISIBLE);
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
                                String[] s = shownToDatabase("databaseToShown",gameOrder.get(0)).split(" ");
                                currentGameName = s[0] + " " + s[1];
                                try {
                                    utilsMap.get(currentGameName).getDeclaredMethod("initVars", AppCompatActivity.class).invoke(null,mAppCompatActivity);
                                } catch (Exception e) {
                                    e.printStackTrace();
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
                                ((TextView) horLL.getChildAt(0)).setText((i+1)+""); // sıralama
                                ((TextView) horLL.getChildAt(1)).setText(plName); // kullanıcı adı
                                ((TextView) horLL.getChildAt(2)).setText(Integer.toString(plScore));
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                params.setMargins(0,0,0,(int) (10 * getResources().getDisplayMetrics().density));
                                horLL.setLayoutParams(params);
                                ((LinearLayout)findViewById(R.id.scoresLL)).addView(horLL,2+i);
                            }
                            findViewById(R.id.waitingDialogCL).setVisibility(GONE);
                            findViewById(R.id.scoresLL).setVisibility(View.VISIBLE);
                            scoresShown = true;


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

        socket.emit("usageType","tournament");
        socket.emit("createRoom", new JSONObject(map));
        Log.i("socket","createRoom - "+map);
    }

    public void joinToRoom(String room_id){
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("room_id", room_id);

        socket.emit("tournament_joinToRoom", new JSONObject(map));
        Log.i("socket","joinToRoom - "+map);
    }

    public void start(){
        Log.i("socket","start");
        socket.emit("start");
    }

    public void sendScore(int score){
        Log.i("socket","sendScore");
        socket.emit("tournament_sendScore",score);
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
        mAppCompatActivity = this;

        utilsMap = new HashMap<>();
        utilsMap.put(getString(R.string.Sudoku), SudokuUtils.class);
        utilsMap.put(getString(R.string.HazineAvı), HazineAviUtils.class);
        utilsMap.put(getString(R.string.Patika), PatikaUtils.class);
        utilsMap.put(getString(R.string.SayıBulmaca), SayiBulmacaUtils.class);
        utilsMap.put(getString(R.string.SözcükTuru), SozcukTuruUtils.class);
        utilsMap.put(getString(R.string.Piramit), PiramitUtils.class);

        findViewById(R.id.waitingDialogCL).setVisibility(GONE);
        findViewById(R.id.createOrJoinLL).setVisibility(GONE);
        findViewById(R.id.tournamentOptionsCL).setVisibility(GONE);
        findViewById(R.id.participantsLayout).setVisibility(GONE);
        findViewById(R.id.scoresLL).setVisibility(GONE);

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
            if(!scoresShown)findViewById(R.id.createOrJoinLL).setVisibility(VISIBLE);
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