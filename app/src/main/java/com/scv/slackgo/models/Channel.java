package com.scv.slackgo.models;

import com.scv.slackgo.helpers.Constants;

import java.io.Serializable;

/**
 * Created by ayelen@scvsoft.com on 10/20/16.
 */

public class Channel implements Serializable {

    String name;
    boolean isArchived;
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Channel(String name, boolean isArchived, String id) {
        this.name = name;
        this.isArchived = isArchived;
        this.id = id;
    }

    public Channel() {
        this(Constants.OFFICE, false, Constants.OFICINA_CHANNEL_ID);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isArchived() {
        return isArchived;
    }

}
