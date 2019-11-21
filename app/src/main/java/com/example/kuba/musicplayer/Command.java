package com.example.kuba.musicplayer;

public class Command {

    String name, operation;
    int id, userId;

    //konstruktory
    public Command(){
    }

    public Command(int id, String name, String operation, int userId){
        this.id = id;
        this.name = name;
        this.operation = operation;
        this.userId = userId;
    }

    public Command(String name, String operation){
        this.name = name;
        this.operation = operation;
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

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
