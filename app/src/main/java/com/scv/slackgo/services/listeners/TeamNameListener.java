package com.scv.slackgo.services.listeners;

/**
 * Created by scvsoft on 11/7/16.
 */
public interface TeamNameListener extends GenericListener {
    void onResponse(String team);
}