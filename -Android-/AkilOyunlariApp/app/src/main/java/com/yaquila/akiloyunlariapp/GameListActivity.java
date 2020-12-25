package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class GameListActivity extends AppCompatActivity {

    int currentExtendedRow = 0;

    public void goToHowtoplay(View view){
        Intent intent = new Intent(getApplicationContext(), HowToPlayActivity.class);
        if(currentExtendedRow % 2 == 1){
            intent.putExtra("gameName", ((TextView)((LinearLayout)((LinearLayout)((RelativeLayout) view.getParent()).getParent()).getChildAt(0)).getChildAt(1)).getText());
        }
        else{
            intent.putExtra("gameName", ((TextView)((LinearLayout)((LinearLayout)((RelativeLayout) view.getParent()).getParent()).getChildAt(0)).getChildAt(0)).getText());
        }
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    public void goToMainMenu(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    public void goToDifficulty(View view){
        Intent intent = new Intent(getApplicationContext(), DifficultyActivity.class);
        if(currentExtendedRow == 1){
            intent = new Intent(getApplicationContext(), SizeActivityForTwoSizedGames.class);
        }
        else if(currentExtendedRow % 2 == 1){
            intent.putExtra("gameName", ((TextView)((LinearLayout)((LinearLayout) view.getParent()).getChildAt(0)).getChildAt(1)).getText());
        }
        else{
            intent.putExtra("gameName", ((TextView)((LinearLayout)((LinearLayout) view.getParent()).getChildAt(0)).getChildAt(0)).getText());
        }
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    public void extendListItem(View view){
        int rowNum = Integer.parseInt(view.getTag().toString());
        if(rowNum<7) {
            if (currentExtendedRow == 0) {
                final LinearLayout ll = (LinearLayout) view.getParent();
                RelativeLayout rl = (RelativeLayout) ll.getChildAt(1);
                ll.setBackground(getResources().getDrawable(R.drawable.rounded_yellowish_bg));
                rl.setVisibility(View.VISIBLE);
                currentExtendedRow = rowNum;
//            Log.i("rowNum/childCount",rowNum+" / "+((LinearLayout)ll.getParent()).getChildCount());

                final ScrollView scrollView = findViewById(R.id.scrollView_gl);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.smoothScrollTo(0, ll.getBottom() - scrollView.getBottom());
                    }
                });

            } else if (currentExtendedRow == rowNum) {
                LinearLayout ll = (LinearLayout) view.getParent();
                RelativeLayout rl = (RelativeLayout) ll.getChildAt(1);
                ll.setBackground(getResources().getDrawable(R.drawable.rounded_f7f5fa_bg));
                rl.setVisibility(View.GONE);
                currentExtendedRow = 0;
            } else {
                final LinearLayout ll = (LinearLayout) ((LinearLayout) ((LinearLayout) view.getParent()).getParent()).getChildAt(currentExtendedRow);
                RelativeLayout rl = (RelativeLayout) ll.getChildAt(1);
                ll.setBackground(getResources().getDrawable(R.drawable.rounded_f7f5fa_bg));
                rl.setVisibility(View.GONE);
                final LinearLayout ll_2 = (LinearLayout) view.getParent();
                RelativeLayout rl_2 = (RelativeLayout) ll_2.getChildAt(1);
                ll_2.setBackground(getResources().getDrawable(R.drawable.rounded_yellowish_bg));
                rl_2.setVisibility(View.VISIBLE);
                currentExtendedRow = rowNum;
                Log.i("rowNum/childCount", rowNum + " / " + ((LinearLayout) ll.getParent()).getChildCount());
                final ScrollView scrollView = findViewById(R.id.scrollView_gl);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.smoothScrollTo(0, ll_2.getBottom() - scrollView.getBottom());
                    }
                });
            }
        }
    }

    public void transferBests(JSONObject jo) throws JSONException {
        SharedPreferences sP = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);


        sP.edit().putString("ScoreSudoku.6.Easy",Integer.toString(jo.getJSONArray("Sudoku.6.Easy").getInt(0))).apply();
        sP.edit().putString("ScoreSudoku.6.Medium",Integer.toString(jo.getJSONArray("Sudoku.6.Medium").getInt(0))).apply();
        sP.edit().putString("ScoreSudoku.6.Hard",Integer.toString(jo.getJSONArray("Sudoku.6.Hard").getInt(0))).apply();
        sP.edit().putString("ScoreSudoku.9.Easy",Integer.toString(jo.getJSONArray("Sudoku.9.Easy").getInt(0))).apply();
        sP.edit().putString("ScoreSudoku.9.Medium",Integer.toString(jo.getJSONArray("Sudoku.9.Medium").getInt(0))).apply();
        sP.edit().putString("ScoreSudoku.9.Hard",Integer.toString(jo.getJSONArray("Sudoku.9.Hard").getInt(0))).apply();

        sP.edit().putString("ScoreHazineAvi.Easy",Integer.toString(jo.getJSONArray("HazineAvi.5").getInt(0))).apply();
        sP.edit().putString("ScoreHazineAvi.Medium",Integer.toString(jo.getJSONArray("HazineAvi.8").getInt(0))).apply();
        sP.edit().putString("ScoreHazineAvi.Hard",Integer.toString(jo.getJSONArray("HazineAvi.10").getInt(0))).apply();

        sP.edit().putString("ScorePatika.Easy",Integer.toString(jo.getJSONArray("Patika.5").getInt(0))).apply();
        sP.edit().putString("ScorePatika.Medium",Integer.toString(jo.getJSONArray("Patika.7").getInt(0))).apply();
        sP.edit().putString("ScorePatika.Hard",Integer.toString(jo.getJSONArray("Patika.9").getInt(0))).apply();

        sP.edit().putString("ScoreSayiBulmaca.Easy",Integer.toString(jo.getJSONArray("SayiBulmaca.3").getInt(0))).apply();
        sP.edit().putString("ScoreSayiBulmaca.Medium",Integer.toString(jo.getJSONArray("SayiBulmaca.4").getInt(0))).apply();
        sP.edit().putString("ScoreSayiBulmaca.Hard",Integer.toString(jo.getJSONArray("SayiBulmaca.5").getInt(0))).apply();

        sP.edit().putString("ScoreSozcukTuru.Easy",Integer.toString(jo.getJSONArray("SozcukTuru.Easy").getInt(0))).apply();
        sP.edit().putString("ScoreSozcukTuru.Medium",Integer.toString(jo.getJSONArray("SozcukTuru.Medium").getInt(0))).apply();
        sP.edit().putString("ScoreSozcukTuru.Hard",Integer.toString(jo.getJSONArray("SozcukTuru.Hard").getInt(0))).apply();
        sP.edit().putString("ScoreSozcukTuru.VeryHard",Integer.toString(jo.getJSONArray("SozcukTuru.Hardest").getInt(0))).apply();

        sP.edit().putString("ScorePiramit.Easy",Integer.toString(jo.getJSONArray("Piramit.3").getInt(0))).apply();
        sP.edit().putString("ScorePiramit.Medium",Integer.toString(jo.getJSONArray("Piramit.4").getInt(0))).apply();
        sP.edit().putString("ScorePİramit.Hard",Integer.toString(jo.getJSONArray("Piramit.5").getInt(0))).apply();
        sP.edit().putString("ScorePiramit.VeryHard",Integer.toString(jo.getJSONArray("Piramit.6").getInt(0))).apply();




        sP.edit().putString("BestSudoku.6.Easy",Integer.toString(jo.getJSONArray("Sudoku.6.Easy").getInt(1))).apply();
        sP.edit().putString("BestSudoku.6.Medium",Integer.toString(jo.getJSONArray("Sudoku.6.Medium").getInt(1))).apply();
        sP.edit().putString("BestSudoku.6.Hard",Integer.toString(jo.getJSONArray("Sudoku.6.Hard").getInt(1))).apply();
        sP.edit().putString("BestSudoku.9.Easy",Integer.toString(jo.getJSONArray("Sudoku.9.Easy").getInt(1))).apply();
        sP.edit().putString("BestSudoku.9.Medium",Integer.toString(jo.getJSONArray("Sudoku.9.Medium").getInt(1))).apply();
        sP.edit().putString("BestSudoku.9.Hard",Integer.toString(jo.getJSONArray("Sudoku.9.Hard").getInt(1))).apply();

        sP.edit().putString("BestHazineAvi.Easy",Integer.toString(jo.getJSONArray("HazineAvi.5").getInt(1))).apply();
        sP.edit().putString("BestHazineAvi.Medium",Integer.toString(jo.getJSONArray("HazineAvi.8").getInt(1))).apply();
        sP.edit().putString("BestHazineAvi.Hard",Integer.toString(jo.getJSONArray("HazineAvi.10").getInt(1))).apply();

        sP.edit().putString("BestPatika.Easy",Integer.toString(jo.getJSONArray("Patika.5").getInt(1))).apply();
        sP.edit().putString("BestPatika.Medium",Integer.toString(jo.getJSONArray("Patika.7").getInt(1))).apply();
        sP.edit().putString("BestPatika.Hard",Integer.toString(jo.getJSONArray("Patika.9").getInt(1))).apply();

        sP.edit().putString("BestSayiBulmaca.Easy",Integer.toString(jo.getJSONArray("SayiBulmaca.3").getInt(1))).apply();
        sP.edit().putString("BestSayiBulmaca.Medium",Integer.toString(jo.getJSONArray("SayiBulmaca.4").getInt(1))).apply();
        sP.edit().putString("BestSayiBulmaca.Hard",Integer.toString(jo.getJSONArray("SayiBulmaca.5").getInt(1))).apply();

        sP.edit().putString("BestSozcukTuru.Easy",Integer.toString(jo.getJSONArray("SozcukTuru.Easy").getInt(1))).apply();
        sP.edit().putString("BestSozcukTuru.Medium",Integer.toString(jo.getJSONArray("SozcukTuru.Medium").getInt(1))).apply();
        sP.edit().putString("BestSozcukTuru.Hard",Integer.toString(jo.getJSONArray("SozcukTuru.Hard").getInt(1))).apply();
        sP.edit().putString("BestSozcukTuru.VeryHard",Integer.toString(jo.getJSONArray("SozcukTuru.Hardest").getInt(1))).apply();

        sP.edit().putString("BestPiramit.Easy",Integer.toString(jo.getJSONArray("Piramit.3").getInt(1))).apply();
        sP.edit().putString("BestPiramit.Medium",Integer.toString(jo.getJSONArray("Piramit.4").getInt(1))).apply();
        sP.edit().putString("BestPiramit.Hard",Integer.toString(jo.getJSONArray("Piramit.5").getInt(1))).apply();
        sP.edit().putString("BestPiramit.VeryHard",Integer.toString(jo.getJSONArray("Piramit.6").getInt(1))).apply();



        Log.i("allSPs", sP.getString("ScoreHazineAvi.Easy","")+" / "+sP.getString("BestHazineAvi.Easy",""));
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    public class GetRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringBuilder result = new StringBuilder();
                SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
                String id = sharedPreferences.getString("id", "non");
                URL reqURL;
                reqURL = new URL(strings[0] + "?" + "Info=" + id + "&Token=" + strings[1]);
                Log.i("sentUrl",reqURL+"");
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
            //noinspection deprecation
            super.onPostExecute(result);

