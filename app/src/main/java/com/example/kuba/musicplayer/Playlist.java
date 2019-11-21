package com.example.kuba.musicplayer;

import java.io.Serializable;

public class Playlist implements Serializable {

    String name;
    int id, userId;

    //konstruktory
    public Playlist(){
    }

    public Playlist(String name, int userId){
        this.name = name;
        this.userId = userId;
    }

    public Playlist(int id, String name, int userId){
        this.id = id;
        this.name = name;
        this.userId = userId;
    }

    //gettery i settery

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
