package com.dedokok.utils.classes;

import com.dedokok.systems.modules.Feature.BlockBreakFinder;

import java.util.ArrayList;

public class Vein{
    public int id;
    public ArrayList<Row> rows = new ArrayList<>();
    public int size;
    public boolean isRemoved = false;
    public Vein(ArrayList<Row> rows, int size){
        this.rows=rows;
        this.size=size;
    }
    public Vein(){}

}
