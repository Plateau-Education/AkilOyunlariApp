package com.yaquila.akiloyunlariapp;

import androidx.annotation.NonNull;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import com.yaquila.akiloyunlariapp.model.AGEventHandler;
import com.yaquila.akiloyunlariapp.model.ConstantApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class GroupSolvingActivity extends BaseActivityForVoice implements AGEventHandler {

    String dbGameName;
    String gameName;
    String difficulty;
    String clickedBox = "-1";
    String switchPosition = "diamond";
    String type;
    String user_name;
    String classid;
    String currentScreen = "selection";
    String instructorName = null;
    int gridSize = 5;
    boolean gotQuestion = false;
    boolean isPermitted = false;
    boolean isConnected = false;
    boolean isParticipantsShown = false;

    List<List<String>> operations = new ArrayList<>();
    List<List<Integer>> currentGrid = new ArrayList<>();
    List<String> clueIndexes = new ArrayList<>();
    List<String> answer = new ArrayList<>();
    List<String> newTaskProperties = new ArrayList<>();
    Map<String,Boolean> participantMap = new HashMap<>();
    LoadingDialog loadingDialog;
    GridLayout gridGL;
    ConstraintLayout participantsLayout;
    AlertDialog ntDialog;
    AlertDialog participantsDialog;
    Spinner gameSpinner;
    Spinner diffSpinner;

    public void wannaLeaveDialog(View view){
        LayoutInflater factory = LayoutInflater.from(this);
        final View leaveDialogView = factory.inflate(R.layout.leave_dialog, null);
        final AlertDialog leaveDialog = new AlertDialog.Builder(this).create();
        leaveDialog.setView(leaveDialogView);

        leaveDialogView.findViewById(R.id.leaveDialogYes).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyClassActivity.class);
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
        LayoutInflater factory = LayoutInflater.from(this);
        final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
        final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
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
                ((ConstraintLayout)findViewById(R.id.diffTV_game).getParent()).setVisibility(View.GONE);
                selectGameDiff(null);
                correctDialog.dismiss();
            }
        });
        correctDialog.show();
    } // Sonraki soruya geç
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void changeClicked(View view){
        TextView box = (TextView) view;
        String answerIndex = box.getTag().toString();
        if(!clueIndexes.contains(answerIndex) && (isPermitted || type.contains("nstructor"))) {
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
            assert op != null;
            currentGrid.get(Integer.parseInt(String.valueOf(answerIndex.charAt(1)))).set(Integer.parseInt(String.valueOf(answerIndex.charAt(0))),Integer.parseInt(op));
            sendGrid();
            Log.i("currentGrid",currentGrid.toString());
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
        if(isPermitted || type.contains("nstructor")) {
            if (switchPosition.equals("diamond")) {
                switchTV.setImageResource(R.drawable.ic_cross);
                switchPosition = "cross";
            } else if (switchPosition.equals("cross")) {
                switchTV.setImageResource(R.drawable.ic_diamond);
                switchPosition = "diamond";
            }
        }
    } // Elmas - çarpı değiştir
    public void undoOperation(View view){
        if(operations.size() > 1 && (isPermitted || type.contains("nstructor"))){
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
            GridLayout gridLayout = gridGL;
            TextView currentBox = gridLayout.findViewWithTag(co1);
            currentGrid.get(Integer.parseInt(String.valueOf(co1.charAt(1)))).set(Integer.parseInt(String.valueOf(co1.charAt(0))),Integer.parseInt(num2));
            sendGrid();

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
        if(isPermitted || type.contains("nstructor")) {
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
                GridLayout gridLayout = gridGL;
                clickedBox = "-1";
                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                        tv.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                        if (!clueIndexes.contains(Integer.toString(j) + i)) {
                            tv.setText("");
                        }
                    }
                }
                for (int i = 0; i < gridSize; i++) {
                    List<Integer> row = currentGrid.get(i);
                    for (int j = 0; j < gridSize; j++) {
                        if (row.get(j) < 0) row.set(j, 0);
                    }
                    currentGrid.set(i, row);
                }
                Log.i("currentGrid-reset", currentGrid.toString());
                sendGrid();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    } // Tüm işlemleri sıfırla
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkAnswer(View view){
        GridLayout gridLayout = gridGL;
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
        Log.i("check",checking+"  "+answer);
        if(checking){
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

            if(type.contains("nstructor")) {
                nextQuestion(null);
            }
        }
    } // Çözümün doğruluğunu kontrol et
    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    public class GetRequest extends AsyncTask<String, Void, String> {

        SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringBuilder result = new StringBuilder();
                String id = sharedPreferences.getString("id", "non");
                URL reqURL;
                reqURL = new URL(strings[0] + "/" + id + "?" + "Info=" + (1) + "&Token=" + strings[1]);
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
                JSONObject jb = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));
                JSONArray gridArrays = (JSONArray)jb.get("Info");
                JSONArray idArray = (JSONArray)jb.get("Ids");
                Log.i("idarray",idArray.toString()+"  "+idArray.length()+"    ga:"+gridArrays.length());
                seperateGridAnswer(gridArrays.getJSONArray(0).getJSONArray(0).getJSONArray(0), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            gotQuestion = true;
            loadingDialog.dismissDialog();
            ((ConstraintLayout)findViewById(R.id.diffTV_game).getParent()).setVisibility(View.VISIBLE);
        }
    } // API'den soru çek
    public void seperateGridAnswer(JSONArray grid, boolean fromStudent) throws JSONException {
        GridLayout gridLayout = gridGL;
        for(int i = 0; i < gridSize; i++){
            for(int j = 0; j <  gridSize; j++) {
                String n = ((JSONArray)grid.get(i)).get(j).toString();
                if(Integer.parseInt(n) > 0){
                    currentGrid.get(i).set(j,Integer.parseInt(n));
                    if(!fromStudent) clueIndexes.add(Integer.toString(j)+i);
                    ((TextView) gridLayout.findViewWithTag(Integer.toString(j)+i)).setText(n);
                }
                else if(n.equals("-1")){
                    if(!type.contains("nstructor") || fromStudent) {
                        currentGrid.get(i).set(j, Integer.parseInt(n));
                        gridLayout.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.ic_diamond));
                    }
                    if(!fromStudent) answer.add(Integer.toString(j)+i);


                }
                else if(n.equals("-2") && (!type.contains("nstructor") || fromStudent)){
                    currentGrid.get(i).set(j, Integer.parseInt(n));
                    gridLayout.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.ic_cross));
                }
                else if(n.equals("0") && (!type.contains("nstructor") || fromStudent)){
                    currentGrid.get(i).set(j, Integer.parseInt(n));
                    gridLayout.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                }
            }
        }
        Log.i("answer",answer+"");
        if(type.contains("nstructor") && !isConnected) {
            isConnected = true;
            connectSocket();
            joinClass();
        } else if (type.contains("nstructor")){
            sendGrid();
        }

        Log.i("clueIndexes",clueIndexes.toString());
        Log.i("currentGrid",currentGrid.toString());
    } // Çekilen soruyu kullanıcıya göster
    @SuppressLint("InflateParams")
    public void loadingDialogFunc(){
        loadingDialog = new LoadingDialog(GroupSolvingActivity.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
        loadingDialog.startLoadingAnimation();
    }
    public int translateDiff(String diff){
        Map<String,Integer> map = new HashMap<>();
        map.put("Kolay",R.string.Easy);
        map.put("Orta",R.string.Medium);
        map.put("Zor",R.string.Hard);
        return map.get(diff);
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
    public boolean checkIfGridHasDC(JSONArray grid) throws JSONException{
        boolean flag = false;
        for(int i = 0; i < gridSize; i++) {
            if(flag) break;
            for (int j = 0; j < gridSize; j++) {
                String n = ((JSONArray)grid.get(i)).get(j).toString();
                if(n.equals("-1") || n.equals("-2")){
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }
    public void clearGrid(){
        operations = new ArrayList<>();
        operations.add(new ArrayList<>(Arrays.asList("00", "0")));
        GridLayout gridLayout = gridGL;
        currentGrid = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < gridSize; j++) {
                TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                tv.setText("");
                tv.setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                tv.setEnabled(true);
                row.add(0);
            }
            currentGrid.add(row);
        }
        Log.i("firstCurrentGrid",currentGrid.toString());
        clickedBox = "-1";
    }
    public void mainFunc(){
        TextView undoTV = findViewById(R.id.undoTV_ga);
        TextView resetTV = findViewById(R.id.resetTV_game);
        undoTV.setEnabled(true);
        resetTV.setEnabled(true);
        findViewById(R.id.clickView).setVisibility(View.GONE);
        clearGrid();
        answer = new ArrayList<>();
        clueIndexes = new ArrayList<>();
        GetRequest getRequest = new GetRequest();
        //noinspection deprecation
        getRequest.execute("https://akiloyunlariapp.herokuapp.com/"+newTaskProperties.get(0),"fx!Ay:;<p6Q?C8N{");
        loadingDialogFunc();
    }

    public void selectGameDiff(View view){
        try{
            LayoutInflater factory = LayoutInflater.from(GroupSolvingActivity.this);
            final View ntLayout = factory.inflate(R.layout.groupsolving_gameselection, null);
            ntDialog = new AlertDialog.Builder(this).create();
            ntDialog.setCancelable(false);
            ntDialog.setView(ntLayout);

            ntLayout.findViewById(R.id.startbutton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendGameDiff(ntLayout);
                    ntDialog.dismiss();
                }
            });
            ntLayout.findViewById(R.id.closebutton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MyClassActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                    ntDialog.dismiss();
                }
            });
            ntDialog.show();

             gameSpinner = ntLayout.findViewById(R.id.gameSpinner);
            diffSpinner = ntLayout.findViewById(R.id.diffSpinner);
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
                        ArrayAdapter<String> changedDiffAdapter = new ArrayAdapter<>(GroupSolvingActivity.this, R.layout.spinner_tv,
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendGameDiff(View view){
        newTaskProperties = new ArrayList<>();
        newTaskProperties.add(shownToDatabase("shownToDatabase",gameSpinner.getSelectedItem() +" "+ diffSpinner.getSelectedItem()));
        dbGameName = newTaskProperties.get(0);
        String[] ntp = newTaskProperties.get(0).split("\\.");
        gridSize = Integer.parseInt(ntp[1]);
        gameName = (String) gameSpinner.getSelectedItem();
        difficulty = (String) diffSpinner.getSelectedItem();
        ((TextView)findViewById(R.id.diffTV_game)).setText(translateDiff(difficulty));
        LayoutInflater inflater = getLayoutInflater();
        gridGL = (GridLayout) inflater.inflate(this.getResources().getIdentifier(gameName.toLowerCase().replace(" ","").
                replace("ı","i").replace("ö","o").replace("ü","u")
                +gridSize+"_grid", "layout", this.getPackageName()),null);
        RelativeLayout gridRL = findViewById(R.id.gridGL_ga);
        gridRL.addView(gridGL);
        initUIandEvent();
        mainFunc();
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    public void openParticipants(View view){
        LayoutInflater factory = getLayoutInflater();
        participantsLayout = (ConstraintLayout) factory.inflate(R.layout.participants_dialog, null);
        ((LinearLayout)participantsLayout.findViewById(R.id.prtpLL)).removeAllViews();
        ConstraintLayout instructorRow= (ConstraintLayout) factory.inflate(this.getResources().getIdentifier("participant_row", "layout", this.getPackageName()),null);
        ((ImageView)instructorRow.findViewById(R.id.avatarS1)).setImageResource(R.drawable.ic_teacher_usericon);
        ((TextView)instructorRow.findViewById(R.id.usernameTV)).setText(instructorName);
        ((LinearLayout)participantsLayout.findViewById(R.id.prtpLL)).addView(instructorRow);
        for(int i = 0; i < participantMap.size(); i++){
            ConstraintLayout participantRow= (ConstraintLayout) factory.inflate(this.getResources().getIdentifier("participant_row", "layout", this.getPackageName()),null);
            ((TextView)participantRow.findViewById(R.id.usernameTV)).setText((String) participantMap.keySet().toArray()[i]);
            if(type.contains("nstructor")){
                participantRow.findViewById(R.id.permissionButton).setVisibility(View.VISIBLE);
                if((Boolean) participantMap.values().toArray()[i]) {
                    ((ImageView)participantRow.findViewById(R.id.permissionButton)).setImageResource(R.drawable.ic_delete_icon);
                } else {
                    ((ImageView)participantRow.findViewById(R.id.permissionButton)).setImageResource(R.drawable.ic_key_icon);
                }
            }
            ((LinearLayout)participantsLayout.findViewById(R.id.prtpLL)).addView(participantRow);
        }

        participantsDialog = new AlertDialog.Builder(this).create();
        participantsDialog.setCancelable(false);
        participantsDialog.setView(participantsLayout);
        participantsLayout.findViewById(R.id.closebutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                participantsDialog.dismiss();
                isParticipantsShown = false;
            }
        });
        participantsDialog.show();
        isParticipantsShown = true;
    }

    public void changeParticipantsInRT(){
        LayoutInflater factory = getLayoutInflater();
        ((LinearLayout)participantsLayout.findViewById(R.id.prtpLL)).removeAllViews();
        ConstraintLayout instructorRow= (ConstraintLayout) factory.inflate(this.getResources().getIdentifier("participant_row", "layout", this.getPackageName()),null);
        ((ImageView)instructorRow.findViewById(R.id.avatarS1)).setImageResource(R.drawable.ic_teacher_usericon);
        ((TextView)instructorRow.findViewById(R.id.usernameTV)).setText(instructorName);
        ((LinearLayout)participantsLayout.findViewById(R.id.prtpLL)).addView(instructorRow);
        for(int i = 0; i < participantMap.size(); i++){
            ConstraintLayout participantRow= (ConstraintLayout) factory.inflate(this.getResources().getIdentifier("participant_row", "layout", this.getPackageName()),null);
            ((TextView)participantRow.findViewById(R.id.usernameTV)).setText((String) participantMap.keySet().toArray()[i]);
            if(type.contains("nstructor")){
                participantRow.findViewById(R.id.permissionButton).setVisibility(View.VISIBLE);
                if((Boolean) participantMap.values().toArray()[i]) {
                    ((ImageView)participantRow.findViewById(R.id.permissionButton)).setImageResource(R.drawable.ic_delete_icon);
                } else {
                    ((ImageView)participantRow.findViewById(R.id.permissionButton)).setImageResource(R.drawable.ic_key_icon);
                }
            }
            ((LinearLayout)participantsLayout.findViewById(R.id.prtpLL)).addView(participantRow);
        }
        participantsDialog.setView(participantsLayout);
    }

    public void giveOrRemovePermission(View view){
        String username = (String) ((TextView)(((ConstraintLayout)view.getParent()).findViewById(R.id.usernameTV))).getText();
        if(!participantMap.get(username)){
            givePermission(username);
            ((ImageView)view).setImageResource(R.drawable.ic_delete_icon);
        } else {
            removePermission(username);
            ((ImageView)view).setImageResource(R.drawable.ic_key_icon);
        }
    }


//    private final static Logger log = LoggerFactory.getLogger(ChatActivity.class);

    private volatile boolean mAudioMuted = true;

    private volatile int mAudioRouting = -1; // Default

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    protected void initUIandEvent() {
        event().addEventHandler(this);

        String channelName = "HG3uzk";
        vSettings().mChannelName = channelName;
        Log.i("channelName", channelName);
        /*
          Allows a user to join a channel.

          Users in the same channel can talk to each other, and multiple users in the same channel can start a group chat. Users with different App IDs cannot call each other.

          You must call the leaveChannel method to exit the current call before joining another channel.

          A successful joinChannel method call triggers the following callbacks:

          The local client: onJoinChannelSuccess.
          The remote client: onUserJoined, if the user joining the channel is in the Communication profile, or is a BROADCASTER in the Live Broadcast profile.

          When the connection between the client and Agora's server is interrupted due to poor
          network conditions, the SDK tries reconnecting to the server. When the local client
          successfully rejoins the channel, the SDK triggers the onRejoinChannelSuccess callback
          on the local client.

         */
        worker().joinChannel(channelName, config().mUid);

//        TextView textChannelName = (TextView) findViewById(R.id.channel_name);
//        textChannelName.setText(channelName);

        optional();

//        LinearLayout bottomContainer = (LinearLayout) findViewById(R.id.bottom_container);
//        FrameLayout.MarginLayoutParams fmp = (FrameLayout.MarginLayoutParams) bottomContainer.getLayoutParams();
//        fmp.bottomMargin = virtualKeyHeight() + 16;
    }

    private Handler mMainHandler;

    private static final int UPDATE_UI_MESSAGE = 0x1024;

    EditText mMessageList;

    StringBuffer mMessageCache = new StringBuffer();

    private void notifyMessageChanged(String msg) {
        if (mMessageCache.length() > 10000) { // drop messages
            mMessageCache = new StringBuffer(mMessageCache.substring(10000 - 40));
        }

        mMessageCache.append(System.currentTimeMillis()).append(": ").append(msg).append("\n"); // append timestamp for messages

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                if (mMainHandler == null) {
                    mMainHandler = new Handler(getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);

                            if (isFinishing()) {
                                return;
                            }

                            switch (msg.what) {
                                case UPDATE_UI_MESSAGE:
                                    String content = (String) (msg.obj);
                                    Log.i("UPDATE_UI_MESSAGE",content);
//                                    mMessageList.setText(content);
//                                    mMessageList.setSelection(content.length());
                                    break;

                                default:
                                    break;
                            }

                        }
                    };

//                    mMessageList = (EditText) findViewById(R.id.msg_list);
                }

                mMainHandler.removeMessages(UPDATE_UI_MESSAGE);
                Message envelop = new Message();
                envelop.what = UPDATE_UI_MESSAGE;
                envelop.obj = mMessageCache.toString();
                mMainHandler.sendMessageDelayed(envelop, 1000l);
            }
        });
    }

    private void optional() {
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    private void optionalDestroy() {
    }

    public void onSwitchSpeakerClicked(View view) {
        Log.i("onSwitchSpeakerClicked ",  view + " " + mAudioMuted + " " + mAudioRouting);

        RtcEngine rtcEngine = rtcEngine();

        /*
          Enables/Disables the audio playback route to the speakerphone.
          This method sets whether the audio is routed to the speakerphone or earpiece.
          After calling this method, the SDK returns the onAudioRouteChanged callback
          to indicate the changes.
         */
        rtcEngine.setEnableSpeakerphone(mAudioRouting != 3);
    }

    @Override
    protected void deInitUIandEvent() {
        optionalDestroy();

        doLeaveChannel();
        event().removeEventHandler(this);
    }

    /**
     * Allows a user to leave a channel.
     *
     * After joining a channel, the user must call the leaveChannel method to end the call before
     * joining another channel. This method returns 0 if the user leaves the channel and releases
     * all resources related to the call. This method call is asynchronous, and the user has not
     * exited the channel when the method call returns. Once the user leaves the channel,
     * the SDK triggers the onLeaveChannel callback.
     *
     * A successful leaveChannel method call triggers the following callbacks:
     *
     * The local client: onLeaveChannel.
     * The remote client: onUserOffline, if the user leaving the channel is in the
     * Communication channel, or is a BROADCASTER in the Live Broadcast profile.
     *
     */
    private void doLeaveChannel() {
        worker().leaveChannel(config().mChannel);
    }

    public void onEndCallClicked(View view) {
        Log.i("onEndCallClicked ", ""+view);

        quitCall();
    }

    private void quitCall() {
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);

        finish();
    }

    public void onVoiceMuteClicked(View view) {
        Log.i("onVoiceMuteClicked ", view + " audio_status: " + mAudioMuted);

        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.muteLocalAudioStream(mAudioMuted = !mAudioMuted);

        ImageView iv = (ImageView) view;

        if (mAudioMuted) {
            iv.setImageResource(R.drawable.ic_microphone_closed);
        } else {
            iv.setImageResource(R.drawable.ic_microphone_open);
        }
    }

    @Override
    public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
        String msg = "onJoinChannelSuccess " + channel + " " + (uid & 0xFFFFFFFFL) + " " + elapsed;
        Log.d("onJoinChannelSuccess", msg);

        notifyMessageChanged(msg);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                rtcEngine().muteLocalAudioStream(mAudioMuted);
            }
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        String msg = "onUserOffline " + (uid & 0xFFFFFFFFL) + " " + reason;
        Log.d("onUserOffline",msg);

        notifyMessageChanged(msg);

    }

    @Override
    public void onExtraCallback(final int type, final Object... data) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                doHandleExtraCallback(type, data);
            }
        });
    }

    private void doHandleExtraCallback(int type, Object... data) {
        int peerUid;
        boolean muted;

        switch (type) {
            case AGEventHandler.EVENT_TYPE_ON_USER_AUDIO_MUTED: {
                peerUid = (Integer) data[0];
                muted = (boolean) data[1];

                notifyMessageChanged("mute: " + (peerUid & 0xFFFFFFFFL) + " " + muted);
                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AUDIO_QUALITY: {
                peerUid = (Integer) data[0];
                int quality = (int) data[1];
                short delay = (short) data[2];
                short lost = (short) data[3];

                notifyMessageChanged("quality: " + (peerUid & 0xFFFFFFFFL) + " " + quality + " " + delay + " " + lost);
                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_SPEAKER_STATS: {
                IRtcEngineEventHandler.AudioVolumeInfo[] infos = (IRtcEngineEventHandler.AudioVolumeInfo[]) data[0];

                if (infos.length == 1 && infos[0].uid == 0) { // local guy, ignore it
                    break;
                }

                StringBuilder volumeCache = new StringBuilder();
                for (IRtcEngineEventHandler.AudioVolumeInfo each : infos) {
                    peerUid = each.uid;
                    int peerVolume = each.volume;

                    if (peerUid == 0) {
                        continue;
                    }

                    volumeCache.append("volume: ").append(peerUid & 0xFFFFFFFFL).append(" ").append(peerVolume).append("\n");
                }

                if (volumeCache.length() > 0) {
                    String volumeMsg = volumeCache.substring(0, volumeCache.length() - 1);
                    notifyMessageChanged(volumeMsg);

                    if ((System.currentTimeMillis() / 1000) % 10 == 0) {
                        Log.d("volumeMsg",volumeMsg);
                    }
                }
                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_APP_ERROR: {
                int subType = (int) data[0];

                if (subType == ConstantApp.AppError.NO_NETWORK_CONNECTION) {
                    showLongToast(getString(R.string.msg_no_network_connection));
                }

                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AGORA_MEDIA_ERROR: {
                int error = (int) data[0];
                String description = (String) data[1];

                notifyMessageChanged(error + " " + description);

                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED: {
                notifyHeadsetPlugged((int) data[0]);

                break;
            }
        }
    }

    public void notifyHeadsetPlugged(final int routing) {
        Log.i("notifyHeadsetPlugged ", ""+routing);

        mAudioRouting = routing;

//        ImageView iv = (ImageView) findViewById(R.id.switch_speaker_id);
//        if (mAudioRouting == 3) { // Speakerphone
//            iv.setColorFilter(getResources().getColor(R.color.agora_blue), PorterDuff.Mode.MULTIPLY);
//        } else {
//            iv.clearColorFilter();
//        }
    }

    private Socket socket;
    {
        try {
            socket = IO.socket("https://server4groups.herokuapp.com");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void connectSocket(){
        socket.connect();
        Log.i("Connected","Connected");
        socket.on("joinToRoom", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg =(String) args[0];
                        Log.i("joinToRoom",msg+ ".");
                        if(msg.equals("failed")){
                            disconnectSocket(null);
                            Toast.makeText(GroupSolvingActivity.this, R.string.Instructor_Hasnt_started, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MyClassActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                        }
                    }
                });
            }
        });
        socket.on("gameType", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!type.contains("nstructor")) {
                            dbGameName = (String) args[0];
                            String[] ntp = dbGameName.split("\\.");
                            gameName = ntp[0];
                            gridSize = Integer.parseInt(ntp[1]);
                            gridGL = (GridLayout) getLayoutInflater().inflate(getResources().getIdentifier(gameName.toLowerCase().replace(" ","").
                                    replace("ı","i").replace("ö","o").replace("ü","u")
                                    +gridSize+"_grid", "layout", getPackageName()),null);
                        }
                        Log.i("gameType", args[0] + ".");
                    }
                });
            }
        });
        socket.on("sendGrid", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
//                        if(!type.contains("nstructor")) {
                            ((ConstraintLayout) findViewById(R.id.diffTV_game).getParent()).setVisibility(View.VISIBLE);

                            try {
                                String result = (String) args[0];
//                                JSONObject jb = new JSONObject(result);
                                JSONArray grid = new JSONArray(result);
                                if(!checkIfGridHasDC(grid)){
                                    clearGrid();
                                    Log.i("grid","cleared");
                                }
                                if(type.contains("nstructor")){
                                    checkAnswer(null);
                                }
                                seperateGridAnswer(grid, true);
                                RelativeLayout gridRL = findViewById(R.id.gridGL_ga);
                                gridRL.removeAllViews();
                                gridRL.addView(gridGL);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
//                        }

                        Log.i("sendGrid", args[0] + ".");
                    }
                });
            }
        });
        socket.on("participants", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject room = (JSONObject) args[0];
                            JSONObject prtps = room.getJSONObject("participants");
                            if(instructorName == null){
                                instructorName = prtps.getJSONObject("instructor").getString("username");
                                if(!type.contains("nstructor")){
                                    initUIandEvent();
                                }
                            }
                            JSONArray students = prtps.getJSONArray("students");
                            List<String> stNameList = new ArrayList<>();
                            for(int i = 0; i<students.length(); i++){
                                String stName = students.getJSONObject(i).getString("username");
                                stNameList.add(stName);
                                if(participantMap.get(stName)==null){
                                    participantMap.put(stName,false);
                                }
                            }
                            for(String s : participantMap.keySet()){
                                if(!stNameList.contains(s)){
                                    try {
                                        participantMap.remove(s);
                                    } catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if(isParticipantsShown){
                                changeParticipantsInRT();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.i("participantMap",participantMap.toString());
                        Log.i("participants",args[0]+ ".");
                    }
                });
            }
        });
        socket.on("permissionGranted", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isPermitted = true;
                        operations = new ArrayList<>();
                        operations.add(new ArrayList<>(Arrays.asList("00", "0")));
                        Toast.makeText(GroupSolvingActivity.this, getString(R.string.permissionGranted), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        socket.on("permissionRemoved", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isPermitted = false;
                        operations = new ArrayList<>();
                        operations.add(new ArrayList<>(Arrays.asList("00", "0")));
                        Toast.makeText(GroupSolvingActivity.this, getString(R.string.permissionRemoved), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


//        socket.on("voiceChat", new Emitter.Listener() {
//            @Override
//            public void call(final Object... args) {
//                runOnUiThread(new Runnable() {
//                    @RequiresApi(api = Build.VERSION_CODES.M)
//                    @Override
//                    public void run() {
////                        byte[] byteArray = (byte[])args[0];
////
////                        playMp3(byteArray);
////                        Log.i("socket- voiceChat", new String(byteArray)+".");
//
//                    }
//                });
//            }
//        });
    }

    public void disconnectSocket(View view){
        socket.disconnect();
    }

    public void joinClass(){
        Map<String, String> map = new HashMap<>();
        map.put("user_name", user_name);
        map.put("role", type);
        map.put("room_id", classid);
        if(type.contains("nstructor")) {
            map.put("grid", currentGrid.toString());
            map.put("gameType", dbGameName);
        }

        socket.emit("joinToRoom", new JSONObject(map));
        Log.i("socket","joinToRoom");
    }

    public void sendGrid(){
        socket.emit("sendGrid",currentGrid);
    }

    public void givePermission(String username){
        socket.emit("givePermission", username);
        participantMap.put(username,true);

    }

    public void removePermission(String username){
        socket.emit("removePermission", username);
        participantMap.put(username,false);
    }


    //    private boolean playPause;
//    private MediaPlayer mediaPlayer;
//    private boolean initialStage = true;
//
//    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_solving);
        SharedPreferences sp = getSharedPreferences("com.yaquila.akiloyunlariapp", MODE_PRIVATE);
        type = sp.getString("type", getString(R.string.Unknown));
        user_name = sp.getString("username", getString(R.string.Unknown));
        classid = sp.getString("classid", getString(R.string.Unknown));
        currentGrid = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < gridSize; j++) {
                row.add(0);
            }
            currentGrid.add(row);
        }
        if (type.contains("nstructor")) {
            selectGameDiff(null);
        } else {
            connectSocket();
            joinClass();
        }
    }

    @Override
    protected void onDestroy() {
        disconnectSocket(null);
        try {
            onEndCallClicked(null);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        disconnectSocket(null);
        try {
            onEndCallClicked(null);
        }catch(Exception e){
            e.printStackTrace();
        }
//        if(currentScreen.equals("selection")) {
//            ntDialog.dismiss();
//            Intent intent = new Intent(getApplicationContext(), MyClassActivity.class);
//            startActivity(intent);
//            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
//        }
//        else if(currentScreen.equals("game")){
//            wannaLeaveDialog(null);
//        }
        Intent intent = new Intent(getApplicationContext(), MyClassActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}