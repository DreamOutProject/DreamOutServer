package com.Manage;

import com.CommunicateObject.MODE;
import com.CommunicateObject.Room;
import com.CommunicateObject.User;

public class RoomManage extends Manage{
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
}
