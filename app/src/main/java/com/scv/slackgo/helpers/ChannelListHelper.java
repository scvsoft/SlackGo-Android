package com.scv.slackgo.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.scv.slackgo.R;
import com.scv.slackgo.activities.LocationActivity;
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

    static List<Channel> filteredValues;

    public static void buildList(final LocationActivity context,final List<Channel> values, ListView channelsListView) {

        channelsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        channelsListView.setItemsCanFocus(false);

        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle("Available Channels");

        final ChannelsListAdapter adapter = getChannelsAdapter(context, values);

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
                        List<Channel> selectedChannels = new ArrayList<Channel>();

                        for (int i = 0; i < values.size(); i++) {
                            if (isIn(channelIds, i)) {
                                if (!concatChannels.equals("")) {
                                    concatChannels = concatChannels.concat(", ");
                                }
                                concatChannels = concatChannels.concat(values.get(i).getName());
                                selectedChannels.add(values.get(i));
                            }
                        }

                        IterableUtils.forEach(values, new Closure<Channel>() {
                            @Override
                            public void execute(Channel input) {

                            }
                        });

                        context.setSelectedChannels(selectedChannels);

                        ((TextView) context.findViewById(R.id.selected_channels)).setText(concatChannels);
                    }
                });

        final AlertDialog channelsAlert = builderSingle.create();


        channelsAlert.show();
    }

    private static ChannelsListAdapter getChannelsAdapter(LocationActivity context, List<Channel> values) {

        final List<Channel> selectedValues = getSelectedValues(context, values);


        filteredValues = Lists.newArrayList(IterableUtils.filteredIterable(values, new Predicate<Channel>() {
            @Override
            public boolean evaluate(Channel channel) {
                return !isIn(selectedValues, channel.getName());
            }
        }));

        return new ChannelsListAdapter(context, R.layout.list_layout, filteredValues, selectedValues);
    }

    private static List<Channel> getSelectedValues(LocationActivity context, List<Channel> values) {

        final List<Channel> selectedChannels = context.getSelectedChannels();

        return Lists.newArrayList(IterableUtils.filteredIterable(values, new Predicate<Channel>() {
            @Override
            public boolean evaluate(Channel channel) {
                return isIn(selectedChannels, channel.getName());
            }
        }));

    }

    private static boolean isIn(List<Channel> selectedChannels, final String channelName) {
        Channel value = IterableUtils.find(selectedChannels, new Predicate<Channel>() {
            @Override
            public boolean evaluate(Channel selectedName) {
                return channelName.equals(selectedName.getName());
            }
        });
        return value != null;
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

    public static Map<String, List<Channel>> getChannelsListForLocations(List<Location> locations) {
        Map<String, List<Channel>> channelLocationMap = new HashedMap<>();
        for (Location loc : locations) {
            channelLocationMap.put(loc.getName(), loc.getChannels());
        }
        return channelLocationMap;
    }

}
