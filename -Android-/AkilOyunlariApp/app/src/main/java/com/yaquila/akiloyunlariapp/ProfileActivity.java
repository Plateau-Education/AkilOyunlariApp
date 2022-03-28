package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;

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
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    String profileInfoState = "Info";
    ConstraintLayout statsCl;
    ConstraintLayout infoCl;
    LinearLayout scrollViewLL;

    public void goToMainMenu(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    public class GetRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringBuilder result = new StringBuilder();
                URL reqURL;
                reqURL = new URL(strings[0] + "/userGet" + "?Info=" + strings[2] + "&Token=" + strings[1]);

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
            super.onPostExecute(result);

//            JSONObject jsonObject = null;
            try {
                org.json.JSONObject jb = new org.json.JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));

                JSONObject scores = jb.getJSONObject("puan");
                List<String> games = new ArrayList<>(Arrays.asList("Sudoku.6", "Sudoku.9", "HazineAvi", "Patika", "SayiBulmaca", "SozcukTuru", "Piramit", "Sudoku"));
                for(String g: games.subList(2,games.size())){
                    ((TextView)statsCl.findViewWithTag(g+"S")).setText(getString(R.string.score)+": "+scores.getDouble(g));
                }
                for(String g2: games.subList(0,games.size()-1)){
                    drawGraph(g2,jb);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

//            loadingDialog.dismissDialog();
        }
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "UseCompatLoadingForColorStateLists"})
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

    LineGraphSeries<DataPoint> series4;
    PointsGraphSeries<DataPoint> pseries4;

    Toast mToast;
    int maxX = 0;

    @SuppressLint("DefaultLocale")
    public void drawGraph(final String gameName, JSONObject jb) throws JSONException {
        if(gameName.contains("Sudoku")) {
            String dif1,dif2,dif3;
            dif1 = ".Easy"; dif2 = ".Medium"; dif3 = ".Hard";
            final String gameSize = Character.toString(gameName.charAt(gameName.length()-1));

            JSONArray game1 = jb.getJSONArray(gameName+dif1);
            JSONArray game2= jb.getJSONArray(gameName+dif2);
            JSONArray game3 = jb.getJSONArray(gameName+dif3);

            mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
            maxX=0;
            double x,y;
            GraphView graphView = statsCl.findViewWithTag(gameName+"G");
            series1 = new LineGraphSeries<>();
            series2 = new LineGraphSeries<>();
            series3 = new LineGraphSeries<>();
            pseries1 = new PointsGraphSeries<>();
            pseries2 = new PointsGraphSeries<>();
            pseries3 = new PointsGraphSeries<>();
//            LineGraphSeries<DataPoint> s2 = new LineGraphSeries<>();
//            s2.appendData(new DataPoint(0,0),true,100);
//            s2.appendData(new DataPoint(50,0),true,100);

//            s2.setColor(getResources().getColor(R.color.transparent));
            series1.appendData(new DataPoint(0,0),true,100);
            series2.appendData(new DataPoint(0,0),true,100);
            series3.appendData(new DataPoint(0,0),true,100);
//            int numDataPoints = 20;
            for(int i = 0; i < game1.length(); i++){
                x = Double.parseDouble(String.format("%.2f",game1.getJSONArray(i).getDouble(1)).replace(",","."));
                y = Double.parseDouble(String.format("%.2f",game1.getJSONArray(i).getDouble(0)).replace(",","."));
                series1.appendData(new DataPoint(x,y),true,100);
                Log.i("series1",series1.getHighestValueX()+"");
                series1.setColor(getResources().getColor(R.color.darkgreen_T));
                pseries1.appendData(new DataPoint(x,y),true,100);
                pseries1.setColor(getResources().getColor(R.color.green_W));
                pseries1.setSize(10f);
                if(x > maxX){
                    maxX = (int)(Math.ceil(x/10)*10);
                }
            }
            series1.setTitle("Sudoku "+gameSize+"x"+gameSize+" "+getString(R.string.Easy));
            pseries1.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    mToast.cancel();
                    mToast = Toast.makeText(ProfileActivity.this, "Sudoku "+gameSize+"x"+gameSize+" "+getString(R.string.Easy)+": "+ (int)dataPoint.getX()+", "+dataPoint.getY()+"s", Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });
            for(int i = 0; i < game2.length(); i++){
                x = Double.parseDouble(String.format("%.2f",game2.getJSONArray(i).getDouble(1)).replace(",","."));
                y = Double.parseDouble(String.format("%.2f",game2.getJSONArray(i).getDouble(0)).replace(",","."));
                series2.appendData(new DataPoint(x,y),true,100);
                Log.i("series2",series2.getHighestValueX()+"");
                series2.setColor(getResources().getColor(R.color.dark_red));
                pseries2.appendData(new DataPoint(x,y),true,100);
                pseries2.setColor(getResources().getColor(R.color.light_red));
                pseries2.setSize(10f);
                if(x > maxX){
                    maxX = (int)(Math.ceil(x/10)*10);
                }
            }
            series2.setTitle("Sudoku "+gameSize+"x"+gameSize+" "+getString(R.string.Medium));
            pseries2.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    mToast.cancel();
                    mToast = Toast.makeText(ProfileActivity.this, "Sudoku "+gameSize+"x"+gameSize+" "+getString(R.string.Medium)+": "+ (int)dataPoint.getX()+", "+dataPoint.getY()+"s", Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });
            for(int i = 0; i < game3.length(); i++){
                x = Double.parseDouble(String.format("%.2f",game3.getJSONArray(i).getDouble(1)).replace(",","."));
                y = Double.parseDouble(String.format("%.2f",game3.getJSONArray(i).getDouble(0)).replace(",","."));
                series3.appendData(new DataPoint(x,y),true,100);
                Log.i("series3",series3.getHighestValueX()+"");
                series3.setColor(getResources().getColor(R.color.dark_blue_green));
                pseries3.appendData(new DataPoint(x,y),true,100);
                pseries3.setColor(getResources().getColor(R.color.light_blue_green));
                pseries3.setSize(10f);
                if(x > maxX){
                    maxX = (int)(Math.ceil(x/10)*10);
                }
            }
            series3.setTitle("Sudoku "+gameSize+"x"+gameSize+" "+getString(R.string.Hard));
            pseries3.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    mToast.cancel();
                    mToast = Toast.makeText(ProfileActivity.this, "Sudoku "+gameSize+"x"+gameSize+" "+getString(R.string.Hard)+": "+ (int) dataPoint.getX()+", "+dataPoint.getY()+"s", Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });

            graphView.setBackgroundColor(getResources().getColor(R.color.f7f5fa));
            graphView.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.near_black_blue));
            graphView.getGridLabelRenderer().setHorizontalLabelsColor(getResources().getColor(R.color.near_black_blue));
            graphView.getGridLabelRenderer().setVerticalLabelsColor(getResources().getColor(R.color.near_black_blue));

            graphView.addSeries(series1);
            graphView.addSeries(series2);
            graphView.addSeries(series3);
