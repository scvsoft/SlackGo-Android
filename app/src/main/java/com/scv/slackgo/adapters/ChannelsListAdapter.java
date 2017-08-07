package com.scv.slackgo.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.scv.slackgo.R;
import com.scv.slackgo.models.Channel;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by ayelen on 10/26/16.
 */

public class ChannelsListAdapter extends ArrayAdapter<Channel> {
    private LinkedHashMap<Channel, Boolean> allChannels;
    private ArrayList<Channel> listChannels;
    private Filter filter;
    private int count;
    private Activity context;

    ViewHolder viewHolder;

    public ChannelsListAdapter(Activity context, int textViewResourceId,
                               List<Channel> unSelectedChannels, List<Channel> selectedChannels) {

        //let android do the initializing :)
        super(context, textViewResourceId, unSelectedChannels);

        this.context = context;

        Collections.sort(unSelectedChannels, Channel.Comparators.NAME);

        this.allChannels = new LinkedHashMap<>(unSelectedChannels.size(), unSelectedChannels.size(), false);
        for (Channel channel: unSelectedChannels) {
            this.allChannels.put(channel, isIn(selectedChannels, channel));
        }
        this.listChannels = new ArrayList<>(allChannels.keySet());
        this.count = unSelectedChannels.size();
    }


    //class for caching the views in a row
    private class ViewHolder {
        TextView channel;
        ImageView checkImage;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.channels_list_row, null, true);
            viewHolder = new ViewHolder();

            viewHolder.channel = (TextView) convertView.findViewById(R.id.channel);
            viewHolder.checkImage = (ImageView) convertView.findViewById(R.id.check_img);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Channel currentChannel = this.listChannels.get(position);
        toogleImage(this.allChannels.get(currentChannel));
        viewHolder.channel.setText(currentChannel.getName());
        return convertView;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    private void toogleImage(boolean checkState){
        if(checkState == true) {
            viewHolder.checkImage.setImageResource(R.drawable.selected);
        } else {
            viewHolder.checkImage.setImageResource(R.drawable.circle);
        }
    }

    private boolean isIn(List<Channel> selectedChannels, final Channel currentChannel) {
        Channel value = IterableUtils.find(selectedChannels, new Predicate<Channel>() {
            @Override
            public boolean evaluate(Channel selectedName) {
                return currentChannel.getName().equals(selectedName.getName());
            }
        });
        return value != null;
    }

    public Channel getChannelFromListPosition(int position) {
        return this.listChannels.get(position);
    }

    public void setChannelSelected(Channel channel, Boolean selected) {
        this.allChannels.put(channel, selected);
    }

    @Override @NotNull
    public Filter getFilter() {
        if(filter == null)
            filter = new ChannelFilter();
        return filter;
    }

    private class ChannelFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            constraint = constraint.toString().toLowerCase();
            FilterResults newFilterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                ArrayList<Channel> filtered = new ArrayList<>();
                for (LinkedHashMap.Entry<Channel, Boolean> entry : allChannels.entrySet()) {
                    Channel key = entry.getKey();
                    Boolean value = entry.getValue();
                    if (key.getName().toLowerCase().contains(constraint)) {
                        filtered.add(key);
                    }
                }
                newFilterResults.count = filtered.size();
                newFilterResults.values = filtered;
            } else {
                newFilterResults.count = allChannels.size();
                newFilterResults.values = new ArrayList<Channel>(allChannels.keySet());
            }
            return newFilterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            listChannels.clear();
            listChannels.addAll((ArrayList<Channel>)results.values);
            count = results.count;
            notifyDataSetInvalidated();
        }
    }
}
