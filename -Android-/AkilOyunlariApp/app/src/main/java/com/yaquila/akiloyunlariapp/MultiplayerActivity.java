package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yaquila.akiloyunlariapp.gameutils.HazineAviUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.view.View.GONE;

public class MultiplayerActivity extends AppCompatActivity {

    String gameName;
    String pType;
    String username;
    String cluster;
    String game;
    String dbGameName;
    String socketId;
    int numberOfQ = 3;
    int currentQ = 0;
    int totalSolveTime;
    int score = 0;
    int secondsToGo = 60;
    int timerInSeconds = 0;
    long currentTimeInMillis;
    long afterTenMinMillis;
    long afterSecondsToGoMinMillis;
    boolean inTheRoom = false;
    boolean gotScores;
    boolean timerStopped = false;

    List<String> playerNames = new ArrayList<>();
    List<Integer> solveTimeList = new ArrayList<>();
    JSONArray allQs = new JSONArray();
    TextView timerTV;
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
//                SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
//                try {
//                    ArrayList<String> questions = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("HazineAvi."+gridSize, ObjectSerializer.serialize(new ArrayList<String>())));
//                    ArrayList<Integer> gameIds = (ArrayList<Integer>) ObjectSerializer.deserialize(sharedPreferences.getString("IDHazineAvi."+gridSize, ObjectSerializer.serialize(new ArrayList<Integer>())));
//                    Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));
//
//                    assert questions != null;
//                    questions.remove(0);
//
//                    assert solvedQuestions != null;
//                    assert gameIds != null;
//                    Objects.requireNonNull(solvedQuestions.get("HazineAvi." + gridSize)).add(gameIds.remove(0)+"-"+"0");
//
//                    Log.i("solvedQuestions",solvedQuestions+"");
//
//                    sharedPreferences.edit().putString("HazineAvi."+gridSize, ObjectSerializer.serialize(questions)).apply();
//                    sharedPreferences.edit().putString("IDHazineAvi."+gridSize, ObjectSerializer.serialize(gameIds)).apply();
//                    sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize((Serializable) solvedQuestions)).apply();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                disconnectSocket();
                timerStopped=true;
                inTheRoom=true;
                gotScores=true;
                Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
                intent.putExtra("type","multi"+pType);
                startActivity(intent);
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
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
    } // Ana Menüye dönmek istiyor musun?

    public void nextQuestion(View view){
        if(timerStopped){
            LayoutInflater factory = LayoutInflater.from(this);
            final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
            final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
            TextView timerTV = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
            timerTV.setVisibility(GONE);
            correctDialog.setView(leaveDialogView);

            leaveDialogView.findViewById(R.id.correctDialogNext).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mainFunc();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    correctDialog.dismiss();
                }
            });