//            graphView.addSeries(s2);
            graphView.addSeries(pseries1);
            graphView.addSeries(pseries2);
            graphView.addSeries(pseries3);

            graphView.setTitle("Sudoku "+gameSize+"x"+gameSize+" "+getString(R.string.avgTime));
            graphView.getGridLabelRenderer().setHorizontalAxisTitle(getString(R.string.numques));
//            graphView.getGridLabelRenderer().setVerticalAxisTitle(getString(R.string.avgTime));
            graphView.getGridLabelRenderer().setHorizontalAxisTitleColor(getResources().getColor(R.color.near_black_blue));
//            graphView.getGridLabelRenderer().setVerticalAxisTitleColor(getResources().getColor(R.color.near_black_blue));
            graphView.setTitleColor(getResources().getColor(R.color.near_black_blue));
//            graphView.getLegendRenderer().setVisible(true);
//            graphView.getLegendRenderer().setAlign(TOP);

            // activate horizontal and vertical zooming and scrolling
            graphView.getViewport().setScalableY(true);


//            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
//            staticLabelsFormatter.setHorizontalLabels(new String[] {"0","5", "10", "15", "20", "25", "30","35", "40", "45","50"});
//            staticLabelsFormatter.setVerticalLabels(new String[] {"0","10", "20", "30", "40", "50", "60","70", "80", "90","100"});
//            graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(3);
//
            graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf,nf){
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        // show normal x values
//                        Log.i("normalX",value+"");
                        return super.formatLabel(value, true);
                    } else {
                        // show currency for y values
//                        Log.i("currencyY",value+"");
                        return super.formatLabel(value, false)+"s";
                    }
                }
            });

            if(maxX == 0){
                graphView.getViewport().setMaxY(10);
                graphView.getViewport().setYAxisBoundsManual(true);
                maxX = 10;
            }
            graphView.getViewport().setMinX(0);
            graphView.getViewport().setMaxX(maxX);
            graphView.getViewport().setMaxXAxisSize(maxX);
            graphView.getViewport().setXAxisBoundsManual(true);

        }

        else if("Patika HazineAvi SayiBulmaca".contains(gameName)){
            String dif1,dif2,dif3;
            final String gameDisplayName;
            if(gameName.contains("HazineAvi")){
                dif1 = ".5"; dif2 = ".8"; dif3 = ".10";
                gameDisplayName = getString(R.string.HazineAvı);
            } else if(gameName.contains("Patika")) {
                dif1 = ".5"; dif2 = ".7"; dif3 = ".9";
                gameDisplayName = getString(R.string.Patika);
            } else {
                dif1 = ".3"; dif2 = ".4"; dif3 = ".5";
                gameDisplayName = getString(R.string.SayıBulmaca);
            }
            JSONArray game1 = jb.getJSONArray(gameName+dif1);
            JSONArray game2= jb.getJSONArray(gameName+dif2);
            JSONArray game3 = jb.getJSONArray(gameName+dif3);

            mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
            maxX=0;
            double x,y;
            GraphView graphView = statsCl.findViewWithTag(gameName+"G");
            series1 = new LineGraphSeries<>();
            series2 = new LineGraphSeries<>();
            series3 = new LineGraphSeries<>();
            pseries1 = new PointsGraphSeries<>();
            pseries2 = new PointsGraphSeries<>();
            pseries3 = new PointsGraphSeries<>();
//            LineGraphSeries<DataPoint> s2 = new LineGraphSeries<>();
//            s2.appendData(new DataPoint(10,10),true,100);
//            s2.appendData(new DataPoint(50,0),true,100);

//            s2.setColor(getResources().getColor(R.color.transparent));
            series1.appendData(new DataPoint(0,0),true,100);
            series2.appendData(new DataPoint(0,0),true,100);
            series3.appendData(new DataPoint(0,0),true,100);
//            int numDataPoints = 20;
            for(int i = 0; i < game1.length(); i++){
                x = Double.parseDouble(String.format("%.2f",game1.getJSONArray(i).getDouble(1)).replace(",","."));
                y = Double.parseDouble(String.format("%.2f",game1.getJSONArray(i).getDouble(0)).replace(",","."));
                series1.appendData(new DataPoint(x,y),true,100);
                Log.i("series1",series1.getHighestValueX()+"");
                series1.setColor(getResources().getColor(R.color.darkgreen_T));
                pseries1.appendData(new DataPoint(x,y),true,100);
                pseries1.setColor(getResources().getColor(R.color.green_W));
                pseries1.setSize(10f);
                if(x > maxX){
                    maxX = (int)(Math.ceil(x/10)*10);
                }
            }
            series1.setTitle(gameDisplayName+" "+getString(R.string.Easy));
            pseries1.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    mToast.cancel();
                    mToast = Toast.makeText(ProfileActivity.this, gameDisplayName+" "+getString(R.string.Easy)+": "+ (int)dataPoint.getX()+", "+dataPoint.getY()+"s", Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });
            for(int i = 0; i < game2.length(); i++){
                x = Double.parseDouble(String.format("%.2f",game2.getJSONArray(i).getDouble(1)).replace(",","."));
                y = Double.parseDouble(String.format("%.2f",game2.getJSONArray(i).getDouble(0)).replace(",","."));
                series2.appendData(new DataPoint(x,y),true,100);
                Log.i("series2",series2.getHighestValueX()+"");
                series2.setColor(getResources().getColor(R.color.dark_red));
                pseries2.appendData(new DataPoint(x,y),true,100);
                pseries2.setColor(getResources().getColor(R.color.light_red));
                pseries2.setSize(10f);
                if(x > maxX){
                    maxX = (int)(Math.ceil(x/10)*10);
                }
            }
            series2.setTitle(gameDisplayName+" "+getString(R.string.Medium));
            pseries2.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    mToast.cancel();
                    mToast = Toast.makeText(ProfileActivity.this, gameDisplayName+" "+getString(R.string.Medium)+": "+ (int)dataPoint.getX()+", "+dataPoint.getY()+"s", Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });
            for(int i = 0; i < game3.length(); i++){
                x = Double.parseDouble(String.format("%.2f",game3.getJSONArray(i).getDouble(1)).replace(",","."));
                y = Double.parseDouble(String.format("%.2f",game3.getJSONArray(i).getDouble(0)).replace(",","."));
                series3.appendData(new DataPoint(x,y),true,100);
                Log.i("series3",series3.getHighestValueX()+"");
                series3.setColor(getResources().getColor(R.color.dark_blue_green));
                pseries3.appendData(new DataPoint(x,y),true,100);
                pseries3.setColor(getResources().getColor(R.color.light_blue_green));
                pseries3.setSize(10f);
                if(x > maxX){
                    maxX = (int)(Math.ceil(x/10)*10);
                }
            }
            series3.setTitle(gameDisplayName+" "+getString(R.string.Hard));
            pseries3.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    mToast.cancel();
                    mToast = Toast.makeText(ProfileActivity.this, gameDisplayName+" "+getString(R.string.Hard)+": "+ (int) dataPoint.getX()+", "+dataPoint.getY()+"s", Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });

            graphView.setBackgroundColor(getResources().getColor(R.color.f7f5fa));
            graphView.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.near_black_blue));
            graphView.getGridLabelRenderer().setHorizontalLabelsColor(getResources().getColor(R.color.near_black_blue));
            graphView.getGridLabelRenderer().setVerticalLabelsColor(getResources().getColor(R.color.near_black_blue));

            graphView.addSeries(series1);
            graphView.addSeries(series2);
            graphView.addSeries(series3);
