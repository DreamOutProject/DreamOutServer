package com.Manage;

import com.CommunicateObject.MODE;
import com.CommunicateObject.Room;
import com.CommunicateObject.User;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManage extends Manage{
    public final Vector<Integer> Wait;//현재 방에 기다리고 있는 사람의 수
    public Vector<Integer>Roomid;//wait를 가리키는 포인터 느낌
    public RoomManage(){

        Wait = new Vector<>();//생성
        Roomid = new Vector<>();
    }
    public boolean WaitReset(Room r){
        for(int i=0;i<Roomid.size();i++){
            if(r.getRoomId() == Roomid.get(i)){
                synchronized (Wait){
                    Wait.set(i,0);
                    return true;
                }
            }
        }
        return false;
    }
    public synchronized void WaitIncrease(Room r){
        for(int i=0;i<Roomid.size();i++){
            if(r.getRoomId() == Roomid.get(i)){
                synchronized (Wait){
                    int cnt = Wait.get(i);
                    Wait.set(i,cnt+1);
                }
            }
        }
    }
    @Override
    public boolean isContain(Object o) {
        if(o instanceof Room r){
            for(Object temp: data){
                if(!(temp instanceof Room))continue;
                if(r.equals(temp))return true;
            }
        }
        return false;
    }
    public boolean RoomToggle(Room r){
        for(Object temp:data){
            if(r.equals(temp)){
                ((Room)temp).setInto(!((Room) temp).getInto());
                return true;
            }
        }
        return false;
    }
    public Room getRoom(Room r){
        Room ret;
        for(Object temp:data){
            if(r.equals(temp)){
                ret = new Room((Room)temp);
                ret.setMod(MODE.SUCCESSED);
                return ret;
            }
        }
        ret = new Room(r);
        ret.setMod(MODE.FAILED);
        return ret;
    }
    public boolean enterRoom(Room r, User u){
        if(!isContain(r))return false;
        Room curR = getRoom(r);
        if(!curR.getInto())return false;//들어갈 수 없다면 애초에 안 됨.
        for(Object temp:data){
            if(curR.equals(temp)){
                return ((Room)temp).addUser(u);
            }
        }
        return false;
    }
    public boolean makeRoom(int size,User u){
        Room nr = new Room(u.getId(),size);
        if(!isContain(nr)){
            data.remove(nr);//원래 있던 방 삭제하기
        }
        if(!nr.addUser(u))return false;
        return add(nr);//현재 방 만들기
    }
    public boolean exitRoom(Room r,User u){
        for(Object temp:data){
            if(r.equals(temp)){
                Room t = (Room)temp;
                return t.removeUser(u);
            }
        }
        return false;
    }
    @Override
    public boolean add(Object o) {
        if(o instanceof Room r){
            data.add(r);
            return true;
        }
        return false;
    }
    public boolean setChoice(Room r,int num){
        for(Object d:data){
            if(r.equals(d)){
                ((Room)d).setGamecategory(num);
                return true;
            }
        }
        return false;
    }
    public boolean nextRound(Room r){
        for(Object t:data){
            if(r.equals(t)){//해당하는 방 찾아냄
                ((Room)t).nextRound();
                return true;
            }
        }
        return false;
    }
    public boolean EndGame(Room r){
        for(int i=0;i<Roomid.size();i++){
            if(r.getRoomId() == Roomid.get(i)){
                Wait.remove(i);
                Roomid.remove(i);
                return true;
            }
        }
        return false;
    }
    public synchronized void newWait(Room room) {
        Roomid.add(room.getRoomId());
        Wait.add(0);//0이라고 넣기
    }
    public int getWait(Room room){
        int value=0;
        for(int i=0;i< Roomid.size();i++){
            if(room.getRoomId() == Roomid.get(i)){
                value = Wait.get(i);
            }
        }
        return value;
    }
}
