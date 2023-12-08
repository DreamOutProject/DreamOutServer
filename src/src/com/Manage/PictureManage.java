package com.Manage;

import com.CommunicateObject.Picture;
import com.CommunicateObject.Room;

import javax.swing.*;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//TO do.
/*
* 해당 부분은 룸에 대해 어느 사진첩이 있는지 확인하는 것 그렇기 때문에 어느 방에 정보에 대해
* 무슨 사진첩이 있는지 확인해야 된다.
* */
public class PictureManage{
    public ConcurrentMap<Integer, Vector<Picture>>roomToPictures;
    //id에 저장된 index에 맞게 picture
    //해당 방에 대한 사진 정보들
    public PictureManage(){
        roomToPictures = new ConcurrentHashMap<>();//생성
    }
    public void newAlbum(Room r){
        Vector<Picture>newAlbum = new Vector<>();//큰 사진첩 만들기
        int part = r.getParticipant().size();
        for(int i=0;i<part;i++){//사람 수만큼 앨범 미리 만들어서 넣어놓자
            Picture temp = new Picture(part);
            newAlbum.add(temp);//
        }
        roomToPictures.put(r.getRoomId(),newAlbum);
    }

    public Vector<Picture> getAlbum(Room r){
        if(!roomToPictures.containsKey(r.getRoomId()))return new Vector<>();
        return roomToPictures.get(r.getRoomId());//데이터 갖고 오기
    }
    public boolean setAlbum(Room r,Vector<Picture> album){
        if(!roomToPictures.containsKey(r.getRoomId()))return false;//실패
        roomToPictures.put(r.getRoomId(),album);//서버에서 갖고 온 데이터를 다시 설정
        return true;
    }
}
