package com.yaquila.akiloyunlariapp.gameutils;

import static com.yaquila.akiloyunlariapp.GroupSolvingActivity.currentGrid;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.yaquila.akiloyunlariapp.GroupSolvingActivity;
import com.yaquila.akiloyunlariapp.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SayiBulmacaUtils {

    public static AppCompatActivity context;

    public static int clickedBox = -1;
    public static int gridSize = 3;
    public static boolean undoing=false;

    public static List<List<Integer>> operations = new ArrayList<>();
    public static JSONArray answer;

    public static void initVars(AppCompatActivity ctx){
        context = ctx;
        clickedBox = -1;
        gridSize = 3;
        undoing=false;
        operations = new ArrayList<>();
        answer = new JSONArray();
    }

    public static void changeClicked(final View view){
        TextView box = (TextView) view;
        GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
        int answerIndex = Integer.parseInt((box.getTag().toString()).substring(box.getTag().toString().length()-1));
        if (clickedBox != answerIndex){
            if (clickedBox != -1){
                if (clickedBox == 0){
                    gridLayout.findViewWithTag("answer"+ clickedBox).setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottomleft));
                }
                else{
                    gridLayout.findViewWithTag("answer"+ clickedBox).setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottom));
                }
                ((TextView) gridLayout.findViewWithTag("answer"+clickedBox)).setTextColor(context.getResources().getColor(R.color.light_red));
            }
            if(answerIndex == 0){
                box.setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottomleft_shallow2));
            }
            else{
                box.setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottom_shallow2));
            }
            box.setTextColor(context.getResources().getColor(R.color.f7f5fa));
            clickedBox = answerIndex;
        }
        else{
            if(!undoing){
                if (answerIndex == 0){
                    box.setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottomleft));
                }
                else{
                    box.setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottom));
                }
                box.setTextColor(context.getResources().getColor(R.color.light_red));
                clickedBox = -1;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    public static boolean numClicked(final View view){
        Button btn = (Button) view;
        GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
        if(clickedBox != -1){
            TextView currentBox = gridLayout.findViewWithTag("answer"+ clickedBox);
            if(currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox,-1)));
            }
            currentBox.setText(btn.getTag().toString());
            List<Integer> newOp = new ArrayList<>(Arrays.asList(clickedBox, Integer.parseInt(btn.getTag().toString())));
            if(!newOp.equals(operations.get(operations.size() - 1))){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox, Integer.parseInt(btn.getTag().toString()))));
            }
            if(context.getClass() == GroupSolvingActivity.class) {
                List<Object> cg = ((List<Object>)currentGrid.get(0));
                ((List<Integer>)cg.get(cg.size()-1)).set(clickedBox, Integer.parseInt(btn.getTag().toString()));
                GroupSolvingActivity.sendGrid(currentGrid, answer, GroupSolvingActivity.socket);
            }
            boolean isFull = true;
            for (int i = 0; i<gridSize; i++){
                if (((TextView)gridLayout.findViewWithTag("answer"+i)).getText().toString().equals("")){
                    isFull = false;
                    break;
                }
            }
            Log.i("operations",operations+"");
            return isFull;
        }
        return false;
    }

    public static void deleteNum(){
        GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
        if(clickedBox != -1){
            TextView currentBox = gridLayout.findViewWithTag("answer"+ clickedBox);
            if(!currentBox.getText().toString().equals("")){
                operations.add(new ArrayList<>(Arrays.asList(clickedBox, -1)));
                Log.i("operations",operations+"");
                currentBox.setText("");
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public static void undoOperation(){
        if(operations.size() > 1){
            operations = operations.subList(0,operations.size()-1);
            List<Integer> tuple = operations.get(operations.size()-1);
            int co = tuple.get(0);
            int num = tuple.get(1);
            Log.i("co/num",co+" / "+num);
            GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
            TextView currentBox = gridLayout.findViewWithTag("answer"+ co);
            if(context.getClass() == GroupSolvingActivity.class) {
                List<Object> cg = ((List<Object>)currentGrid.get(0));
                ((List<Integer>)cg.get(cg.size()-1)).set(co,num);
                GroupSolvingActivity.sendGrid(currentGrid, answer, GroupSolvingActivity.socket);
            }
            if(num == -1){
                currentBox.setText("");
            }
            else{
                currentBox.setText(Integer.toString(num));
            }
            undoing = true;
            changeClicked(currentBox);
            undoing =false;
        }
    }

    public static void resetGrid(final View view){
        try {
            final TextView resetTV = (TextView) view;
            resetTV.setTextColor(context.getResources().getColor(R.color.light_red));
            resetTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            resetTV.setText(R.string.ResetNormal);
            resetTV.postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetTV.setTextColor(context.getResources().getColorStateList(R.color.reset_selector_tvcolor));
                    resetTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    resetTV.setText(R.string.ResetUnderlined);
                }
            }, 100);

            operations = new ArrayList<>();
            GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
            for (int i = 0; i < gridSize; i++) {
                TextView currentBox = gridLayout.findViewWithTag("answer" + i);
                currentBox.setText("");
            }
            if(clickedBox!=-1) {
                if (clickedBox == 0) {
                    gridLayout.findViewWithTag("answer" + clickedBox).setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottomleft));
                } else {
                    gridLayout.findViewWithTag("answer" + clickedBox).setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottom));
                }
            }
            clickedBox = -1;
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                }
            }
            if(context.getClass() == GroupSolvingActivity.class) {
                for (int i = 0; i < gridSize; i++) {
                    List<Object> row = (List<Object>) ((List<Object>) currentGrid.get(0)).get(i);
                    for (int j = 0; j < gridSize; j++) {
                        if ((int)row.get(j) < 0) row.set(j, 0);
                    }
                    ((List<Object>)currentGrid.get(0)).set(i, row);
                }
                GroupSolvingActivity.sendGrid(currentGrid, answer, GroupSolvingActivity.socket);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void notesOnGrid(final View view) {
        TextView clickedTV = (TextView) view;
        GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
        String clickedNum = clickedTV.getText().toString();
        if(currentGrid.size()<1){
//            currentGrid.add(new ArrayList<>());
        }
        if (Objects.equals(clickedTV.getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.stroke_bg).getConstantState())) {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    if (tv.getText().toString().equals(clickedNum)) {
                        tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg_bluegreen));
                    }
                }
            }
        }
        else if (Objects.equals(clickedTV.getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.stroke_bg_bluegreen).getConstantState())) {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    if (tv.getText().toString().equals(clickedNum)) {
                        tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg_red));
                    }
                }
            }
        }
        else if (Objects.equals(clickedTV.getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.stroke_bg_red).getConstantState())) {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    if (tv.getText().toString().equals(clickedNum)) {
                        tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean checkAnswer(){
        GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
        boolean checking=true;
        for(int i = 0; i < gridSize; i++){
            try {
                if(!((TextView)gridLayout.findViewWithTag("answer"+ i)).getText().equals(answer.get(i).toString())){
                    checking=false;
                }
                Log.i("checking",((TextView)gridLayout.findViewWithTag("answer"+ i)).getText().toString()+" / "+(answer.get(i).toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("check",checking+"  "+answer);
        return checking && answer.length()>0;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    public static void seperateGridAnswer(final JSONArray grid) throws JSONException {
        GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
        answer = (JSONArray) grid.get(grid.length() - 1);
        Log.i("jsonGrid",""+grid);
        for (int i = 0; i < grid.length()-1; i++){
            JSONArray row = (JSONArray) grid.get(i);
            for (int j = 0; j< row.length()-1; j++){
                TextView tv = gridLayout.findViewWithTag(Integer.toString(j)+ i);
                tv.setText(row.get(j).toString());
            }
            JSONArray rguide = (JSONArray)row.get(row.length()-1);
            TextView rGuideTV = gridLayout.findViewWithTag("g"+i);
            if(rguide.get(0).toString().equals("0")){
                rGuideTV.setText(rguide.get(1).toString());
            }
            else if(rguide.get(1).toString().equals("0")){
                rGuideTV.setText("+"+rguide.get(0).toString());
            }
            else{
                rGuideTV.setText("+"+rguide.get(0).toString()+"  "+rguide.get(1).toString());
            }
        }
        TextView guideanswer = gridLayout.findViewWithTag("answerguide");
        guideanswer.setText("+"+((JSONArray) answer.get(answer.length()-1)).get(0).toString());
        answer.remove(answer.length()-1);
    }

    public static void clearGrid(){
        operations = new ArrayList<>();
        GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
        if(context.getClass() == GroupSolvingActivity.class)
            currentGrid.clear();
        for (int i = 0; i < gridSize; i++) {
            TextView currentBox = gridLayout.findViewWithTag("answer" + i);
            currentBox.setText("");
            currentBox.setEnabled(true);
        }
        if(clickedBox != -1){
            if (clickedBox == 0) {
                gridLayout.findViewWithTag("answer" + clickedBox).setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottomleft));
            } else {
                gridLayout.findViewWithTag("answer" + clickedBox).setBackground(context.getResources().getDrawable(R.drawable.strokebg_topbottom));
            }
        }

        for (int i = 0; i < gridSize; i++) {
            List<Object> row = new ArrayList<>();
            for (int j = 0; j < gridSize; j++) {
                TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                tv.setEnabled(true);
                row.add(0);
            }
            if(context.getClass() == GroupSolvingActivity.class) ((List<Object>)currentGrid.get(0)).add(row);
        }
        clickedBox = -1;
        answer = new JSONArray();
    }

}
