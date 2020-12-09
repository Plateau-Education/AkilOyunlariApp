package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    public void goToGameList(View view){
        Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    public void checkSavedQuestions() throws IOException {
        SharedPreferences sP = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        if(!"none".equals(sP.getString("Sudoku.6.Easy","none"))){
            sP.edit().putString("IDSudoku.6.Easy",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSudoku.6.Medium",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSudoku.6.Hard",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSudoku.9.Easy",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSudoku.9.Medium",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSudoku.9.Hard",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDHazineAvi.5",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDHazineAvi.8",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDHazineAvi.10",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPatika.5",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPatika.7",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPatika.9",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSayiBulmaca.3",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSayiBulmaca.4",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSayiBulmaca.5",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSozcukTuru.Easy",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSozcukTuru.Medium",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSozcukTuru.Hard",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDSozcukTuru.Hardest",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPiramit.3",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPiramit.4",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPiramit.5",ObjectSerializer.serialize(new ArrayList<String>())).apply();
            sP.edit().putString("IDPiramit.6",ObjectSerializer.serialize(new ArrayList<String>())).apply();

            sP.edit().putString("Sudoku.6.Easy",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Sudoku.6.Medium",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Sudoku.6.Hard",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Sudoku.9.Easy",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Sudoku.9.Medium",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Sudoku.9.Hard",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("HazineAvi.5",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("HazineAvi.8",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("HazineAvi.10",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Patika.5",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Patika.7",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Patika.9",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SayiBulmaca.3",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SayiBulmaca.4",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SayiBulmaca.5",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SozcukTuru.Easy",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SozcukTuru.Medium",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SozcukTuru.Hard",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("SozcukTuru.Hardest",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Piramit.3",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Piramit.4",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Piramit.5",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
            sP.edit().putString("Piramit.6",ObjectSerializer.serialize(new ArrayList<Integer>())).apply();
        }
        Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sP.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));
        assert solvedQuestions != null;
        Log.i("solveque",solvedQuestions+"\n  "+solvedQuestions.size());
        if(solvedQuestions.size()<5){
            solvedQuestions = new HashMap<>();
            solvedQuestions.put("Sudoku.6.Easy",new ArrayList<String>());
            solvedQuestions.put("Sudoku.6.Medium",new ArrayList<String>());
            solvedQuestions.put("Sudoku.6.Hard",new ArrayList<String>());
            solvedQuestions.put("Sudoku.9.Easy",new ArrayList<String>());
            solvedQuestions.put("Sudoku.9.Medium",new ArrayList<String>());
            solvedQuestions.put("Sudoku.9.Hard",new ArrayList<String>());
            solvedQuestions.put("HazineAvi.5",new ArrayList<String>());
            solvedQuestions.put("HazineAvi.8",new ArrayList<String>());
            solvedQuestions.put("HazineAvi.10",new ArrayList<String>());
            solvedQuestions.put("Patika.5",new ArrayList<String>());
            solvedQuestions.put("Patika.7",new ArrayList<String>());
            solvedQuestions.put("Patika.9",new ArrayList<String>());
            solvedQuestions.put("SayiBulmaca.3",new ArrayList<String>());
            solvedQuestions.put("SayiBulmaca.4",new ArrayList<String>());
            solvedQuestions.put("SayiBulmaca.5",new ArrayList<String>());
            solvedQuestions.put("SozcukTuru.Easy",new ArrayList<String>());
            solvedQuestions.put("SozcukTuru.Medium",new ArrayList<String>());
            solvedQuestions.put("SozcukTuru.Hard",new ArrayList<String>());
            solvedQuestions.put("SozcukTuru.Hardest",new ArrayList<String>());
            solvedQuestions.put("Piramit.3",new ArrayList<String>());
            solvedQuestions.put("Piramit.4",new ArrayList<String>());
            solvedQuestions.put("Piramit.5",new ArrayList<String>());
            solvedQuestions.put("Piramit.6",new ArrayList<String>());

            sP.edit().putString("SolvedQuestions",ObjectSerializer.serialize((Serializable) solvedQuestions)).apply();
        }
    }

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
                        try {
                            JSONObject objres = new JSONObject(result);
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
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return result == null ? null: result.getBytes("utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            return null;
                        }
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
        setContentView(R.layout.activity_main);
        try {
            checkSavedQuestions();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp", MODE_PRIVATE);
            String id = sharedPreferences.getString("id", "non");
            Map<String,ArrayList<String>> solvedQuestions = (Map<String, ArrayList<String>>) ObjectSerializer.deserialize(sharedPreferences.getString("SolvedQuestions", ObjectSerializer.serialize(new HashMap<>())));
            Log.i("solvedQuestions1",solvedQuestions+"");
            assert solvedQuestions != null;
            for(String s : solvedQuestions.keySet()) {
                if(Objects.requireNonNull(solvedQuestions.get(s)).size() == 0)
                    continue;
                PutRequest putRequest = new PutRequest();
                //noinspection deprecation
                putRequest.execute("https://akiloyunlariapp.herokuapp.com/user", "Update", "fx!Ay:;<p6Q?C8N{", id, s, Objects.requireNonNull(solvedQuestions.get(s)).toString());
                Objects.requireNonNull(solvedQuestions.get(s)).clear();
            }
            sharedPreferences.edit().putString("SolvedQuestions", ObjectSerializer.serialize((Serializable) solvedQuestions)).apply();
            Log.i("solvedQuestions2",solvedQuestions+"");




        } catch (Exception e){
            e.printStackTrace();
        }

//        try{
//            Intent intent = getIntent();
//            if(Objects.equals(intent.getStringExtra("Logged in"), "Logged in")){
//                Log.i("loggedin","loggedin");
//            } else {
//                Intent intent1 = new Intent(getApplicationContext(),LoginActivity.class);
//                startActivity(intent1);
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//            Intent intent1 = new Intent(getApplicationContext(),LoginActivity.class);
//            startActivity(intent1);
//        }

    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }


}