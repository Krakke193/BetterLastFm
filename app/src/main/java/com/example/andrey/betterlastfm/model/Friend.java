package com.example.andrey.betterlastfm.model;

/**
 * Created by Andrey on 08.04.2015.
 */
public class Friend {

    private String friendName;
    private String friendImageURL;

    public Friend (String friendName, String friendImageURL){
        this.setFriendName(friendName);
        this.setFriendImageURL(friendImageURL);
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendImageURL() {
        return friendImageURL;
    }

    public void setFriendImageURL(String friendImageURL) {
        this.friendImageURL = friendImageURL;
    }
}
