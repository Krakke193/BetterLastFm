package com.example.andrey.betterlastfm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.andrey.betterlastfm.R;

/**
 * Created by Андрей on 25.03.2015.
 */
public class NavigationListAdapter extends BaseAdapter {
    private Context mContext;
    private int[] mThumbIds = {R.drawable.home, R.drawable.friends};
    private String[] mStrings;

    public NavigationListAdapter(Context context) {
        this.mContext = context;
        mStrings = mContext.getResources().getStringArray(R.array.navigation_drawer_points);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View item;

        if (convertView == null) {
            item = new View(mContext);
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            item = inflater.inflate(R.layout.item_navigation_drawer, parent, false);
        } else
            item = (View) convertView;

        TextView textView = (TextView) item.findViewById(R.id.item_navigation_drawer_textview);
        ImageView imageView = (ImageView) item.findViewById(R.id.item_navigation_drawer_icon);

        imageView.setImageResource(mThumbIds[position]);
        textView.setText(mStrings[position]);

        return item;
    }
}
