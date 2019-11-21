package com.example.kuba.musicplayer;

import java.io.Serializable;

public class Song implements Serializable {
    String name, path;
    int id, playlistId;

    //konstruktory
    public Song(){
    }

    public Song(String name, String path){
        this.id = id;
        this.name = name;
        this.path = path;
        this.playlistId = playlistId;
    }

    public Song(int id, String name, String path, int playlistId){
        this.id = id;
        this.name = name;
        this.path = path;
        this.playlistId = playlistId;
    }

    //gettery i settry

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }
}
