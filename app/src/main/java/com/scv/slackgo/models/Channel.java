package com.scv.slackgo.models;

import com.scv.slackgo.helpers.Constants;

import java.io.Serializable;

import java.util.Comparator;

/**
 * Created by ayelen@scvsoft.com on 10/20/16.
 */


public class Channel implements Comparable<Channel>, Serializable{

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

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    @Override
    public int compareTo(Channel another) {
        return Comparators.NAME.compare(this, another);
    }

    public static class Comparators {

        public static Comparator<Channel> NAME = new Comparator<Channel>() {
            @Override
            public int compare(Channel o1, Channel o2) {
                return o1.name.compareTo(o2.name);
            }
        };
    }
}
