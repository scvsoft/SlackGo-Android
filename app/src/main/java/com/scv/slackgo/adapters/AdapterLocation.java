package com.scv.slackgo.adapters;


import android.content.Context;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.greenfrvr.hashtagview.HashtagView;
import com.scv.slackgo.R;
import com.scv.slackgo.exceptions.GeocoderException;
import com.scv.slackgo.helpers.ErrorUtils;
import com.scv.slackgo.helpers.MapHelper;
import com.scv.slackgo.models.Channel;
import com.scv.slackgo.models.Location;

import java.util.List;

/**
 * Created by scvsoft on 11/10/16.
 */


public class AdapterLocation extends BaseAdapter {
    List<Location> locationsList;
    Context context;

    public AdapterLocation(Context activity, List<Location> locations) {
        try {
            this.context = activity;
            this.locationsList = locations;
        } catch (Exception e) {

        }
    }

    @Override
    public int getCount() {
        return locationsList.size();
    }

    public Location getItem(int position) {
        return locationsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context,
                    R.layout.locations_list_row, null);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Location item = getItem(position);
        holder.locationName.setText(item.getName());
        holder.channelsTags.setData(item.getChannels(), CHANNEL);
        try {
            holder.locationAddress.setText(MapHelper.getAddressFromLocation(context, item));
        } catch (GeocoderException e) {
            ErrorUtils.toastError(context, e.getMessage(), Toast.LENGTH_SHORT);
        }

        return convertView;
    }

    public static class ViewHolder {
        public TextView locationName;
        public TextView locationAddress;
        public HashtagView channelsTags;

        public ViewHolder(View view) {
            locationName = (TextView) view.findViewById(R.id.location_name);
            locationAddress = (TextView) view.findViewById(R.id.location_address);
            channelsTags = (HashtagView) view.findViewById(R.id.channels_tags);
            view.setTag(this);
        }
    }

    public static final HashtagView.DataTransform<Channel> CHANNEL = new HashtagView.DataTransform<Channel>() {
        @Override
        public CharSequence prepare(Channel item) {
            SpannableString spannableString = new SpannableString("#" + item.getName());
            return spannableString;
        }
    };
}