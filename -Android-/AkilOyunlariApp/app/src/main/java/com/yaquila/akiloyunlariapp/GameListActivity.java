package com.yaquila.akiloyunlariapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class GameListActivity extends AppCompatActivity {

    private ListView listView1;
    int currentExtendedRow = 0;

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public void goToMainMenu(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void goToGame(View view){
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        if(currentExtendedRow % 2 == 1){
            intent.putExtra("gameName", ((TextView)((LinearLayout)((LinearLayout)((RelativeLayout)view).getParent()).getChildAt(0)).getChildAt(1)).getText());
        }
        else{
            intent.putExtra("gameName", ((TextView)((LinearLayout)((LinearLayout)((RelativeLayout)view).getParent()).getChildAt(0)).getChildAt(0)).getText());
        }
        startActivity(intent);
    }

    public void extendListItem(View view){
        int rowNum = Integer.parseInt(view.getTag().toString());
        if(currentExtendedRow == 0){
            final LinearLayout ll = (LinearLayout) view.getParent();
            RelativeLayout rl = (RelativeLayout) ll.getChildAt(1);
            ll.setBackgroundColor(getResources().getColor(R.color.yellowish));
            rl.setVisibility(View.VISIBLE);
            currentExtendedRow = rowNum;
            Log.i("rowNum/childCount",rowNum+" / "+((LinearLayout)ll.getParent()).getChildCount());

            final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView_gl);
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.smoothScrollTo(0,ll.getBottom()-scrollView.getBottom());
                }
            });

        }
        else if(currentExtendedRow == rowNum){
            LinearLayout ll = (LinearLayout) view.getParent();
            RelativeLayout rl = (RelativeLayout) ll.getChildAt(1);
            ll.setBackgroundColor(getResources().getColor(R.color.f7f5fa));
            rl.setVisibility(View.GONE);
            currentExtendedRow = 0;
        }
        else {
            final LinearLayout ll = (LinearLayout) ((LinearLayout) ((LinearLayout)view.getParent()).getParent()).getChildAt(currentExtendedRow);
            RelativeLayout rl = (RelativeLayout) ll.getChildAt(1);
            ll.setBackgroundColor(getResources().getColor(R.color.f7f5fa));
            rl.setVisibility(View.GONE);
            final LinearLayout ll_2 = (LinearLayout) view.getParent();
            RelativeLayout rl_2 = (RelativeLayout) ll_2.getChildAt(1);
            ll_2.setBackgroundColor(getResources().getColor(R.color.yellowish));
            rl_2.setVisibility(View.VISIBLE);
            currentExtendedRow = rowNum;
            Log.i("rowNum/childCount",rowNum+" / "+((LinearLayout)ll.getParent()).getChildCount());
            final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView_gl);
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.smoothScrollTo(0,ll_2.getBottom()-scrollView.getBottom());
                }
            });
        }

    }

//    public void adaptGameList(){
//        String[] textString = {"Sudoku", "Hazine Avı", "Patika", "Sayı Bulmaca", "Sözcük Türü", "Piramit", "Pentomino", "Anagram", "Kakuro", "Köşeler"};
//        int[] drawableIds = {R.drawable.hazineavi_gamelist_image, R.drawable.hazineavi_gamelist_image, R.drawable.hazineavi_gamelist_image, R.drawable.hazineavi_gamelist_image, R.drawable.hazineavi_gamelist_image, R.drawable.piramit_gamelist_image, R.drawable.hazineavi_gamelist_image, R.drawable.hazineavi_gamelist_image, R.drawable.hazineavi_gamelist_image, R.drawable.hazineavi_gamelist_image};
//
//        CustomAdapter adapter = new CustomAdapter(this, textString, drawableIds);
//
//        listView1 = (ListView)findViewById(R.id.gameList);
//        listView1.setAdapter(adapter);
//    }
//
//    public void gameListListener(){
//        final int[] clickedRow = {-1};
//        final boolean[] noneClicked = {true};
//
//        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                if(clickedRow[0] == -1 || (clickedRow[0] == i && noneClicked[0])){
//                    LinearLayout ll = (LinearLayout) view;
//                    RelativeLayout rl = (RelativeLayout) ll.getChildAt(1);
//                    ll.setBackgroundColor(getResources().getColor(R.color.yellowish));
//                    rl.setVisibility(View.VISIBLE);
//                    clickedRow[0] = i;
//                    noneClicked[0] = false;
////                    if(i >= listView1.getChildCount()-2){
////                        listView1.scrollTo(0,10);
////                    }
//                }
//                else if(clickedRow[0] == i){
//                    LinearLayout ll = (LinearLayout) view;
//                    RelativeLayout rl = (RelativeLayout) ll.getChildAt(1);
//                    ll.setBackgroundColor(getResources().getColor(R.color.f7f5fa));
//                    rl.setVisibility(View.GONE);
//                    noneClicked[0] = true;
//                }
//                else{
//                    LinearLayout ll = (LinearLayout) getViewByPosition(clickedRow[0], listView1);
//                    RelativeLayout rl = (RelativeLayout) ll.getChildAt(1);
//                    ll.setBackgroundColor(getResources().getColor(R.color.f7f5fa));
//                    rl.setVisibility(View.GONE);
//                    LinearLayout ll_2 = (LinearLayout) view;
//                    RelativeLayout rl_2 = (RelativeLayout) ll_2.getChildAt(1);
//                    ll_2.setBackgroundColor(getResources().getColor(R.color.yellowish));
//                    rl_2.setVisibility(View.VISIBLE);
//                    clickedRow[0] = i;
//                    noneClicked[0] = false;
////                    if(i >= listView1.getChildCount()-2){
////                        listView1.scrollTo(0,10);
////                    }
//
//                }
//
//            }
//        });
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);
//        adaptGameList();
//        gameListListener();
    }
}