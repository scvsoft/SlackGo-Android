package com.scv.slackgo.helpers;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.scv.slackgo.R;
import com.scv.slackgo.models.Channel;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ayelen on 10/26/16.
 */

class ChannelsListAdapter extends ArrayAdapter<Channel> {
    // boolean array for storing
    //the state of each CheckBox
    boolean[] checkBoxState;
    Channel[] channels;
    Channel[] selectedChannels;

    private Activity context;


    ViewHolder viewHolder;

    public ChannelsListAdapter(Activity context, int textViewResourceId,
                               List<Channel> unSelectedChannels, List<Channel> selectedChannels) {

        //let android do the initializing :)
        super(context, textViewResourceId, unSelectedChannels);

        this.context = context;

        unSelectedChannels.addAll(selectedChannels);

        Collections.sort(unSelectedChannels, Channel.Comparators.NAME);

        this.channels = unSelectedChannels.toArray(new Channel[0]);
        this.selectedChannels = selectedChannels.toArray(new Channel[0]);

        //create the boolean array with
        //initial state as false
        checkBoxState = new boolean[unSelectedChannels.size()];

        for(int i=0; i < channels.length; i++) {
            checkBoxState[i] = isIn(selectedChannels, channels[i]);
        }
    }


    //class for caching the views in a row
    private class ViewHolder {
        TextView channel;
        CheckBox checkBox;
    }

    public Integer[] getItemsChecked() {
        List<Integer> itemsChecked = new ArrayList<Integer>();

        for (int i = 0; i < checkBoxState.length; i++) {
            if (checkBoxState[i]) {
                itemsChecked.add(i);
            }
        }

        return itemsChecked.toArray(new Integer[0]);
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.list_layout, null, true);
            viewHolder = new ViewHolder();

            viewHolder.channel = (TextView) convertView.findViewById(R.id.channel);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);


            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();


        viewHolder.checkBox.setChecked(checkBoxState[position]);
        viewHolder.channel.setText(channels[position].getName());

        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (((CheckBox) v).isChecked())
                    checkBoxState[position] = true;
                else
                    checkBoxState[position] = false;

            }
        });

        //return the view to be displayed
        return convertView;
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