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
        int roomid = room.getRoomId();
        if(!idRoom.containsKey(roomid))return ObjectMsg.FAILED;
        if(!idRoom.get(roomid).addUser(u))return ObjectMsg.FAILED;//방 들어가기 실패
        return ObjectMsg.SUCESSED;
    }
    public Room getRoom(Room room){
        int roomid = room.getRoomId();
        if(!idRoom.containsKey(roomid)){
            room.setMsgMode(ObjectMsg.FAILED);
            return room;//실패한 룸 보내주기
        }
        return idRoom.get(roomid);//서버에 저장된 룸으로 보내주기
    }
    public ConcurrentHashMap<Integer,Room> getIdRoom(){return this.idRoom;}
//    public int getRoomIdx(Room room) {
//        if (!roomID.containsKey(room))
//    }

}
