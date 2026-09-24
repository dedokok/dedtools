package com.dedokok.utils.classes;

public class PlayerNote{
    public String username;
    public String description;
    public boolean fromTracker;
    public PlayerNote(String username, String description, boolean fromTracker) {
        this.username = username;
        this.description = description;
        this.fromTracker = fromTracker;
    }
    public PlayerNote(){}
    public String getUsername(){
        return username;
    }
    public String getDescription(){
        return description;
    }

    public void  setUsername(String username){
        this.username = username;
    }
    public void  setDescription(String description){
        this.description = description;
    }
}
