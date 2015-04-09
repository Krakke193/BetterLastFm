package com.example.andrey.betterlastfm.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.andrey.betterlastfm.R;
import com.example.andrey.betterlastfm.data.TopArtist;
import com.squareup.picasso.Picasso;

/**
 * Created by andrey on 01.03.15.
 */
public class ArtistsAdapter extends ArrayAdapter<TopArtist> {
    private Context mContext;

    public ArtistsAdapter(Context context, int resource) {
        super(context,resource);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TopArtist topArtist = getItem(position);

        View viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_top_artists_grid, null);


        }

        ((TextView) convertView.findViewById(R.id.tv_artists_name_grid)).setText(topArtist.artistInfo);
        ((TextView) convertView.findViewById(R.id.tv_artists_playcount_grid)).setText(topArtist.artistPlaycount);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_grid);

        //new TaskDownloadImage(imageView).execute(topArtist.artistImageURL);
        //new DownloadImageLoader(context, imageView, topArtist.artistImageURL).forceLoad();
        //Picasso.with(mContext).setIndicatorsEnabled(true);
        Picasso.with(mContext).load(topArtist.artistImageURL).resize(220,220).centerCrop().into(imageView);

        return convertView;
    }
}