//            JSONObject jsonObject = null;
            try {
                org.json.JSONObject jb = new org.json.JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));
                transferBests(jb);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    public class PutRequest extends AsyncTask<String, Void, String> {

        RequestQueue requestQueue;
        String result = null;
        String responseMessage = null;
        @Override
        protected String doInBackground(String... strings) {
            try {
//                Map<String,String> info = new HashMap<>();
//
//                info.put("Id", strings[3]);
//
////                Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(strings[4]);
//                info.put("Query",strings[4]);
//                info.put("Ids",strings[5]);
//                result = "{\"Info\":" + (new JSONObject(info)).toString() + ", \"Token\":"+ "\""+strings[2]+ "\"}";
                result = "{\"Info\":"+ "{\"Id\":\"" + strings[3] + "\", \"Query\":\"" + strings[4] + "\", \"Ids\":"+ new JSONArray(strings[5]) + "}, \"Token\":"+ "\""+strings[2]+ "\"}";

                Log.i("request",result);
                String URL = strings[0]+strings[1];
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                StringRequest stringRequest = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            Log.i("getReq","in putReq onresponse");
                            GetRequest getRequest = new GetRequest();
                            //noinspection deprecation
                            getRequest.execute("https://akiloyunlariapp.herokuapp.com/userBest","fx!Ay:;<p6Q?C8N{");


                        } catch (Exception e){
                            e.printStackTrace();
                        }

                        try {
                            JSONObject objres = new JSONObject(result);
                            Log.i("objres",objres+"");
                            responseMessage = response;



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i("Volley", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley",error.getMessage()+"");

                        try{
                            Log.i("getReq","in putReq onErrorResponse");
                            GetRequest getRequest = new GetRequest();
                            //noinspection deprecation
                            getRequest.execute("https://akiloyunlariapp.herokuapp.com/userBest","fx!Ay:;<p6Q?C8N{");


                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public byte[] getBody() {
                        return result == null ? null: result.getBytes(StandardCharsets.UTF_8);
                    }
                };
                requestQueue.add(stringRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        if(message!=null){
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        findViewById(R.id.pentominoLL_GL).setEnabled(false);
        findViewById(R.id.anagramLL_GL).setEnabled(false);


        try{
            Log.i("getReq","in onCreate");
            GetRequest getRequest = new GetRequest();
            //noinspection deprecation
            getRequest.execute("https://akiloyunlariapp.herokuapp.com/userBest","fx!Ay:;<p6Q?C8N{");


        } catch (Exception e){
            e.printStackTrace();
        }

        try{
            SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp", MODE_PRIVATE);
            String id = sharedPreferences.getString("id", "non");
            Map<String, ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));
            Log.i("solvedQuestions1",solvedQuestions+"");
            assert solvedQuestions != null;
            for(String s : solvedQuestions.keySet()) {
                if(Objects.requireNonNull(solvedQuestions.get(s)).size() == 0)
                    continue;
                PutRequest putRequest = new PutRequest();
                //noinspection deprecation
                putRequest.execute("https://akiloyunlariapp.herokuapp.com/user", "Update", "fx!Ay:;<p6Q?C8N{", id, s, new ArrayList<>(new HashSet<>(Objects.requireNonNull(solvedQuestions.get(s)))).toString());
                Objects.requireNonNull(solvedQuestions.get(s)).clear();
            }
            sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize((Serializable) solvedQuestions)).apply();
            Log.i("solvedQuestions2",solvedQuestions+"");


        } catch (Exception e){
            e.printStackTrace();

            try{
                Log.i("getReq","in catch");
                GetRequest getRequest = new GetRequest();
                //noinspection deprecation
                getRequest.execute("https://akiloyunlariapp.herokuapp.com/userBest","fx!Ay:;<p6Q?C8N{");


            } catch (Exception e2){
                e2.printStackTrace();
            }

        }//putRequest and getRequest


    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}