package com.Manage;

import com.CommunicateObject.MsgMode;
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
    public void addRoom(Room room){//서버에 해당 방 등록
        idRoom.put(room.getRoomId(),room);
    }

    public Room makeRoom(User u,Integer roomsize){//자동으로 서버에서 방을 만들고 그 방을 클라이언트에게 알려주기
        Room newRoom = new Room(new MsgMode(ObjectMsg.SUCESSED),u.getId(),u.getId(),roomsize);
        newRoom.addUser(u);
        addRoom(newRoom);
        return newRoom;
    }
    public int enterRoom(User u,Integer roomid){
        if(!idRoom.containsKey(roomid))return ObjectMsg.FAILED;//방이 없음
        if(!idRoom.get(roomid).addUser(u))return ObjectMsg.FAILED;//방 들어가기 실패
        return ObjectMsg.SUCESSED;
    }
    public Room getRoom(Integer roomid){
        if(!idRoom.containsKey(roomid)){
            Room room = new Room(new MsgMode(ObjectMsg.FAILED),0,0,0);
            return room;//실패한 룸 보내주기
        }
        Room temp = idRoom.get(roomid);
        temp.setMsgMode(ObjectMsg.SUCESSED);
        return temp;//서버에 저장된 룸으로 보내주기
    }
    public ConcurrentHashMap<Integer,Room> getIdRoom(){return this.idRoom;}
}
