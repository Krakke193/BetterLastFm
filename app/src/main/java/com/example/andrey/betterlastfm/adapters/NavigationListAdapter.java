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
    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_LIST = 1;
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

//        int layoutId = -1;

        View item;
//
//        switch (position){
//            case VIEW_TYPE_HEADER: {
//                layoutId = R.layout.navigation_header;
//                break;
//            }
//            case VIEW_TYPE_LIST: {
//                layoutId = R.layout.item_navigation_drawer;
//                break;
//            }
//        }

        /**
         * TODO: Why do I need to inflate different types of layouts? they are completely different
         * TODO: and share no fields! It is better idea just to put two different items to drawer:
         * TODO: header layout as different object and ListView beneath it!
         */

        if (convertView == null) {
            item = new View(mContext);
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            item = inflater.inflate(R.layout.item_navigation_drawer, parent, false);
        } else {
            item = (View) convertView;
        }

//        if (layoutId == VIEW_TYPE_LIST){
            TextView textView = (TextView) item.findViewById(R.id.item_navigation_drawer_textview);
            ImageView imageView = (ImageView) item.findViewById(R.id.item_navigation_drawer_icon);

            imageView.setImageResource(mThumbIds[position]);
            textView.setText(mStrings[position]);
//        }



        return item;
    }

//    @Override
//    public int getItemViewType(int position) {
//        return (position == 0) ? VIEW_TYPE_HEADER : VIEW_TYPE_LIST;
//    }
//
//    @Override
//    public int getViewTypeCount() {
//        return VIEW_TYPE_COUNT;
//    }
}
