package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.view.View.GONE;

public class MyClassActivity extends AppCompatActivity {


    LoadingDialog loadingDialog;
    LinearLayout membersLL;
    LinearLayout tasksLL;
    LinearLayout scrollViewLL;
//    ConstraintLayout newTaskLayout;
    ConstraintLayout taskRowInstructor;
    Spinner nameSpinner;
    Spinner gameSpinner;
    Spinner diffSpinner;

    String classInfoState = "Members";
    List<String> studentNames = new ArrayList<>();
    List<String> studentIds = new ArrayList<>();
    List<String> newTaskProperties = new ArrayList<>();
    JSONArray tasks4Student;
    JSONArray tasks4Instructor = new JSONArray();

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

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void classInfoChange(View view) throws JSONException {
        TextView tasksTV = findViewById(R.id.tasks_tabTV);
        TextView membersTV = findViewById(R.id.members_tabTV);

        if(classInfoState.equals("Members") && view.getId()==R.id.tasks_tabTV){
            classInfoState = "Tasks";
            tasksTV.setTextColor(getResources().getColor(R.color.light_blue_green));
            tasksTV.setBackground(getResources().getDrawable(R.drawable.more_rounded_f7f5fa_bg));
            membersTV.setTextColor(getResources().getColorStateList(R.color.tab_selector_tvcolor));
            membersTV.setBackground(getResources().getDrawable(R.drawable.tab_selector_bg));

            scrollViewLL.removeView(membersLL);
            scrollViewLL.addView(tasksLL);

            ImageView addButton = tasksLL.findViewById(R.id.newTaskButton);
            tasksLL.removeAllViews();
            tasksLL.addView(addButton);
            if(Objects.equals(getSharedPreferences("com.yaquila.akiloyunlariapp", MODE_PRIVATE).getString("type", "None"), "Student")) {
                tasksLL.findViewById(R.id.newTaskButton).setVisibility(GONE);
                for (int i = 0; i < tasks4Student.length(); i++) {
                    JSONArray task = (JSONArray) tasks4Student.get(i);
                    ConstraintLayout taskRow = (ConstraintLayout) getLayoutInflater().inflate(this.getResources().getIdentifier("task_row_student", "layout", this.getPackageName()),null);
                    Log.i("task",visibleToDatabase("databaseToVisible",task.getString(0)));
                    ((TextView)taskRow.findViewById(R.id.gameNameTV_taskrow)).setText(visibleToDatabase("databaseToVisible",task.getString(0)));
                    ((TextView)taskRow.findViewById(R.id.progressTV_taskrow)).setText(task.getInt(2)+" / "+task.getInt(1));
                    taskRow.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    tasksLL.addView(taskRow);
                    View space = getLayoutInflater().inflate(this.getResources().getIdentifier("space_view", "layout", this.getPackageName()),null);
                    space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (10 * getResources().getDisplayMetrics().density)));
                    tasksLL.addView(space);
                }
            } else {
                tasksLL.findViewById(R.id.newTaskButton).setVisibility(View.VISIBLE);
                arrangeTasks4Instructor(tasks4Instructor);
            }
        }
        else if(classInfoState.equals("Tasks") && view.getId() == R.id.members_tabTV){
            classInfoState = "Members";
//            closeNewTask(null);
            membersTV.setTextColor(getResources().getColor(R.color.light_blue_green));
            membersTV.setBackground(getResources().getDrawable(R.drawable.more_rounded_f7f5fa_bg));
            tasksTV.setTextColor(getResources().getColorStateList(R.color.tab_selector_tvcolor));
            tasksTV.setBackground(getResources().getDrawable(R.drawable.tab_selector_bg));

            scrollViewLL.removeView(tasksLL);
            scrollViewLL.addView(membersLL);
        }
    }

    public void addNewTask(View view){
        try{
            LayoutInflater factory = LayoutInflater.from(this);
            final View ntLayout = factory.inflate(R.layout.newtask_layout, null);
            final AlertDialog ntDialog = new AlertDialog.Builder(this).create();
            ntDialog.setView(ntLayout);

            ntLayout.findViewById(R.id.sendbutton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadingDialogFunc();
                    sendNewTask(ntLayout);
                    ntDialog.dismiss();
                }
            });
            ntLayout.findViewById(R.id.closebutton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ntDialog.dismiss();
                }
            });
            ntDialog.show();

            ((EditText)ntLayout.findViewById(R.id.editTextNumber)).setText("");
            nameSpinner = ntLayout.findViewById(R.id.nameSpinner);
            gameSpinner = ntLayout.findViewById(R.id.gameSpinner);
            diffSpinner = ntLayout.findViewById(R.id.diffSpinner);
            studentNames.add(0, getString(R.string.Everyone));
            ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(this, R.layout.spinner_tv, studentNames);
            nameSpinner.setAdapter(nameAdapter);
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
                        ArrayAdapter<String> changedDiffAdapter = new ArrayAdapter<>(MyClassActivity.this, R.layout.spinner_tv,
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

    public void sendNewTask(View view){
//        scrollViewLL.addView(taskRowInstructor);
//        ((TextView)taskRowInstructor.findViewById(R.id.taskInfoTV)).setText("");

        newTaskProperties = new ArrayList<>();
        if(String.valueOf(nameSpinner.getSelectedItem()).equals(getString(R.string.Everyone))) newTaskProperties.add("all");
        else newTaskProperties.add(studentIds.get(studentNames.indexOf(String.valueOf(nameSpinner.getSelectedItem()))-1));

        newTaskProperties.add(visibleToDatabase("visibleToDatabase",gameSpinner.getSelectedItem() +" "+ diffSpinner.getSelectedItem()));
        newTaskProperties.add(String.valueOf(((EditText)view.findViewById(R.id.editTextNumber)).getText()));

        PostRequest postRequest = new PostRequest();
        //noinspection deprecation
        postRequest.execute("https://akiloyunlariapp.herokuapp.com/task" , "Send", "fx!Ay:;<p6Q?C8N{",
                newTaskProperties.get(0), newTaskProperties.get(1), newTaskProperties.get(2),
                getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE).getString("classid", "None"));

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
                if(classInfoState.equals("Members")) {
                    addRows(jb.getJSONObject("Instructor"), jb.getJSONArray("Students"));
                    SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp", MODE_PRIVATE);
                    String classid = sharedPreferences.getString("classid", "None");
                    ((TextView) findViewById(R.id.codeTV_cl)).setText(getString(R.string.Code) + ": " + classid);
                    findViewById(R.id.classScrollView).setVisibility(View.VISIBLE);
                    findViewById(R.id.classSearchLL).setVisibility(GONE);
                } else { //Yeni task oluşturulduğundaki taskları yenileme
                    Log.i("jb In else",jb.toString());
                    refreshTasks(jb.getJSONArray("Students"));
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            loadingDialog.dismissDialog();
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    public class PostRequest extends AsyncTask<String, Void, String> {

        RequestQueue requestQueue;
        String result = null;
        SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);

        @Override
        protected String doInBackground(final String... strings) {
            try {
                Map<String,String> info = new HashMap<>();

                info.put("to", strings[3]);
                info.put("game",strings[4]);
                info.put("goal",strings[5]);
                info.put("class",strings[6]);

                result = "{\"Info\":" + (new JSONObject(info)).toString() + ", \"Token\":"+ "\""+strings[2]+ "\"}";

                Log.i("request",result);
                String URL = strings[0]+strings[1];
                Log.i("URL",URL);
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject objres = new JSONObject(response);
                            Log.i("objres",objres.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i("Volley", response);
                        GetRequest getRequest = new GetRequest();
                        //noinspection deprecation
                        getRequest.execute("https://akiloyunlariapp.herokuapp.com/classGet","fx!Ay:;<p6Q?C8N{");
                    }
                }, new Response.ErrorListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", error.getMessage()+"");
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
            return sharedPreferences.getString("id","none");
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
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
                            findViewById(R.id.classSearchLL).setVisibility(GONE);
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    private void addRows(JSONObject teacherArray, JSONArray studentsArray) throws JSONException {
        final LayoutInflater inflater = getLayoutInflater();
        int offset;
        try {
            offset = membersLL.indexOfChild(findViewById(R.id.instructorTV_cl));
        } catch (Exception e){
            offset = 0;
            e.printStackTrace();
        }
        ConstraintLayout studentCL = (ConstraintLayout) inflater.inflate(this.getResources().getIdentifier("student_row_class", "layout", this.getPackageName()),null);
        ((TextView)studentCL.getChildAt(1)).setText(teacherArray.getString("displayname"));
        studentCL.getChildAt(1).setTag("Instructor");

        ((TextView)studentCL.getChildAt(2)).setText("@"+teacherArray.getString("username"));
        membersLL.addView(studentCL,offset+1);

        for(int i = 0; i<studentsArray.length(); i++){
            JSONObject studentInfo = studentsArray.getJSONObject(i);
            String studentDname = studentInfo.getString("displayname");
            studentNames.add(studentDname);
            String studentUsername = studentInfo.getString("username");
            String studentId = studentInfo.getString("id");
            studentIds.add(studentId);
            studentCL = (ConstraintLayout) inflater.inflate(this.getResources().getIdentifier("student_row_class", "layout", this.getPackageName()),null);
            View space = inflater.inflate(this.getResources().getIdentifier("space_view", "layout", this.getPackageName()),null);
            space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (10 * getResources().getDisplayMetrics().density)));
            ((TextView)studentCL.getChildAt(1)).setText(studentDname);
            studentCL.getChildAt(1).setTag(studentId);
            ((TextView)studentCL.getChildAt(2)).setText("@"+studentUsername);
            membersLL.addView(studentCL,i*2+3+offset);
            membersLL.addView(space,i*2+4+offset);

            if(Objects.equals(getSharedPreferences("com.yaquila.akiloyunlariapp", MODE_PRIVATE).getString("type", "None"), "Student")
                    && studentId.equals(getSharedPreferences("com.yaquila.akiloyunlariapp", MODE_PRIVATE).getString("id", "None"))){
                tasks4Student = (JSONArray)studentInfo.get("tasks");
            } else if(Objects.equals(getSharedPreferences("com.yaquila.akiloyunlariapp", MODE_PRIVATE).getString("type", "None"), "Instructor")){
                JSONArray tasks = (JSONArray)studentInfo.get("tasks");
                if(tasks.length()>0){
                    Object[] taskObject = new Object[2];
                    taskObject[0] = studentDname;
                    taskObject[1] = tasks;
                    tasks4Instructor.put(taskObject);
                }
            }

        }
    }

    @SuppressLint("SetTextI18n")
    public void arrangeTasks4Instructor(JSONArray tasks4Instructor) throws JSONException{
        for(int i = 0; i< tasks4Instructor.length(); i++){
            Object[] taskObject = (Object[]) tasks4Instructor.get(i);
            String studentName = (String) taskObject[0];
            JSONArray tasks = (JSONArray) taskObject[1];

            TextView studentNameTV = (TextView) getLayoutInflater().inflate(this.getResources().getIdentifier("studentname_tv", "layout", this.getPackageName()),null);
            studentNameTV.setText(studentName);
            tasksLL.addView(studentNameTV);
            View space = getLayoutInflater().inflate(this.getResources().getIdentifier("space_view", "layout", this.getPackageName()),null);
            space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (20 * getResources().getDisplayMetrics().density)));
            tasksLL.addView(space);

            for (int j = 0; j < tasks.length(); j++) {
                JSONArray task = (JSONArray) tasks.get(j);
                ConstraintLayout taskRow = (ConstraintLayout) getLayoutInflater().inflate(this.getResources().getIdentifier("task_row_student", "layout", this.getPackageName()),null);
                ((TextView)taskRow.findViewById(R.id.gameNameTV_taskrow)).setText(visibleToDatabase("databaseToVisible",task.getString(0)));
                ((TextView)taskRow.findViewById(R.id.progressTV_taskrow)).setText(task.getInt(2)+" / "+task.getInt(1));
                taskRow.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                tasksLL.addView(taskRow);
                View space2 = getLayoutInflater().inflate(this.getResources().getIdentifier("space_view", "layout", this.getPackageName()),null);
                space2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (10 * getResources().getDisplayMetrics().density)));
                tasksLL.addView(space2);

            }
        }
    }

    public void refreshTasks(JSONArray studentsArray) throws  JSONException{
        ImageView addButton = tasksLL.findViewById(R.id.newTaskButton);
        tasksLL.removeAllViews();
        tasksLL.addView(addButton);

        tasks4Instructor = new JSONArray();
        for(int i = 0; i<studentsArray.length(); i++){
            JSONObject studentInfo = studentsArray.getJSONObject(i);
            String studentDname = studentInfo.getString("displayname");
            JSONArray tasks = (JSONArray)studentInfo.get("tasks");
            if(tasks.length()>0){
                Object[] taskObject = new Object[2];
                taskObject[0] = studentDname;
                taskObject[1] = tasks;
                tasks4Instructor.put(taskObject);
            }
        }
        Log.i("tasks4Instructor",tasks4Instructor.toString());
        arrangeTasks4Instructor(tasks4Instructor);
    }

    public String visibleToDatabase(String visibleOrDatabase, String string){
        Map<String,String> visibleToDB = new HashMap<>();
        List<String> gosterilenOyunIsimleri = new ArrayList<>(Arrays.asList(
                "Sudoku 6x6 "+getString(R.string.Easy), "Sudoku 6x6 "+getString(R.string.Medium), "Sudoku 6x6 "+getString(R.string.Hard),
                "Sudoku 9x9 "+getString(R.string.Easy), "Sudoku 9x9 "+getString(R.string.Medium), "Sudoku 9x9 "+getString(R.string.Hard),
                "Hazine Avı "+getString(R.string.Easy), "Hazine Avı "+getString(R.string.Medium), "Hazine Avı "+getString(R.string.Hard),
                "Patika "+getString(R.string.Easy), "Patika "+getString(R.string.Medium), "Patika "+getString(R.string.Hard),
                "Sayı Bulmaca "+getString(R.string.Easy), "Sayı Bulmaca "+getString(R.string.Medium), "Sayı Bulmaca "+getString(R.string.Hard),
                "Sözcük Turu "+getString(R.string.Easy), "Sözcük Turu "+getString(R.string.Medium), "Sözcük Turu "+getString(R.string.Hard), "Sözcük Turu "+getString(R.string.VeryHard),
                "Piramit "+getString(R.string.Easy), "Piramit "+getString(R.string.Medium), "Piramit "+getString(R.string.Hard), "Piramit "+getString(R.string.VeryHard)));
        List<String> databaseOyunIsimleri = new ArrayList<>(Arrays.asList(
                "Sudoku.6.Easy", "Sudoku.6.Medium", "Sudoku.6.Hard", "Sudoku.9.Easy", "Sudoku.9.Medium", "Sudoku.9.Hard",
                "HazineAvi.5", "HazineAvi.8", "HazineAvi.10", "Patika.5", "Patika.7", "Patika.9",
                "SayiBulmaca.3", "SayiBulmaca.4", "SayiBulmaca.5", "SozcukTuru.Easy", "SozcukTuru.Medium", "SozcukTuru.Hard", "SozcukTuru.Hardest",
                "Piramit.3", "Piramit.4","Piramit.5","Piramit.6"));
        for(int i = 0; i<gosterilenOyunIsimleri.size(); i++)
            visibleToDB.put(gosterilenOyunIsimleri.get(i),databaseOyunIsimleri.get(i));

        if(visibleOrDatabase.equals("visibleToDatabase")){
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
        LayoutInflater inflater = getLayoutInflater();
        membersLL = (LinearLayout) inflater.inflate(this.getResources().getIdentifier("classmembers_layout", "layout", this.getPackageName()),null);
        tasksLL = (LinearLayout) inflater.inflate(this.getResources().getIdentifier("classtasks_layout", "layout", this.getPackageName()),null);
//        newTaskLayout = (ConstraintLayout) inflater.inflate(this.getResources().getIdentifier("newtask_layout", "layout", this.getPackageName()),null);
        taskRowInstructor = (ConstraintLayout) inflater.inflate(this.getResources().getIdentifier("task_row_instructor", "layout", this.getPackageName()),null);
        scrollViewLL = findViewById(R.id.classLL);

        if(!Objects.equals(sharedPreferences.getString("classid", "None"), "None")){
            scrollViewLL.addView(membersLL);
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
            findViewById(R.id.classScrollView).setVisibility(GONE);
        }
    }
}