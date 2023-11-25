package com.Manage;

import com.CommunicateObject.ObjectMsg;
import com.CommunicateObject.Room;
import com.CommunicateObject.User;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;


public class RoomManage implements Serializable {
    private ConcurrentHashMap<Integer,Room> idRoom;

    public RoomManage(){
        idRoom = new ConcurrentHashMap<>();
    }
    public void addRoom(Room room){
        idRoom.put(room.getRoomId(),room);
    }

    public int makeRoom(User u, Room room){
        room.addUser(u);
        addRoom(room);
        return ObjectMsg.SUCESSED;
    }
    public int enterRoom(User u,Room room){
        if(!room.addUser(u))return ObjectMsg.FAILED;//방 들어가기 실패
        return ObjectMsg.SUCESSED;
    }
    public ConcurrentHashMap<Integer,Room> getIdRoom(){return this.idRoom;}
//    public int getRoomIdx(Room room) {
//        if (!roomID.containsKey(room))
//    }

}