//            leaveDialogView.findViewById(R.id.correctDialogGameMenu).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
//                    startActivity(intent);
//                    overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
//                    correctDialog.dismiss();
//                }
//            });
            leaveDialogView.findViewById(R.id.correctDialogGameMenu).setVisibility(GONE);
            correctDialog.show();
        }
    } // Sonraki soruya geç

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void changeClicked(View view){
        switch (gameName){
            case "Hazine Avı":
                TextView box = (TextView) view;
                String answerIndex = box.getTag().toString();
                HazineAviUtils.changeClicked(view);
                if(!HazineAviUtils.clueIndexes.contains(answerIndex)) checkAnswer(null);
                break;
            case "Patika":
                break;
            default:
                break;
        }
    } // Tıklanan kutuya elmas/çarpı koy

    public void changeSwitch(View view){
        HazineAviUtils.changeSwitch(view);
    } // Elmas - çarpı değiştir

    public void undoOperation(View view){
        switch (gameName){
            case "Hazine Avı":
                HazineAviUtils.undoOperation();
                break;
            case "Patika":
                break;
            default:
                break;
        }
    } // Son işlemi geri al

    public void resetGrid(View view){
        switch (gameName){
            case "Hazine Avı":
                HazineAviUtils.resetGrid(view);
                break;
            case "Patika":
                break;
            default:
                break;
        }
    } // Tüm işlemleri sıfırla

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkAnswer(View view){
        GridLayout gridLayout = findViewById(R.id.gridGL_ga);
        switch (gameName){
            case "Hazine Avı":
                if(HazineAviUtils.checkAnswer(gridLayout)) {
                    if (currentQ != numberOfQ) {
                        timerStopped = true;
                        LayoutInflater factory = LayoutInflater.from(this);
                        final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
                        final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
                        TextView timerTV = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
                        timerTV.setText(formatTime((int) (secondsToGo - ((afterSecondsToGoMinMillis-Calendar.getInstance().getTimeInMillis())/1000))));
//                totalSolveTime += timerInSeconds;
                        correctDialog.setView(leaveDialogView);

                        findViewById(R.id.clickView).setVisibility(View.VISIBLE);
                        TextView undoTV = findViewById(R.id.undoTV_ga);
                        TextView resetTV = findViewById(R.id.resetTV_game);
                        for (int i = 0; i < HazineAviUtils.gridSize; i++) {
                            for (int j = 0; j < HazineAviUtils.gridSize; j++) {
                                gridLayout.findViewWithTag(Integer.toString(j) + i).setEnabled(false);
                            }
                        }
                        undoTV.setEnabled(false);
                        resetTV.setEnabled(false);

                        solveTimeList.add((int) (secondsToGo - ((afterSecondsToGoMinMillis-Calendar.getInstance().getTimeInMillis())/1000)));

                        leaveDialogView.findViewById(R.id.correctDialogNext).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    mainFunc();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                correctDialog.dismiss();
                            }
                        });
                        leaveDialogView.findViewById(R.id.correctDialogGameMenu).setVisibility(GONE);
                        correctDialog.show();
                    } else {
                        currentQ++;
                        Log.i("socket-qnums","current: "+currentQ+" total: "+numberOfQ+" timerInSeconds: "+timerInSeconds);
                        solveTimeList.add((int) (secondsToGo - ((afterSecondsToGoMinMillis-Calendar.getInstance().getTimeInMillis())/1000)));
                        score = 10 + 30 + 60;
                        if(solveTimeList.get(0)<60) score += (int)(((60f-solveTimeList.get(0))/60f)*10f);
                        if(solveTimeList.get(1)<180) score += (int)(((180f-solveTimeList.get(1))/180f)*30f);
                        if(solveTimeList.get(2)<360) score += (int)(((360f-solveTimeList.get(2))/360f)*60f);
                        Log.i("solveTimeList",solveTimeList.toString());
                        Log.i("score",score+"");
                        timerStopped=true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setContentView(R.layout.activity_multiplayer);
                                ((TextView)findViewById(R.id.waitingDialogCL).findViewById(R.id.loadingTextView2)).setText(R.string.WaitingForScores);
                                findViewById(R.id.waitingDialogCL).setVisibility(View.VISIBLE);

//                sendScore(score);
                            }
                        },500);
                    }
                }
                break;
            case "Patika":
                break;
            default:
                break;
        }
    } // Çözümün doğruluğunu kontrol et

    public void seperateGridAnswer(JSONArray grid) throws JSONException {
        switch (gameName){
            case "Hazine Avı":
                HazineAviUtils.seperateGridAnswer(grid);
                break;
            case "Patika":
                break;
            default:
                break;
        }
    } // Çekilen soruyu kullanıcıya göster

    public void timerFunc(){
        currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
        afterTenMinMillis = currentTimeInMillis + 600000;

        //noinspection deprecation
        timerHandler = new Handler();
        timerTV = findViewById(R.id.timeTV_game);
        runnable = new Runnable() {
            @Override
            public void run() {
                timerInSeconds = 1;
                Log.i("countDown",((afterTenMinMillis-Calendar.getInstance().getTimeInMillis())/1000)+"");
                timerTV.setText(formatTime((int) (afterTenMinMillis-Calendar.getInstance().getTimeInMillis())/1000));
//                totalSolveTime = (int)(afterTenMinMillis-Calendar.getInstance().getTimeInMillis())/1000;
                if((afterTenMinMillis-Calendar.getInstance().getTimeInMillis())>300){
                    timerStopped=false;
                }
                else{
                    timerStopped=true;
                    if(currentQ==1){
                        score=0;
                    } else if(currentQ==2){
                        score = 10;
                        if(solveTimeList.get(0)<60) score += (int)(((60f-solveTimeList.get(0))/60f)*10f);
                    } else if(currentQ==3){
                        score = (int)(10f + (((60f-solveTimeList.get(0))/60f)*10f) + 30f + (((180f-solveTimeList.get(1))/180f)*30f));
                        if(solveTimeList.get(0)<60) score += (int)(((60f-solveTimeList.get(0))/60f)*10f);
                        if(solveTimeList.get(1)<180) score += (int)(((180f-solveTimeList.get(1))/180f)*30f);
                    }
                    setContentView(R.layout.activity_multiplayer);
                    findViewById(R.id.waitingDialogCL).setVisibility(View.VISIBLE);

                    Log.i("socket-qnums","current: "+currentQ+" total: "+numberOfQ +" time: "+totalSolveTime);
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
        switch (gameName){
            case "Hazine Avı":
                HazineAviUtils.clearGrid();
                break;
            case "Patika":
                break;
            default:
                break;
        }
        timerInSeconds = 0;
        timerStopped=true;
    }

    public void mainFunc() throws JSONException {
        if(currentQ == 1){
            solveTimeList.add(timerInSeconds);
            setContentView(R.layout.activity_game_hazine_avi8);
            timerTV = findViewById(R.id.timeTV_game);
            switch (gameName){
                case "Hazine Avı":
                    HazineAviUtils.gridSize=8;
                    secondsToGo = 180;
                    afterSecondsToGoMinMillis = Calendar.getInstance().getTimeInMillis() + secondsToGo*1000;
                    break;
                case "Patika":
                    break;
                default:
                    break;
            }
            Log.i("solveTimeList",solveTimeList.toString());
        }
        else if(currentQ == 2){
            solveTimeList.add(timerInSeconds-solveTimeList.get(0));
            setContentView(R.layout.activity_game_hazine_avi10);
            timerTV = findViewById(R.id.timeTV_game);
            if(gameName.contains("azine")){
                HazineAviUtils.gridSize=10;
                secondsToGo = 300;
                afterSecondsToGoMinMillis = Calendar.getInstance().getTimeInMillis() + secondsToGo*1000;
            }
            Log.i("solveTimeList",solveTimeList.toString());
        }
        TextView undoTV = findViewById(R.id.undoTV_ga);
        TextView resetTV = findViewById(R.id.resetTV_game);
        undoTV.setEnabled(true);
        resetTV.setEnabled(true);
        findViewById(R.id.clickView).setVisibility(GONE);
        clearGrid();
        seperateGridAnswer(allQs.getJSONArray(currentQ).getJSONArray(0).getJSONArray(0));
        currentQ++;
    }



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
    public void doSthEveryXSec(){

    }

    public void goToGameList(View view){
        disconnectSocket();
        timerStopped=true;
        inTheRoom=true;
        gotScores=true;
        Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
        intent.putExtra("type","multi"+pType);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    private Socket socket;
    {
        try {
            socket = IO.socket("https://plato-multiplayer.herokuapp.com");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void connectSocket(){
        socket.connect();
        Log.i("socket","connectSocket");
        socket.on("wait", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("socket-wait",".");
                    }
                });
            }
        });
        socket.on("roomFound", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        inTheRoom = true;
                        socketId = (String) args[0];
                        Log.i("socket-roomFound","Socket Id: "+socketId);
                        getInRoom();
                    }
                });
            }
        });
        socket.on("start", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            findViewById(R.id.waitingDialogCL).setVisibility(GONE);
                            JSONObject room = (JSONObject) args[0];
                            JSONArray players = room.getJSONArray("players");
                            for(int i = 0; i<players.length(); i++){
                                String plName = players.getJSONObject(i).getString("username");
                                playerNames.add(plName);
                                findViewById(R.id.opponentNamesLL).findViewWithTag("opp"+(i+1));
                            }
                            findViewById(R.id.opponentNamesLL).setVisibility(View.VISIBLE);
                            allQs = (new JSONObject(room.getString("games"))).getJSONArray("Info");
                            currentQ = 1;
                            setContentView(R.layout.activity_game_hazine_avi5);

                            if(gameName.contains("azine")){
                                HazineAviUtils.gridSize=5;
                                secondsToGo = 60;
                                afterSecondsToGoMinMillis = Calendar.getInstance().getTimeInMillis() + secondsToGo*1000;
                            }
                            TextView undoTV = findViewById(R.id.undoTV_ga);
                            TextView resetTV = findViewById(R.id.resetTV_game);
                            undoTV.setEnabled(true);
                            resetTV.setEnabled(true);
                            findViewById(R.id.clickView).setVisibility(GONE);
                            clearGrid();
                            seperateGridAnswer(allQs.getJSONArray(0).getJSONArray(0).getJSONArray(0));
                            timerFunc();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i("socket-start","Socket Room: "+args[0]);

                    }
                });
            }
        });
        socket.on("timeisup", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("socket-timeisup",".");
                    }
                });
            }
        });
        socket.on("scores", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        try {
                            gotScores=true;
                            findViewById(R.id.waitingDialogCL).setVisibility(GONE);
                            JSONArray players = (JSONArray) args[0];
                            for(int i = 0; i<players.length(); i++){
                                JSONObject player = players.getJSONObject(i);
                                String plName = player.getString("username");
                                int plScore = players.getJSONObject(i).getInt("score");
                                LinearLayout horLL = findViewById(R.id.scoresLL).findViewWithTag("LL" + (i + 1));
                                ((TextView) horLL.getChildAt(1)).setText(plName); // kullanıcı adı
                                ((TextView) horLL.getChildAt(2)).setText(Integer.toString(plScore));
                            }
                            findViewById(R.id.scoresLL).setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.i("socket-scores","Scores: "+args[0]);

                    }
                });
            }
        });
    }

    public void disconnectSocket(){
        socket.disconnect();
    }

    public void playerJoin(){
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("cluster", cluster);
        map.put("game", dbGameName);
        map.put("pType",pType);

        socket.emit("playerJoin", new JSONObject(map));
        Log.i("socket","playerJoin");
    }

    public void findRoom(){
        Log.i("socket","findRoom");
        socket.emit("findRoom");
    }

    public void getInRoom(){
        Log.i("socket","getInRoom");
        socket.emit("getInRoom",socketId);
    }

    @SuppressWarnings("deprecation")
    public void sendScore(int score){
        Log.i("socket","sendScore: "+score);
        socket.emit("sendScore",score);
        final Handler getScoresHandler = new Handler();
        Runnable getScoresRunnable = null;
        final Runnable finalFindRoomRunnable = getScoresRunnable;
        getScoresRunnable = new Runnable() {
            @Override
            public void run() {
                if(!gotScores) {
                    getScoresHandler.postDelayed(this, 3000);
                    getScores();
                } else {
                    getScoresHandler.removeCallbacks(this);
                    getScoresHandler.removeCallbacks(finalFindRoomRunnable);
                }
            }
        };
        getScoresHandler.post(getScoresRunnable);
    }

    public void getScores(){
        Log.i("socket","getScores");
        socket.emit("getScores");
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        Intent intent = getIntent();
        gameName = intent.getStringExtra("gameName");
        Log.i("gameName",gameName);
        pType = intent.getStringExtra("pType");
        Log.i("pType",pType);
        game = shownToDatabase("shownToDatabase", gameName+" "+getString(R.string.Easy));
        Log.i("game",game);
        dbGameName = game.split("\\.")[0];
        Log.i("dbGameName",dbGameName);

        SharedPreferences sp =getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        username = sp.getString("username",getString(R.string.Unknown));
        Log.i("username",username);
        cluster = sp.getString(dbGameName+"Cluster",getString(R.string.Unknown));
        Log.i("cluster",cluster);
        if(gameName.contains("iramit")){
            numberOfQ = 4;
        }

        connectSocket();
        playerJoin();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if(!inTheRoom){
//                    findRoom();
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(!inTheRoom){
//                                findRoom();
//                                new Handler().postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if(!inTheRoom){
//                                            findRoom();
//                                            new Handler().postDelayed(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    findRoom();
//                                                }
//                                            },5000);
//                                        }
//                                    }
//                                },5000);
//                            }
//                        }
//                    },5000);
//                }
//            }
//        }, 5000);

        final Handler findRoomHandler = new Handler();
        Runnable findRoomRunnable = null;
        final Runnable finalFindRoomRunnable = findRoomRunnable;
        findRoomRunnable = new Runnable() {
            @Override
            public void run() {
                if(!inTheRoom) {
                    findRoomHandler.postDelayed(this, 5000);
                    findRoom();
                } else {
                    findRoomHandler.removeCallbacks(this);
                    findRoomHandler.removeCallbacks(finalFindRoomRunnable);
                }
            }
        };
        findRoomHandler.post(findRoomRunnable);
    }

    @Override
    public void onBackPressed() {
        disconnectSocket();
        timerStopped=true;
        inTheRoom=true;
        gotScores=true;
        Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
        intent.putExtra("type","multi"+pType);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @Override
    protected void onDestroy() {
        disconnectSocket();
        timerStopped=true;
        super.onDestroy();
    }
}