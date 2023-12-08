package com.Manage;

import com.CommunicateObject.MODE;
import com.CommunicateObject.Room;
import com.CommunicateObject.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManage extends Manage{
    private Map<Integer, Integer>Wait;//모두 끝이 났는지 파악하기 위해
    public RoomManage(){
        Wait = new ConcurrentHashMap<>();//생성
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

    public void newWait(Room room) {
        Wait.put(room.getRoomId(),0);//데이터 새로 만들기
    }
    public synchronized void setWait(Room room){
        int waitCnt = Wait.get(room.getRoomId());
        Wait.put(room.getRoomId(),waitCnt+1);
        System.out.println("Wait cnt : " + Wait.get(room.getRoomId()));
    }
    public int getWait(Room room){
        return Wait.get(room.getRoomId());
    }
}
