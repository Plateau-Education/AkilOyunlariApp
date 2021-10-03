package com.yaquila.akiloyunlariapp.gameutils;

import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.yaquila.akiloyunlariapp.R;

import org.json.JSONArray;
import org.json.JSONException;

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
        if (!clueIndexes.contains(answerIndex)) {
            String op = null;
            if (switchPosition.equals("diamond")) {
                if (Objects.equals(box.getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.ic_diamond).getConstantState())) {
                    box.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                    op = "0";
                } else {
                    box.setBackground(context.getResources().getDrawable(R.drawable.ic_diamond));
                    op = "-1";
                }
            } else if (switchPosition.equals("cross")) {
                if (Objects.equals(box.getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.ic_cross).getConstantState())) {
                    box.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                    op = "0";
                } else {
                    box.setBackground(context.getResources().getDrawable(R.drawable.ic_cross));
                    op = "-2";
                }
            }
            clickedBox = answerIndex;
            List<String> newOp = new ArrayList<>(Arrays.asList(answerIndex, op));
            if (operations != null && !newOp.equals(operations.get(operations.size() - 1))) {
                operations.add(new ArrayList<>(Arrays.asList(answerIndex, op)));
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
            GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
            TextView currentBox = gridLayout.findViewWithTag(co1);
            if(num2.equals("-1")){
                currentBox.setBackground(context.getResources().getDrawable(R.drawable.ic_diamond));
            }
            else if(num2.equals("-2")){
                currentBox.setBackground(context.getResources().getDrawable(R.drawable.ic_cross));
            }
            else{
                currentBox.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
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
            GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
            clickedBox = "-1";
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                    tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                    if(!clueIndexes.contains(Integer.toString(j)+i)){
                        tv.setText("");
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    } // Tüm işlemleri sıfırla

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean checkAnswer(){
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        boolean checking=true;
        for(int i = 0; i<gridSize; i++){
            for(int j = 0; j<gridSize; j++){
                String co = Integer.toString(j)+i;
                if(answer.contains(co) && !Objects.equals(gridLayout.findViewWithTag(co).getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.ic_diamond).getConstantState())){
                    checking=false;
                    break;
                }
                else if(!answer.contains(co) && Objects.equals(gridLayout.findViewWithTag(co).getBackground().getConstantState(), context.getResources().getDrawable(R.drawable.ic_diamond).getConstantState())){
                    checking=false;
                    break;
                }
            }
        }
        Log.i("check",checking+"  "+answer);
        return checking;
    } // Çözümün doğruluğunu kontrol et

    public static void seperateGridAnswer(final JSONArray grid) throws JSONException {
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
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
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                tv.setText("");
                tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                tv.setEnabled(true);
            }
        }
        clickedBox = "-1";
        clueIndexes = new ArrayList<>();
        answer = new ArrayList<>();
    }

}
