package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class MyClassActivity extends AppCompatActivity {


    LoadingDialog loadingDialog;

    public void goToMainMenu(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    public void goToStudentStats(View view){
        if(!((ConstraintLayout)view).getChildAt(1).getTag().toString().equals("Instructor")) {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            intent.putExtra("id", ((ConstraintLayout) view).getChildAt(1).getTag().toString());
            intent.putExtra("displayname", ((TextView) ((ConstraintLayout) view).getChildAt(1)).getText());
            intent.putExtra("username", ((TextView) ((ConstraintLayout) view).getChildAt(2)).getText().toString().substring(1));

            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        }
    }

    public void joinClicked(View view){
        try{
            loadingDialogFunc();
            PutRequest putRequest = new PutRequest();
            SharedPreferences sP = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
            //noinspection deprecation
            putRequest.execute("https://akiloyunlariapp.herokuapp.com/class", "Update", "fx!Ay:;<p6Q?C8N{", sP.getString("id","none"), sP.getString("displayname","none"), sP.getString("username","none"), ((EditText)findViewById(R.id.classCodeEditText)).getText().toString());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    public class GetRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringBuilder result = new StringBuilder();
                SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
                String classid = sharedPreferences.getString("classid", "None");
                URL reqURL;
                reqURL = new URL(strings[0] + "?" + "Info={\"ClassId\":\""+ classid +"\"}" + "&Token=" + strings[1]);
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
                JSONObject jb = new JSONObject(result);
                addRows(jb.getJSONObject("Instructor"),jb.getJSONArray("Students"));
                SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
                String classid = sharedPreferences.getString("classid", "None");
                ((TextView) findViewById(R.id.codeTV_cl)).setText("Code: "+classid);
                findViewById(R.id.classScrollView).setVisibility(View.VISIBLE);
                findViewById(R.id.classSearchLL).setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            loadingDialog.dismissDialog();
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    public class PutRequest extends AsyncTask<String, Void, String> {

        RequestQueue requestQueue;
        String result = null;
        String responseMessage = null;
        @Override
        protected String doInBackground(final String... strings) {
            try {
                result = "{\"Info\":"+ "{\"Id\":\"" + strings[3] + "\", \"Displayname\":\"" + strings[4] + "\", \"Username\":\""+ strings[5] + "\", \"ClassId\":\""+ strings[6] + "\"}, \"Token\":"+ "\""+strings[2]+ "\"}";
                Log.i("request",result);
                String URL = strings[0]+strings[1];
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                StringRequest stringRequest = new StringRequest(Request.Method.PUT, URL, new Response.Listener<String>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(String response) {
                        try {
                            if(response.contains("Not Found")) {
                                Toast.makeText(MyClassActivity.this, getString(R.string.CouldntFind), Toast.LENGTH_SHORT).show();
                                ((EditText)findViewById(R.id.classCodeEditText)).setText("");
                                View view = getCurrentFocus();
                                if (view != null) {
                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }

                            }
                            responseMessage = response;
                            JSONObject jb = new JSONObject(response);
                            Log.i("jb",jb+"");
                            addRows(jb.getJSONObject("Instructor"), jb.getJSONArray("Students"));
                            loadingDialog.dismissDialog();
                            findViewById(R.id.classScrollView).setVisibility(View.VISIBLE);
                            findViewById(R.id.classSearchLL).setVisibility(View.GONE);
                            ((TextView) findViewById(R.id.codeTV_cl)).setText("Code: "+strings[6]);
                            SharedPreferences sP = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
                            sP.edit().putString("classid",strings[6]).apply();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i("Volley", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingDialog.dismissDialog();
                        try {
                            if (error.networkResponse.statusCode == 404) {
                                Toast.makeText(MyClassActivity.this, getString(R.string.CouldntFind), Toast.LENGTH_SHORT).show();
                                ((EditText) findViewById(R.id.classCodeEditText)).setText("");
                                View view = getCurrentFocus();
                                if (view != null) {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        Log.e("Volley",error.getMessage()+"");
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


    @SuppressLint("SetTextI18n")
    private void addRows(JSONObject teacherArray, JSONArray studentsArray) throws JSONException {
        final LayoutInflater inflater = getLayoutInflater();
        final LinearLayout linearLayout = findViewById(R.id.classLL);
        int offset = linearLayout.indexOfChild(findViewById(R.id.instructorTV_cl));

        ConstraintLayout studentCL = (ConstraintLayout) inflater.inflate(this.getResources().getIdentifier("student_row_class", "layout", this.getPackageName()),null);
        ((TextView)studentCL.getChildAt(1)).setText(teacherArray.getString("displayname"));
        studentCL.getChildAt(1).setTag("Instructor");

        ((TextView)studentCL.getChildAt(2)).setText("@"+teacherArray.getString("username"));
        linearLayout.addView(studentCL,offset+1);

        for(int i = 0; i<studentsArray.length(); i++){
            JSONObject studentInfo = studentsArray.getJSONObject(i);
            String studentDname = studentInfo.getString("displayname");
            String studentUsername = studentInfo.getString("username");
            String studentId = studentInfo.getString("id");
            studentCL = (ConstraintLayout) inflater.inflate(this.getResources().getIdentifier("student_row_class", "layout", this.getPackageName()),null);
            View space = inflater.inflate(this.getResources().getIdentifier("space_view", "layout", this.getPackageName()),null);
            space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (10 * getResources().getDisplayMetrics().density)));
            ((TextView)studentCL.getChildAt(1)).setText(studentDname);
            studentCL.getChildAt(1).setTag(studentId);
            ((TextView)studentCL.getChildAt(2)).setText("@"+studentUsername);
            linearLayout.addView(studentCL,i*2+3+offset);
            linearLayout.addView(space,i*2+4+offset);
        }
    }

    @SuppressLint("InflateParams")
    public void loadingDialogFunc(){
        loadingDialog = new LoadingDialog(MyClassActivity.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
        loadingDialog.startLoadingAnimation();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_class);
        SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        Log.i("classid", Objects.requireNonNull(sharedPreferences.getString("classid", "None")));
        if(!Objects.equals(sharedPreferences.getString("classid", "None"), "None")){

            try{
                loadingDialogFunc();
                GetRequest getRequest = new GetRequest();
                //noinspection deprecation
                getRequest.execute("https://akiloyunlariapp.herokuapp.com/classGet","fx!Ay:;<p6Q?C8N{");
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            findViewById(R.id.classSearchLL).setVisibility(View.VISIBLE);
            findViewById(R.id.classScrollView).setVisibility(View.GONE);
        }
    }
}