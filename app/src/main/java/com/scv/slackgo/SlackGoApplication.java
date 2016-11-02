package com.scv.slackgo;

import android.app.Application;

/**
 * Created by kado on 11/2/16.
 */

public class SlackGoApplication extends Application {
    private static SlackGoApplication instance;
    public static SlackGoApplication get() { return instance; }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
