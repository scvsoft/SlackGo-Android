package com.scv.slackgo.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.scv.slackgo.R;
import com.scv.slackgo.models.Channel;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ayelen on 10/26/16.
 */

public class ChannelsListAdapter extends ArrayAdapter<Channel> {
    public boolean[] checkState;
    Channel[] channels;
    Channel[] selectedChannels;

    private Activity context;

    ViewHolder viewHolder;

    public ChannelsListAdapter(Activity context, int textViewResourceId,
                               List<Channel> unSelectedChannels, List<Channel> selectedChannels) {

        //let android do the initializing :)
        super(context, textViewResourceId, unSelectedChannels);

        this.context = context;

        Collections.sort(unSelectedChannels, Channel.Comparators.NAME);

        this.channels = unSelectedChannels.toArray(new Channel[0]);
        this.selectedChannels = selectedChannels.toArray(new Channel[0]);

        //create the boolean array with
        //initial state as false
        checkState = new boolean[unSelectedChannels.size()];

        for(int i=0; i < channels.length; i++) {
            checkState[i] = isIn(selectedChannels, channels[i]);
        }
    }


    //class for caching the views in a row
    private class ViewHolder {
        TextView channel;
        ImageView checkImage;
    }

    public Integer[] getItemsChecked() {
        List<Integer> itemsChecked = new ArrayList<Integer>();

        for (int i = 0; i < checkState.length; i++) {
            if (checkState[i]) {
                itemsChecked.add(i);
            }
        }

        return itemsChecked.toArray(new Integer[0]);
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.channels_list_row, null, true);
            viewHolder = new ViewHolder();

            viewHolder.channel = (TextView) convertView.findViewById(R.id.channel);
            viewHolder.checkImage = (ImageView) convertView.findViewById(R.id.check_img);


            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        toogleImage(checkState[position]);
        viewHolder.channel.setText(channels[position].getName());
        return convertView;
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

}