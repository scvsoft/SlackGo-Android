package com.scv.slackgo.services;

import com.android.volley.Response;
import com.scv.slackgo.services.listeners.ChannelsListListener;

/**
 * Created by ayelen@scvsoft.com on 10/12/16.
 */

public interface APIInterface {

    Response authenticate();

    void joinChannel(String channel);

    void leaveChannel(String channel);

    void getAvailableChannels(ChannelsListListener listener);
}
