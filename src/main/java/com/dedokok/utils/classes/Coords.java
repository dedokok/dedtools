package com.dedokok.utils.classes;

public class Coords{
    private int x;
    private int y;
    private int z;

    String world;
    public Coords(int x, int y, int z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }
    public int getX(){
        return x;
    }
    public int getY(){return y;}
    public int getZ(){return z;}
    public String getWorld(){return world;}
}