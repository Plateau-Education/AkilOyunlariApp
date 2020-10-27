package com.yaquila.akiloyunlariapp;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class CustomAdapter extends BaseAdapter {

    private Context mContext;
    private String[]  Title;
    private int[] imge;

    public CustomAdapter(Context context, String[] text1,int[] imageIds) {
        mContext = context;
        Title = text1;
        imge = imageIds;

    }

    public int getCount() {
        // TODO Auto-generated method stub
        return Title.length;
    }

    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View row;
        if (position % 2 == 1) row = inflater.inflate(R.layout.row, parent, false);
        else row = inflater.inflate(R.layout.row2, parent, false);
        TextView title;
        ImageView i1;
        i1 = (ImageView) row.findViewById(R.id.infoIcon);

        RelativeLayout rl1 = (RelativeLayout) i1.getParent();
        title = (TextView) row.findViewById(R.id.txtTitle);
        title.setText(Title[position]);

        rl1.setBackground(ContextCompat.getDrawable(mContext, imge[position]));
//        i1.setImageResource(imge[position]);

        return (row);
    }
}

