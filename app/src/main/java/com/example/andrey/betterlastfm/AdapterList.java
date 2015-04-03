package com.example.andrey.betterlastfm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by andrey on 01.03.15.
 */
public class AdapterList extends ArrayAdapter<RecentTrack> {
    private Context mContext;

    public AdapterList(Context context, int resource) {
        super(context,resource);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecentTrack recentTrack = getItem(position);

        View viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, null);


        }

        ((TextView) convertView.findViewById(R.id.list_textview)).setText(recentTrack.trackInfo);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.list_imageview);

        //new TaskDownloadImage(imageView).execute(recentTrack.trackImageURL);
        //new DownloadImageLoader(context, imageView, recentTrack.trackImageURL).forceLoad();
        Picasso.with(mContext).load(recentTrack.trackImageURL).into(imageView);

        return convertView;
    }
}

class RecentTrack {

    public String trackInfo;
    public String trackImageURL;

    public RecentTrack (String trackInfo, String trackImageURL){
        this.trackInfo = trackInfo;
        this.trackImageURL = trackImageURL;
    }

    @Override
    public String toString() {
        return trackInfo;
    }
}
