package com.scv.slackgo.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ListView;
import android.widget.TextView;

import com.scv.slackgo.R;
import com.scv.slackgo.models.Channel;
import com.scv.slackgo.models.Location;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.map.HashedMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by SCV on 10/25/16.
 */

public class ChannelListHelper {

    public static void buildList(final Activity context, final List<Channel> values, ListView channelsListView) {

        channelsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        channelsListView.setItemsCanFocus(false);

        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle("Available Channels");

        final ChannelsListAdapter adapter = new ChannelsListAdapter(context,
                R.layout.list_layout, values);

        builderSingle.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = adapter.getItem(which).getName();
            }
        });

        builderSingle.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Integer[] channelIds = adapter.getItemsChecked();

                        String concatChannels = "";

                        for (int i = 0; i < values.size(); i++) {
                            if (isIn(channelIds, i)) {
                                if (!concatChannels.equals("")) {
                                    concatChannels = concatChannels.concat(", ");
                                }
                                concatChannels = concatChannels.concat(values.get(i).getName());
                            }
                        }
                        IterableUtils.forEach(values, new Closure<Channel>() {
                            @Override
                            public void execute(Channel input) {

                            }
                        });

                        ((TextView) context.findViewById(R.id.selected_channels)).setText(context.getText(R.string.channels_to_join) + concatChannels);
                    }
                });

        final AlertDialog channelsAlert = builderSingle.create();


        channelsAlert.show();
    }

    private static boolean isIn(Integer[] ids, final int id) {
        Integer value = IterableUtils.find(Arrays.asList(ids), new Predicate<Integer>() {
            @Override
            public boolean evaluate(Integer index) {
                return index == id;
            }
        });
        return value != null;
    }

    public static List<String> channelsFromTextViewString(String textViewString) {
        if (!textViewString.equals("")) {
            String channels = textViewString.split(":")[1].trim();
            return Arrays.asList(channels.split(","));
        } else {
            return new ArrayList<String>();
        }
    }

    public static List<String> channelsByIDFromTextViewString(List<String> channelsByName, List<Channel> channels) {
        List<String> channelsById = new ArrayList<String>();
        for (Channel channel : channels ){
            for (String channelName : channelsByName){
                if (channelName.equals(channel.getName())){
                    channelsById.add(channel.getId());
                }
            }
        }
        return channelsById;
    }


    public static Map<String, List<String>>  channelsByNameFromLocations(List<Location> locations){
        Map<String,List<String>> channelLocationMap = new HashedMap<>();

        for (Location loc : locations) {
            channelLocationMap.put(loc.getName(),loc.getChannelsByName());
        }

        return channelLocationMap;
    }

}
