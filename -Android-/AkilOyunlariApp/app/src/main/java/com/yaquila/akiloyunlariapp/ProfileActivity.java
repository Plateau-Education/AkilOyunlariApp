package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    String profileInfoState = "Info";
    ConstraintLayout statsCl;
    ConstraintLayout infoCl;
    LinearLayout scrollViewLL;

    @SuppressLint("StaticFieldLeak")
    public class GetRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringBuilder result = new StringBuilder();
                SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
                String id = sharedPreferences.getString("id", "non");
                URL reqURL;
                reqURL = new URL(strings[0] + "/userGet" + "?Info=" + id + "&Token=" + strings[1]);

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

//            JSONObject jsonObject = null;
            try {
                org.json.JSONObject jb = new org.json.JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));

            } catch (Exception e) {
                e.printStackTrace();
            }

//            loadingDialog.dismissDialog();
        }
    }

    public void profileInfoChange(View view){
        TextView statsTV = findViewById(R.id.stats_tabTV);
        TextView infoTV = findViewById(R.id.info_tabTV);

        if(profileInfoState.equals("Info") && view.getId()==R.id.stats_tabTV){
            profileInfoState = "Stats";
            statsTV.setTextColor(getResources().getColor(R.color.light_blue_green));
            statsTV.setBackground(getResources().getDrawable(R.drawable.more_rounded_f7f5fa_bg));
            infoTV.setTextColor(getResources().getColorStateList(R.color.tab_selector_tvcolor));
            infoTV.setBackground(getResources().getDrawable(R.drawable.tab_selector_bg));

            scrollViewLL.removeView(infoCl);
            scrollViewLL.addView(statsCl);
        }
        else if(profileInfoState.equals("Stats") && view.getId() == R.id.info_tabTV){
            profileInfoState = "Info";
            infoTV.setTextColor(getResources().getColor(R.color.light_blue_green));
            infoTV.setBackground(getResources().getDrawable(R.drawable.more_rounded_f7f5fa_bg));
            statsTV.setTextColor(getResources().getColorStateList(R.color.tab_selector_tvcolor));
            statsTV.setBackground(getResources().getDrawable(R.drawable.tab_selector_bg));

            scrollViewLL.removeView(statsCl);
            scrollViewLL.addView(infoCl);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        scrollViewLL = findViewById(R.id.scrollViewLL);

        LayoutInflater inflater = getLayoutInflater();
        statsCl = (ConstraintLayout) inflater.inflate(this.getResources().getIdentifier("stats_layout", "layout", this.getPackageName()),null);
        infoCl = (ConstraintLayout) inflater.inflate(this.getResources().getIdentifier("info_layout", "layout", this.getPackageName()),null);

        SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);
        ((TextView) infoCl.findViewById(R.id.nameAndSurnameTV2)).setText(sharedPreferences.getString("displayname","Unknown"));
        ((TextView) infoCl.findViewById(R.id.usernameTV2)).setText(sharedPreferences.getString("username","Unknown"));
        ((TextView) infoCl.findViewById(R.id.emailTV2)).setText(sharedPreferences.getString("email","Unknown"));

        scrollViewLL.addView(infoCl);

//        GetRequest getRequest = new GetRequest();
//        getRequest.execute("https://akiloyunlariapp.herokuapp.com","fx!Ay:;<p6Q?C8N{");

    }
}