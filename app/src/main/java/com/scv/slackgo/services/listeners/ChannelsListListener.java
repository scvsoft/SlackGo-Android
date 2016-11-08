package com.scv.slackgo.services.listeners;

import android.content.Context;

import com.scv.slackgo.models.Channel;

import java.util.List;

/**
 * Created by scvsoft on 11/7/16.
 */
public interface ChannelsListListener extends GenericListener {
    void onResponse(List<Channel> channels);
}
