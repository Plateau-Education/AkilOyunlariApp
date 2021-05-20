package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class GroupSolvingActivity extends AppCompatActivity {

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
                        } catch (JSONException e) {
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
//                        byte[] byteArray = (byte[])args[0];
//

//                        Log.i("socket.on", new String()+ ".");
                        // Create the AudioData object from the byte array
//                        AudioData audiodata = new AudioData(byteArray);
//// Create an AudioDataStream to play back
//                        AudioDataStream audioStream = new AudioDataStream(audioData);
//// Play the sound
//                        AudioPlayer.player.start(audioStream);
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
        SharedPreferences sp =getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        type = sp.getString("type",getString(R.string.Unknown));
        user_name = sp.getString("username",getString(R.string.Unknown));
        classid = sp.getString("classid",getString(R.string.Unknown));
        currentGrid = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < gridSize; j++) {
                row.add(0);
            }
            currentGrid.add(row);
        }
        if(type.contains("nstructor")) {
            selectGameDiff(null);
        }
        else{
            connectSocket();
            joinClass();
        }


//        btn = (Button) findViewById(R.id.button2);
//        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!playPause) {
//                    btn.setText("Pause Streaming");
//
//                    if (initialStage) {
//                        new Player().execute("https://server4groups.herokuapp.com");
//                    } else {
//                        if (!mediaPlayer.isPlaying())
//                            mediaPlayer.start();
//                    }
//
//                    playPause = true;
//
//                } else {
//                    btn.setText("Launch Streaming");
//
//                    if (mediaPlayer.isPlaying()) {
//                        mediaPlayer.pause();
//                    }
//
//                    playPause = false;
//                }
//            }
//        });
//
//
    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        if (mediaPlayer != null) {
//            mediaPlayer.reset();
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }
//    }
//
//    @SuppressWarnings("deprecation")
//    class Player extends AsyncTask<String, Void, Boolean> {
//        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//        @Override
//        protected Boolean doInBackground(String... strings) {
//            Boolean prepared = false;
//
//            try {
//                mediaPlayer.setDataSource(strings[0]);
//                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mediaPlayer) {
//                        initialStage = true;
//                        playPause = false;
//                        btn.setText("Launch Streaming");
//                        mediaPlayer.stop();
//                        mediaPlayer.reset();
//                    }
//                });
//
//                mediaPlayer.prepare();
//                prepared = true;
//
//            } catch (Exception e) {
//                Log.e("MyAudioStreamingApp", Objects.requireNonNull(e.getMessage()));
//                prepared = false;
//            }
//
//            return prepared;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean aBoolean) {
//            super.onPostExecute(aBoolean);
//
////            if (progressDialog.isShowing()) {
////                progressDialog.cancel();
////            }
//
//            mediaPlayer.start();
//            initialStage = false;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
////            progressDialog.setMessage("Buffering...");
////            progressDialog.show();
//        }
//    }


    @Override
    protected void onDestroy() {
        disconnectSocket(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        disconnectSocket(null);
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