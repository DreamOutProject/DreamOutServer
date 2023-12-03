package com.Manage;

import com.CommunicateObject.Room;

import java.util.Objects;
import java.util.Vector;

public abstract class Manage {
    public Vector<Object>data;
    public Manage(){data = new Vector<>();}

    public abstract boolean isContain(Object o);

    public abstract boolean add(Object o);
    public Vector<Object>getData(){return this.data;}

    public boolean remove(Object o){
        for(Object d:data){
            if(d.equals(o)){
                data.remove(d);
                return true;
            }
        }
        return false;
    }
}
