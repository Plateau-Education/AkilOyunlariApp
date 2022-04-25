package com.yaquila.akiloyunlariapp.gameutils;

import static com.yaquila.akiloyunlariapp.GroupSolvingActivity.currentGrid;

import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.yaquila.akiloyunlariapp.GroupSolvingActivity;
import com.yaquila.akiloyunlariapp.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HazineAviUtils {

    public static AppCompatActivity context;

    public static String clickedBox = "-1";
    public static String switchPosition = "diamond";
    public static int gridSize = 5;

    public static List<List<String>> operations = new ArrayList<>();
    public static List<String> clueIndexes = new ArrayList<>();
    public static List<String> answer = new ArrayList<>();
    public static String[][] gridDCs = new String[10][10];

    public static void initVars(AppCompatActivity ctx){
        clickedBox = "-1";
        switchPosition = "diamond";
        gridSize = 5;
        operations = new ArrayList<>();
        clueIndexes = new ArrayList<>();
        answer = new ArrayList<>();
        context = ctx;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void changeClicked(final View view){
        TextView box = (TextView) view;
        String answerIndex = box.getTag().toString();
        int i1 = Integer.parseInt(String.valueOf(answerIndex.charAt(1)));
        int i2 = Integer.parseInt(String.valueOf(answerIndex.charAt(0)));
        if (!clueIndexes.contains(answerIndex)) {
            String op = null;
            if (switchPosition.equals("diamond")) {
                if (gridDCs[i1][i2].equals("-1")) {
                    box.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                    gridDCs[i1][i2] = "0";
                    op = "0";
                } else {
                    box.setBackground(context.getResources().getDrawable(R.drawable.ic_diamond));
                    gridDCs[i1][i2] = "-1";
                    op = "-1";
                }
            } else if (switchPosition.equals("cross")) {
                if (gridDCs[i1][i2].equals("-2")) {
                    box.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                    gridDCs[i1][i2] = "0";
                    op = "0";
                } else {
                    box.setBackground(context.getResources().getDrawable(R.drawable.ic_cross));
                    gridDCs[i1][i2] = "-2";
                    op = "-2";
                }
            }
            clickedBox = answerIndex;
            List<String> newOp = new ArrayList<>(Arrays.asList(answerIndex, op));
            if (operations != null && !newOp.equals(operations.get(operations.size() - 1))) {
                operations.add(new ArrayList<>(Arrays.asList(answerIndex, op)));
            }
            if(context.getClass() == GroupSolvingActivity.class) {
                currentGrid.get(i2).set(i1, Integer.parseInt(op));
                GroupSolvingActivity.sendGrid(currentGrid, answer, GroupSolvingActivity.socket);
            }
            Log.i("operations", operations + "");
        }
    }

    public static void changeSwitch(final View view){
        ImageView switchTV = (ImageView) view;
        if(switchPosition.equals("diamond")){
            switchTV.setImageResource(R.drawable.ic_cross);
            switchPosition = "cross";
        }
        else if(switchPosition.equals("cross")){
            switchTV.setImageResource(R.drawable.ic_diamond);
            switchPosition = "diamond";
        }
    } // Elmas - çarpı değiştir

    public static Object[] undoOperation(){
        String co1 = "0";
        String num2 = "0";

        if(operations.size() > 1){
            //            operations = operations.subList(0,operations.size()-1);
            List<String> tuple1 = operations.get(operations.size()-1);
            operations = operations.subList(0,operations.size()-1);
            co1 = tuple1.get(0);
            num2 = "0";
            String co2;
            for(int i = operations.size()-1; i>0; i--){
                List<String> tuple2 = operations.get(i);
                co2 = tuple2.get(0);
                if(co1.equals(co2)){
                    num2 = tuple2.get(1);
                    break;
                }
            }
            Log.i("co/num",co1+" / "+num2);
            GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
            TextView currentBox = gridLayout.findViewWithTag(co1);
            final int i1 = Integer.parseInt(String.valueOf(co1.charAt(1)));
            final int i2 = Integer.parseInt(String.valueOf(co1.charAt(0)));
            if(context.getClass() == GroupSolvingActivity.class) {
                currentGrid.get(i1).set(i2,Integer.parseInt(num2));
                GroupSolvingActivity.sendGrid(currentGrid, answer, GroupSolvingActivity.socket);
            }
            if(num2.equals("-1")){
                currentBox.setBackground(context.getResources().getDrawable(R.drawable.ic_diamond));
                gridDCs[i1][i2] = "-1";
            }
            else if(num2.equals("-2")){
                currentBox.setBackground(context.getResources().getDrawable(R.drawable.ic_cross));
                gridDCs[i1][i2] = "-2";
            }
            else{
                currentBox.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                gridDCs[i1][i2] = "0";
            }
        }
        return new Object[]{co1, num2};
    } // Son işlemi geri al

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
            operations.add(new ArrayList<>(Arrays.asList("00", "0")));
            GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
            clickedBox = "-1";
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                    gridDCs[j][i] = "0";
                    if(!clueIndexes.contains(Integer.toString(j)+i)){
                        tv.setText("");
                    }
                }
            }
            if(context.getClass() == GroupSolvingActivity.class) {
                for (int i = 0; i < gridSize; i++) {
                    List<Object> row = currentGrid.get(i);
                    for (int j = 0; j < gridSize; j++) {
                        if ((int)row.get(j) < 0) row.set(j, 0);
                    }
                    currentGrid.set(i, row);
                }
                GroupSolvingActivity.sendGrid(currentGrid, answer, GroupSolvingActivity.socket);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    } // Tüm işlemleri sıfırla

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean checkAnswer(){
        GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
        boolean checking=true;
        for(int i = 0; i<gridSize; i++){
            for(int j = 0; j<gridSize; j++){
                String co = Integer.toString(j)+i;
                if(answer.contains(co) && !gridDCs[i][j].equals("-1")){
                    checking=false;
//                    Log.i("checkfalse1","i,j: "+ i + ", "+ j);
                    break;
                }
                else if(!answer.contains(co) && gridDCs[i][j].equals("-1")){
                    checking=false;
//                    Log.i("checkfalse2","i,j: "+ i + ", "+ j);
                    break;
                }
            }
        }
        Log.i("check",checking+"  "+answer);
        return checking;
    } // Çözümün doğruluğunu kontrol et

    public static void seperateGridAnswer(final JSONArray grid) throws JSONException {
        GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
        for(int i = 0; i < gridSize; i++){
            for(int j = 0; j <  gridSize; j++) {
                String n = ((JSONArray)grid.get(i)).get(j).toString();
                if(Integer.parseInt(n) > 0){
                    clueIndexes.add(Integer.toString(j)+i);
                    ((TextView) gridLayout.findViewWithTag(Integer.toString(j)+i)).setText(n);
                }
                else if(n.equals("-1")){
                    answer.add(Integer.toString(j)+i);
                }
            }
        }
    } // Çekilen soruyu kullanıcıya göster

    public static void clearGrid(){
        operations = new ArrayList<>();
        operations.add(new ArrayList<>(Arrays.asList("00", "0")));
        GridLayout gridLayout = context.findViewById(R.id.gridGL_grid);
        if(context.getClass() == GroupSolvingActivity.class)
            currentGrid = new ArrayList<>();
        for (int i = 0; i < gridSize; i++) {
            List<Object> row = new ArrayList<>();
            for (int j = 0; j < gridSize; j++) {
                TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                tv.setText("");
                tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                gridDCs[j][i] = "0";
                tv.setEnabled(true);
                row.add(0);
            }
            if(context.getClass() == GroupSolvingActivity.class) currentGrid.set(i,row);
        }
        clickedBox = "-1";
    }

}
