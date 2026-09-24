package com.dedokok.utils.classes;

public class Row{
    private Long coords;
    private String block;
    private String user;
    private long timestamp;
    private String world;
    public Row(Long coords,String block, String user, long timestamp, String world){
        this.block = block;
        this.coords=coords;
        this.user = user;
        this.timestamp = timestamp;
        this.world = world;
    }
    public Row(){};

    public void setBlock(String block){
        this.block = block;
    }
    public String getBlock(){
        return this.block;
    }

    public void setUser(String user){
        this.user = user;
    }
    public String getUser(){return this.user;}

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }
    public long getTimestamp(){return this.timestamp;}

    public void setWorld(String world){
        this.world = world;
    }
    public String getWrld(){return this.world;}

    public void setCoords(Long coords){
        this.coords=coords;
    }
    public Long getCoords(){return this.coords;}


}
