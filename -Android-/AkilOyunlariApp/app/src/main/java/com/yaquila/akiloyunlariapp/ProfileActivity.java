package com.yaquila.akiloyunlariapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    String profileInfoState = "Info";
    ConstraintLayout statsCl;
    ConstraintLayout infoCl;
    LinearLayout scrollViewLL;

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
//                drawGraph("Sudoku",jb);
                drawGraph("Patika",jb);



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

    LineGraphSeries<DataPoint> series1;
    LineGraphSeries<DataPoint> series2;
    LineGraphSeries<DataPoint> series3;
    PointsGraphSeries<DataPoint> pseries1;
    PointsGraphSeries<DataPoint> pseries2;
    PointsGraphSeries<DataPoint> pseries3;
    List<Integer> pxs = new ArrayList<>();

    public void drawGraph(String gameName, JSONObject jb) throws JSONException {
        if(gameName.contains("Sudoku")){

            JSONArray sudoku6Easy = jb.getJSONArray("Sudoku.6.Easy");
            JSONArray sudoku6Medium = jb.getJSONArray("Sudoku.6.Medium");
            JSONArray sudoku6Hard = jb.getJSONArray("Sudoku.6.Hard");
            JSONArray sudoku9Easy = jb.getJSONArray("Sudoku.9.Easy");
            JSONArray sudoku9Medium = jb.getJSONArray("Sudoku.9.Medium");
            JSONArray sudoku9Hard = jb.getJSONArray("Sudoku.9.Hard");

            double x,y;
            x = 0;
            GraphView graphView = statsCl.findViewWithTag(gameName+"G");
            series1 = new LineGraphSeries<>();
            series1.appendData(new DataPoint(0,0),true,100);
//            int numDataPoints = 20;
            for(int i = 0; i < sudoku6Easy.length(); i++){
                x = (double) sudoku6Easy.getJSONArray(i).getInt(1);
                y = (double) sudoku6Easy.getJSONArray(i).getInt(0);
                series1.appendData(new DataPoint(x,y),true,100);
                series1.setColor(getResources().getColor(R.color.f7f5fa));
            }
            graphView.setBackgroundColor(getResources().getColor(R.color.near_black_blue));
            graphView.addSeries(series1);
        }

        else if(gameName.contains("Patika")){

            JSONArray patika5 = jb.getJSONArray("Patika.5");
            JSONArray patika7= jb.getJSONArray("Patika.7");
            JSONArray patika9 = jb.getJSONArray("Patika.9");



            double x,y;
            x = 0;
            GraphView graphView = statsCl.findViewWithTag(gameName+"G");
            series1 = new LineGraphSeries<>();
            series2 = new LineGraphSeries<>();
            series3 = new LineGraphSeries<>();
            pseries1 = new PointsGraphSeries<>();
            pseries2 = new PointsGraphSeries<>();
            pseries3 = new PointsGraphSeries<>();
            LineGraphSeries<DataPoint> s2 = new LineGraphSeries<>();
            s2.appendData(new DataPoint(0,0),true,100);
//            s2.appendData(new DataPoint(50,0),true,100);

            s2.setColor(getResources().getColor(R.color.transparent));
            series1.appendData(new DataPoint(0,0),true,100);
            series2.appendData(new DataPoint(0,0),true,100);
            series3.appendData(new DataPoint(0,0),true,100);
            pxs.add(0);
//            int numDataPoints = 20;
            for(int i = 0; i < patika5.length(); i++){
                x = (double) patika5.getJSONArray(i).getInt(1);
                y = (double) patika5.getJSONArray(i).getInt(0);
                series1.appendData(new DataPoint(x,y),true,100);
                Log.i("series1",series1.getHighestValueX()+"");
                series1.setColor(getResources().getColor(R.color.f7f5fa));
                pseries1.appendData(new DataPoint(x,y),true,100);
                pseries1.setColor(Color.WHITE);
                pseries1.setSize(10f);
                if(!pxs.contains((int)x)){
                    pxs.add((int)x);
                }
            }
            for(int i = 0; i < patika7.length(); i++){
                x = (double) patika7.getJSONArray(i).getInt(1);
                y = (double) patika7.getJSONArray(i).getInt(0);
                series2.appendData(new DataPoint(x,y),true,100);
                Log.i("series2",series2.getHighestValueX()+"");
                series2.setColor(getResources().getColor(R.color.light_red));
                pseries2.appendData(new DataPoint(x,y),true,100);
                pseries2.setColor(getResources().getColor(R.color.dark_red));
                pseries2.setSize(10f);
                if(!pxs.contains((int)x)){
                    pxs.add((int)x);
                }
            }
            for(int i = 0; i < patika9.length(); i++){
                x = (double) patika9.getJSONArray(i).getInt(1);
                y = (double) patika9.getJSONArray(i).getInt(0);
                series3.appendData(new DataPoint(x,y),true,100);
                Log.i("series3",series3.getHighestValueX()+"");
                series3.setColor(getResources().getColor(R.color.light_blue_green));
                pseries3.appendData(new DataPoint(x,y),true,100);
                pseries3.setColor(getResources().getColor(R.color.dark_blue_green));
                pseries3.setSize(10f);
                if(!pxs.contains((int)x)){
                    pxs.add((int)x);
                }
            }
            Collections.sort(pxs);
            List<String> newpxs = new ArrayList<>();
            for(int i : pxs){
                newpxs.add(Integer.toString(i));
            }
            s2.appendData(new DataPoint((double)pxs.get(pxs.size()-1)+5,0),true,100);

            graphView.setBackgroundColor(getResources().getColor(R.color.near_black_blue));
            graphView.addSeries(series1);
            graphView.addSeries(series2);
            graphView.addSeries(series3);
            graphView.addSeries(s2);
            graphView.addSeries(pseries1);
            graphView.addSeries(pseries2);
            graphView.addSeries(pseries3);

            graphView.setTitle(getString(R.string.avgTime));

            // activate horizontal and vertical zooming and scrolling
//            graphView.getViewport().setScalableY(true);

            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
            staticLabelsFormatter.setHorizontalLabels(newpxs.toArray(new String[0]));
//            staticLabelsFormatter.setVerticalLabels(new String[] {"0","15", "30", "45", "60","75", "90"});
            graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

//            NumberFormat nf = NumberFormat.getInstance();
//            nf.setMaximumIntegerDigits(2);
//
//            graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
//                @Override
//                public String formatLabel(double value, boolean isValueX) {
//                    if (isValueX) {
//                        // show normal x values
//                        Log.i("normalX",value+"");
//                        return super.formatLabel(value, isValueX);
//                    } else {
//                        // show currency for y values
//                        Log.i("currencyY",value+"");
//                        return super.formatLabel(value, isValueX)+"s";
//                    }
//                }
//            });
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

        GetRequest getRequest = new GetRequest();
        getRequest.execute("https://akiloyunlariapp.herokuapp.com","fx!Ay:;<p6Q?C8N{");

    }
}