//            graphView.addSeries(s2);
            graphView.addSeries(pseries1);
            graphView.addSeries(pseries2);
            graphView.addSeries(pseries3);

            graphView.setTitle(gameDisplayName+" "+getString(R.string.avgTime));
            graphView.getGridLabelRenderer().setHorizontalAxisTitle(getString(R.string.numques));
//            graphView.getGridLabelRenderer().setVerticalAxisTitle(getString(R.string.avgTime));
            graphView.getGridLabelRenderer().setHorizontalAxisTitleColor(getResources().getColor(R.color.near_black_blue));
//            graphView.getGridLabelRenderer().setVerticalAxisTitleColor(getResources().getColor(R.color.near_black_blue));
            graphView.setTitleColor(getResources().getColor(R.color.near_black_blue));
//            graphView.getLegendRenderer().setVisible(true);
//            graphView.getLegendRenderer().setAlign(TOP);

            // activate horizontal and vertical zooming and scrolling
            graphView.getViewport().setScalableY(true);


//            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
//            staticLabelsFormatter.setHorizontalLabels(new String[] {"0","5", "10", "15", "20", "25", "30","35", "40", "45","50"});
//            staticLabelsFormatter.setVerticalLabels(new String[] {"0","10", "20", "30", "40", "50", "60","70", "80", "90","100"});
//            graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(3);
//
            graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf,nf){
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        // show normal x values
//                        Log.i("normalX",value+"");
                        return super.formatLabel(value, true);
                    } else {
                        // show currency for y values
//                        Log.i("currencyY",value+"");
                        return super.formatLabel(value, false)+"s";
                    }
                }
            });

            if(maxX == 0){
                graphView.getViewport().setMaxY(10);
                graphView.getViewport().setYAxisBoundsManual(true);
                maxX = 10;
            }
            graphView.getViewport().setMinX(0);
            graphView.getViewport().setMaxX(maxX);
            graphView.getViewport().setMaxXAxisSize(maxX);
            graphView.getViewport().setXAxisBoundsManual(true);



        }

        else{
            String dif1,dif2,dif3,dif4;
            final String gameDisplayName;
            if(gameName.contains("SozcukTuru")){
                dif1 = ".Easy"; dif2 = ".Medium"; dif3 = ".Hard"; dif4 = ".Hardest";
                gameDisplayName = getString(R.string.SözcükTuru);
            } else {
                dif1 = ".3"; dif2 = ".4"; dif3 = ".5"; dif4 = ".6";
                gameDisplayName = getString(R.string.Piramit);
            }
            JSONArray game1 = jb.getJSONArray(gameName+dif1);
            JSONArray game2= jb.getJSONArray(gameName+dif2);
            JSONArray game3 = jb.getJSONArray(gameName+dif3);
            JSONArray game4 = jb.getJSONArray(gameName+dif4);


            mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
            maxX=0;
            double x,y;
            GraphView graphView = statsCl.findViewWithTag(gameName+"G");
            series1 = new LineGraphSeries<>();
            series2 = new LineGraphSeries<>();
            series3 = new LineGraphSeries<>();
            series4 = new LineGraphSeries<>();
            pseries1 = new PointsGraphSeries<>();
            pseries2 = new PointsGraphSeries<>();
            pseries3 = new PointsGraphSeries<>();
            pseries4 = new PointsGraphSeries<>();
//            LineGraphSeries<DataPoint> s2 = new LineGraphSeries<>();
//            s2.appendData(new DataPoint(0,0),true,100);
//            s2.appendData(new DataPoint(50,0),true,100);

//            s2.setColor(getResources().getColor(R.color.transparent));
            series1.appendData(new DataPoint(0,0),true,100);
            series2.appendData(new DataPoint(0,0),true,100);
            series3.appendData(new DataPoint(0,0),true,100);
            series4.appendData(new DataPoint(0,0),true,100);
//            int numDataPoints = 20;
            for(int i = 0; i < game1.length(); i++){
                x = Double.parseDouble(String.format("%.2f",game1.getJSONArray(i).getDouble(1)).replace(",","."));
                y = Double.parseDouble(String.format("%.2f",game1.getJSONArray(i).getDouble(0)).replace(",","."));
                series1.appendData(new DataPoint(x,y),true,100);
                Log.i("series1",series1.getHighestValueX()+"");
                series1.setColor(getResources().getColor(R.color.darkgreen_T));
                pseries1.appendData(new DataPoint(x,y),true,100);
                pseries1.setColor(getResources().getColor(R.color.green_W));
                pseries1.setSize(10f);
                if(x > maxX){
                    maxX = (int)(Math.ceil(x/10)*10);
                }
            }
            series1.setTitle(gameDisplayName+" "+getString(R.string.Easy));
            pseries1.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    mToast.cancel();
                    mToast = Toast.makeText(ProfileActivity.this, gameDisplayName+" "+getString(R.string.Easy)+": "+ (int)dataPoint.getX()+", "+dataPoint.getY()+"s", Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });
            for(int i = 0; i < game2.length(); i++){
                x = Double.parseDouble(String.format("%.2f",game2.getJSONArray(i).getDouble(1)).replace(",","."));
                y = Double.parseDouble(String.format("%.2f",game2.getJSONArray(i).getDouble(0)).replace(",","."));
                series2.appendData(new DataPoint(x,y),true,100);
                Log.i("series2",series2.getHighestValueX()+"");
                series2.setColor(getResources().getColor(R.color.dark_red));
                pseries2.appendData(new DataPoint(x,y),true,100);
                pseries2.setColor(getResources().getColor(R.color.light_red));
                pseries2.setSize(10f);
                if(x > maxX){
                    maxX = (int)(Math.ceil(x/10)*10);
                }
            }
            series2.setTitle(gameDisplayName+" "+getString(R.string.Medium));
            pseries2.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    mToast.cancel();
                    mToast = Toast.makeText(ProfileActivity.this, gameDisplayName+" "+getString(R.string.Medium)+": "+ (int)dataPoint.getX()+", "+dataPoint.getY()+"s", Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });
            for(int i = 0; i < game3.length(); i++){
                x = Double.parseDouble(String.format("%.2f",game3.getJSONArray(i).getDouble(1)).replace(",","."));
                y = Double.parseDouble(String.format("%.2f",game3.getJSONArray(i).getDouble(0)).replace(",","."));
                series3.appendData(new DataPoint(x,y),true,100);
                Log.i("series3",series3.getHighestValueX()+"");
                series3.setColor(getResources().getColor(R.color.dark_blue_green));
                pseries3.appendData(new DataPoint(x,y),true,100);
                pseries3.setColor(getResources().getColor(R.color.light_blue_green));
                pseries3.setSize(10f);
                if(x > maxX){
                    maxX = (int)(Math.ceil(x/10)*10);
                }
            }
            series3.setTitle(gameDisplayName+" "+getString(R.string.Hard));
            pseries3.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    mToast.cancel();
                    mToast = Toast.makeText(ProfileActivity.this, gameDisplayName+" "+getString(R.string.Hard)+": "+ (int) dataPoint.getX()+", "+dataPoint.getY()+"s", Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });

            for(int i = 0; i < game4.length(); i++){
                x = Double.parseDouble(String.format("%.2f",game4.getJSONArray(i).getDouble(1)).replace(",","."));
                y = Double.parseDouble(String.format("%.2f",game4.getJSONArray(i).getDouble(0)).replace(",","."));
                series4.appendData(new DataPoint(x,y),true,100);
                Log.i("series4",series4.getHighestValueX()+"");
                series4.setColor(Color.parseColor("#af01b5"));
                pseries4.appendData(new DataPoint(x,y),true,100);
                pseries4.setColor(Color.parseColor("#f700ff"));
                pseries4.setSize(10f);
                if(x > maxX){
                    maxX = (int)(Math.ceil(x/10)*10);
                }
            }
            series4.setTitle(gameDisplayName+" "+getString(R.string.VeryHard));
            pseries4.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    mToast.cancel();
                    mToast = Toast.makeText(ProfileActivity.this, gameDisplayName+" "+getString(R.string.VeryHard)+": "+ (int) dataPoint.getX()+", "+dataPoint.getY()+"s", Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });

            graphView.setBackgroundColor(getResources().getColor(R.color.f7f5fa));
            graphView.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.near_black_blue));
            graphView.getGridLabelRenderer().setHorizontalLabelsColor(getResources().getColor(R.color.near_black_blue));
            graphView.getGridLabelRenderer().setVerticalLabelsColor(getResources().getColor(R.color.near_black_blue));

            graphView.addSeries(series1);
            graphView.addSeries(series2);
            graphView.addSeries(series3);
            graphView.addSeries(series4);
