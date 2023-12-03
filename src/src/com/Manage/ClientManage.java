package com.Manage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientManage extends Manage{
    public ConcurrentMap<Client,Client>reapintClient= new ConcurrentHashMap<>();
    @Override
    public boolean isContain(Object o) {
        if(o instanceof Client c){
            for(Object temp:data){
                if(!(temp instanceof Client))continue;
                if(c.equals(temp))return true;
            }
        }
        return false;
    }

    @Override
    public boolean add(Object o) {
        if(o instanceof Client c){
            data.add(c);
            return true;
        }
        return false;
    }
    public boolean repaintAdd(Client repaint){
        for(Object c : data){
            if(!(c instanceof Client))continue;
            Client t = (Client) c;
            if (t.ID.equals(repaint.ID)){
                reapintClient.put(t,repaint);//현재 같은 클라이다.
                return true;
            }
        }
        return false;
    }
    public boolean Exit(Client c){
        for(Object temp:data){
            if(c.equals(temp)){
                data.remove(c);
                return true;
            }
        }
        return false;
    }
}
