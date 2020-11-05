package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.JsonReader;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    String gameName;
    String difficulty;
    int clickedBox = -1;
    int gridSize = 3;
    List<List<Integer>> operations = new ArrayList<>();
    boolean undoing=false;
    boolean[] draftModeActive={false,false,false};
    JSONArray answer;
    LoadingDialog loadingDialog;


    public void wannaLeaveDialog(View view){
        LayoutInflater factory = LayoutInflater.from(this);
        final View leaveDialogView = factory.inflate(R.layout.leave_dialog, null);
        final AlertDialog leaveDialog = new AlertDialog.Builder(this).create();
        leaveDialog.setView(leaveDialogView);

        leaveDialogView.findViewById(R.id.leaveDialogYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //your business logic
                Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
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
    }

    public void changeClicked(View view){
        TextView box = (TextView) view;
        GridLayout gridLayout = (GridLayout) findViewById(R.id.gridGL_ga);
        int answerIndex = Integer.parseInt((box.getTag().toString()).substring(box.getTag().toString().length()-1));
        if (clickedBox != answerIndex){
            if (clickedBox != -1){
                if (clickedBox == 0){
                    ((TextView)gridLayout.findViewWithTag("answer"+ clickedBox)).setBackground(getResources().getDrawable(R.drawable.strokebg_topbottomleft));
                }
                else{
                    ((TextView)gridLayout.findViewWithTag("answer"+ clickedBox)).setBackground(getResources().getDrawable(R.drawable.strokebg_topbottom));
                }
            }
            if(answerIndex == 0){
                box.setBackground(getResources().getDrawable(R.drawable.strokebg_topbottomleft_shallow2));
            }
            else{
                box.setBackground(getResources().getDrawable(R.drawable.strokebg_topbottom_shallow2));
            }
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
                clickedBox = -1;
            }
        }
        if(clickedBox != -1 && draftModeActive[clickedBox]){
            for(int i = 0; i<10; i++){
                ((Button)gridLayout.findViewWithTag(Integer.toString(i))).setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
            }
        }
    }

    public void numClicked(View view){
        Button btn = (Button) view;
        GridLayout gridLayout = (GridLayout) findViewById(R.id.gridGL_ga);
        if(clickedBox != -1){
            TextView currentBox = ((TextView)gridLayout.findViewWithTag("answer"+ clickedBox));
            if(currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox,-1)));
            }
            if(draftModeActive[clickedBox]){
                currentBox.setText(currentBox.getText().toString()+" "+btn.getTag().toString());
            }
            else{
                currentBox.setText(btn.getTag().toString());
            }
            operations.add(new ArrayList<>(Arrays.asList(clickedBox, Integer.parseInt(btn.getTag().toString()))));
            checkAnswer(null);
            Log.i("operations",operations+"");
        }
    }

    public void deleteNum(View view){
        GridLayout gridLayout = (GridLayout) findViewById(R.id.gridGL_ga);
        if(clickedBox != -1){
            TextView currentBox = ((TextView)gridLayout.findViewWithTag("answer"+ clickedBox));
            if(!currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox, -1)));
                Log.i("operations",operations+"");
                currentBox.setText("");
            }
        }
    }

    public void undoOperation(View view){
        if(operations.size() > 1){
            operations = operations.subList(0,operations.size()-1);
            List<Integer> tuple = operations.get(operations.size()-1);
            int co = tuple.get(0);
            int num = tuple.get(1);
            Log.i("co/num",co+" / "+num);
            GridLayout gridLayout = (GridLayout) findViewById(R.id.gridGL_ga);
            TextView currentBox = ((TextView)gridLayout.findViewWithTag("answer"+ co));
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
                    resetTV.setTextColor(getResources().getColor(R.color.near_black_blue));
                    resetTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    resetTV.setText(R.string.ResetUnderlined);
                }
            }, 100);

            operations = new ArrayList<>();
            GridLayout gridLayout = (GridLayout) findViewById(R.id.gridGL_ga);
            for (int i = 0; i < 3; i++) {
                TextView currentBox = ((TextView) gridLayout.findViewWithTag("answer" + i));
                currentBox.setText("");
            }
            if (clickedBox == 0) {
                ((TextView) gridLayout.findViewWithTag("answer" + clickedBox)).setBackground(getResources().getDrawable(R.drawable.strokebg_topbottomleft));
            } else {
                ((TextView) gridLayout.findViewWithTag("answer" + clickedBox)).setBackground(getResources().getDrawable(R.drawable.strokebg_topbottom));
            }
            clickedBox = -1;
            //TODO reset time.
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void checkAnswer(View view){
        GridLayout gridLayout = (GridLayout) findViewById(R.id.gridGL_ga);
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
            LayoutInflater factory = LayoutInflater.from(this);
            final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
            final AlertDialog correctDialog = new AlertDialog.Builder(this).create();
            correctDialog.setView(leaveDialogView);

            leaveDialogView.findViewById(R.id.correctDialogNext).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //your business logic
                    Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                    intent.putExtra("gameName",gameName);
                    intent.putExtra("difficulty",difficulty);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
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
        GridLayout numGrid = (GridLayout) findViewById(R.id.numsGL_ga);
        GridLayout questionGrid = (GridLayout) findViewById(R.id.gridGL_ga);
        if(clickedBox != -1){
            TextView currentClickedBox = (TextView) questionGrid.findViewWithTag("answer"+ clickedBox);
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
                String result = "";
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
                    result += current;
                    data = reader.read();
                }
                Log.i("result",result);
                return result;
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
            GridLayout gridLayout = (GridLayout) findViewById(R.id.gridGL_ga);
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
                        TextView tv = (TextView) gridLayout.findViewWithTag(Integer.toString(j)+ i);
                        tv.setText(row.get(j).toString());
                    }
                    JSONArray rguide = (JSONArray)row.get(row.length()-1);
                    TextView rGuideTV = (TextView) gridLayout.findViewWithTag("g"+i);
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
                TextView guideanswer = (TextView) gridLayout.findViewWithTag("answerguide");
                guideanswer.setText("+"+((JSONArray)answer.get(answer.length()-1)).get(0).toString());
                answer.remove(answer.length()-1);

                loadingDialog.dismissDialog();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadingDialogFunc(){
        loadingDialog = new LoadingDialog(GameActivity.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
        loadingDialog.startLoadingAnimation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        gameName = intent.getStringExtra("gameName");
        difficulty = intent.getStringExtra("difficulty");
        if (gameName.matches("Sayı Bulmaca")){
            setContentView(R.layout.activity_game_sayibulmaca);
            GetRequest getRequest = new GetRequest();
            getRequest.execute("https://akiloyunlariapp.herokuapp.com/SayiBulmaca3","fx!Ay:;<p6Q?C8N{");
            loadingDialogFunc();
        }
        else {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Not Sayı Bulmaca");
        }

//        if (gameName != null && difficulty != null){
//            Toast.makeText(this, gameName+" / "+difficulty, Toast.LENGTH_SHORT).show();
//        }

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        wannaLeaveDialog(null);
//        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}