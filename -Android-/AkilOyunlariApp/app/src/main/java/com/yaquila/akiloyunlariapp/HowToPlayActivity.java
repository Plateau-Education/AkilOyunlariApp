package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

public class HowToPlayActivity extends AppCompatActivity {

    String currentGameName;
    String type;

    public void goToGameList(View view){
        Intent intent = new Intent(getApplicationContext(), GameListActivity.class);
        intent.putExtra("type",type);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    public void goToGuide(View view) {
        if (currentGameName.contains("azine")) {
            Intent intent = new Intent(this, GameGuideActivity.class);
            intent.putExtra("gamename", "Hazine Avı");
            intent.putExtra("type",type);
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        } else if (currentGameName.contains("atika")){
            Intent intent = new Intent(this, GameGuideActivity.class);
            intent.putExtra("gamename", "Patika");
            intent.putExtra("type",type);
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        }
    }
    public void howToPlayGameListOpen(View view){
        ConstraintLayout constraintLayout = (ConstraintLayout) view.getParent();
        RelativeLayout howToPlayGameListRL = (RelativeLayout) constraintLayout.getChildAt(2);
        howToPlayGameListRL.setY(constraintLayout.getBottom());
        howToPlayGameListRL.setVisibility(View.VISIBLE);
        view.animate().translationYBy(-constraintLayout.getBottom()).setDuration(300);
        howToPlayGameListRL.animate().translationYBy(-constraintLayout.getBottom()).setDuration(300);
    }

    public void howToPlayGameListClose(View view){
        RelativeLayout howToPlayGameListRL = (RelativeLayout) ((ScrollView)((LinearLayout) view.getParent()).getParent()).getParent();
        ConstraintLayout constraintLayout = (ConstraintLayout) howToPlayGameListRL.getParent();
        Button slideupButton = (Button)constraintLayout.getChildAt(1);
        slideupButton.animate().translationYBy(constraintLayout.getBottom()).setDuration(300);
        howToPlayGameListRL.animate().translationYBy(constraintLayout.getBottom()).setDuration(300);
    }

    public void howToPlayChangeGame(View view){
        Log.i("tag", (String) view.getTag());
        TextView tv = (TextView) view;
        TextView gameNameTV = findViewById(R.id.gameNameTV_htp);
        gameNameTV.setText((CharSequence) tv.getTag());
        //TODO change content of the how to play text
        if(tv.getTag().equals("Hazine Avı"))
            ((TextView)findViewById(R.id.howToPlay_text)).setText("Hazine Avı oyununda, verilen sayılar komşularında kaç elmas bulunduğunu gösterir. Çözerken ilk bakılması gereken şey, içinde yazan sayı kadar komşusu olan ipuçlarıdır. İçinde yazan sayı kadar komşusu olan ipuçları, her soruda bulunmayabilir veya çözüme ulaşmada yetersiz kalabilir. Bu durumlarda bakılması gereken şey, ipuçlarının komşularına koyulacak elmasların diğer ipuçlarındaki ortak etkileridir.");
        else {
            ((TextView)findViewById(R.id.howToPlay_text)).setText("Yakında...");
        }
        LinearLayout howToPlayGameListLL = (LinearLayout) tv.getParent();
        Typeface mohave_bold = Typeface.createFromAsset(getAssets(), "fonts/mohave_bold.ttf");
        tv.setTypeface(mohave_bold);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);
        Typeface mohave_regular = Typeface.createFromAsset(getAssets(), "fonts/mohave_regular.ttf");
        ((TextView) howToPlayGameListLL.findViewWithTag(currentGameName)).setTypeface(mohave_regular);
        ((TextView) howToPlayGameListLL.findViewWithTag(currentGameName)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 37);
        currentGameName = (String) tv.getTag();
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        Intent intent = getIntent();
        currentGameName = intent.getStringExtra("gameName");
        type = intent.getStringExtra("type");
        if (currentGameName!=null){
            Log.i("gameName", Objects.requireNonNull(intent.getStringExtra("gameName")));
//            Toast.makeText(this, currentGameName, Toast.LENGTH_SHORT).show();
        }
        TextView gameNameTV = findViewById(R.id.gameNameTV_htp);
        if(currentGameName.contains(" ")){
            gameNameTV.setText(currentGameName.substring(0, currentGameName.indexOf(" "))+"\n"+currentGameName.substring(currentGameName.indexOf(" ")));
        }
        else{
            gameNameTV.setText(currentGameName);
        }
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.scrollView_htp).getParent();
        RelativeLayout howToPlayGameListRL = (RelativeLayout) constraintLayout.getChildAt(2);
        Typeface mohave_bold = Typeface.createFromAsset(getAssets(), "fonts/mohave_bold.ttf");
        ((TextView) howToPlayGameListRL.findViewWithTag(currentGameName)).setTypeface(mohave_bold);
        ((TextView) howToPlayGameListRL.findViewWithTag(currentGameName)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);

        switch (currentGameName) {
            case "Sudoku":
                ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_sudoku);
                break;
            case "Hazine Avı":
                ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_hazineavi);
                break;
            case "Patika":
                ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_patika);
                break;
            case "Sayı Bulmaca":
                ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_sayibulmaca);
                break;
            case "Sözcük Türü":
                ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_sozcukturu);
                break;
            case "Piramit":
                ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.htp_piramit);
                break;
            default:
                ((TextView) findViewById(R.id.howToPlay_text)).setText(R.string.Comingsoon);
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        goToGameList(null);
    }
}