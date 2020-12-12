package com.yaquila.akiloyunlariapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    String profileInfoState = "Info";
    ConstraintLayout statsCl;
    ConstraintLayout infoCl;
    LinearLayout scrollViewLL;


    public void profileInfoChange(View view){
        TextView statsTV = (TextView) findViewById(R.id.stats_tabTV);
        TextView infoTV = (TextView) findViewById(R.id.info_tabTV);



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

        scrollViewLL = (LinearLayout) findViewById(R.id.scrollViewLL);

        LayoutInflater inflater = getLayoutInflater();
        statsCl = (ConstraintLayout) inflater.inflate(this.getResources().getIdentifier("stats_layout", "layout", this.getPackageName()),null);
        infoCl = (ConstraintLayout) inflater.inflate(this.getResources().getIdentifier("info_layout", "layout", this.getPackageName()),null);

        scrollViewLL.addView(infoCl);


    }
}