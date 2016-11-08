package com.scv.slackgo.services.listeners;

import com.scv.slackgo.models.Channel;

import java.util.List;

/**
 * Created by scvsoft on 11/8/16.
 */

public interface SlackTokenListener extends GenericListener {
    void onResponse();
}
