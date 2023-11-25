package com.Manage;

import com.CommunicateObject.ObjectMsg;
import com.CommunicateObject.Picture;
import com.CommunicateObject.Room;
import com.CommunicateObject.User;

import java.io.Serializable;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class PictureManage implements Serializable {
    //몇 번방에 몇 번 어느 그림의 데이터가 있는지 맵핑
    private ConcurrentHashMap<Room, Vector<Picture>> RoomPicture;//어느 방에 어느 그림들이 있다.
    public PictureManage(){
        RoomPicture = new ConcurrentHashMap<>();//만들어 주고
    }
    public Vector<Picture> getRoomPicture(Room room){//
        if(!RoomPicture.containsKey(room))return new Vector<>();//빈 백터주기
        return this.RoomPicture.get(room);
    }
    public int newRoom(Room room){//방 만들 때 해당 함수를 호출해야된다.
        Vector<Picture>addPicture = new Vector<>();
        RoomPicture.put(room,addPicture);
        return ObjectMsg.SUCESSED;
    }
    public int newUser(Room room){//여기서 맵핑하자 해당 user의 지금 vector가 몇 번째있는지. 아니면 UserManage한테 물어봐야됨.

        return ObjectMsg.SUCESSED;
    }
    public int addPicture(Room room, User u){//해당 방에 성공적으로 사진을 추가했습니다.
        if(!RoomPicture.containsKey(room))return ObjectMsg.FAILED;//해당 방이 존재하지 않는 상태
        //1. 지금 현재 보낸 유저가 방에서 몇 번째 인덱스인지 알아내야 한다. 그걸 알아낸다음에 그 인덱스에다가 추가해야된다.

        return ObjectMsg.SUCESSED;
    }
    public int resetRoom(Room room){//겜 끝나고 돌아왔을 때는 그림 데이터 다 사라져야 됨.
        if(!RoomPicture.containsKey(room))return ObjectMsg.FAILED;
        RoomPicture.get(room).clear();//날리기
        return ObjectMsg.SUCESSED;
    }
}