//            graphView.addSeries(s2);
            graphView.addSeries(pseries1);
            graphView.addSeries(pseries2);
            graphView.addSeries(pseries3);
            graphView.addSeries(pseries4);

            graphView.setTitle(gameDisplayName+" "+getString(R.string.avgTime));
            graphView.getGridLabelRenderer().setHorizontalAxisTitle(getString(R.string.numques));
//            graphView.getGridLabelRenderer().setVerticalAxisTitle(getString(R.string.avgTime));
            graphView.getGridLabelRenderer().setHorizontalAxisTitleColor(getResources().getColor(R.color.near_black_blue));
//            graphView.getGridLabelRenderer().setVerticalAxisTitleColor(getResources().getColor(R.color.near_black_blue));
            graphView.setTitleColor(getResources().getColor(R.color.near_black_blue));
//            graphView.getLegendRenderer().setVisible(true);
//            graphView.getLegendRenderer().setAlign(TOP);

            // activate horizontal and vertical zooming and scrolling
            graphView.getViewport().setScalableY(true);


//            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
//            staticLabelsFormatter.setHorizontalLabels(new String[] {"0","5", "10", "15", "20", "25", "30","35", "40", "45","50"});
//            staticLabelsFormatter.setVerticalLabels(new String[] {"0","10", "20", "30", "40", "50", "60","70", "80", "90","100"});
//            graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(3);
//
            graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf,nf){
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        // show normal x values
//                        Log.i("normalX",value+"");
                        return super.formatLabel(value, true);
                    } else {
                        // show currency for y values
//                        Log.i("currencyY",value+"");
                        return super.formatLabel(value, false)+"s";
                    }
                }
            });

            if(maxX == 0){
                graphView.getViewport().setMaxY(10);
                graphView.getViewport().setYAxisBoundsManual(true);
                maxX = 10;
            }
            graphView.getViewport().setMinX(0);
            graphView.getViewport().setMaxX(maxX);
            graphView.getViewport().setMaxXAxisSize(maxX);
            graphView.getViewport().setXAxisBoundsManual(true);


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        scrollViewLL = findViewById(R.id.scrollViewLL);

        SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        String displayname = intent.getStringExtra("displayname");
        String username = intent.getStringExtra("username");

        LayoutInflater inflater = getLayoutInflater();
        statsCl = (ConstraintLayout) inflater.inflate(this.getResources().getIdentifier("stats_layout", "layout", this.getPackageName()),null);
        infoCl = (ConstraintLayout) inflater.inflate(this.getResources().getIdentifier("info_layout", "layout", this.getPackageName()),null);

        if(id == null){
            id = sharedPreferences.getString("id", "non");
            displayname = sharedPreferences.getString("displayname",getString(R.string.Unknown));
            username = sharedPreferences.getString("username",getString(R.string.Unknown));
            ((TextView) infoCl.findViewById(R.id.emailTV2)).setText(sharedPreferences.getString("email",getString(R.string.Unknown)));
        } else {
            assert username != null;
            if(username.equals(sharedPreferences.getString("username", "non"))){
                ((TextView) infoCl.findViewById(R.id.emailTV2)).setText(sharedPreferences.getString("email",getString(R.string.Unknown)));
            } else {
                infoCl.findViewById(R.id.emailLL).setVisibility(View.GONE);
            }
        }

        ((TextView) infoCl.findViewById(R.id.nameAndSurnameTV2)).setText(displayname);
        ((TextView) infoCl.findViewById(R.id.usernameTV2)).setText(username);


        scrollViewLL.addView(infoCl);

        GetRequest getRequest = new GetRequest();
        getRequest.execute("https://mind-plateau-api.herokuapp.com","fx!Ay:;<p6Q?C8N{", id);

    }
}