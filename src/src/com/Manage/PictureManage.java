package com.Manage;

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
    public ConcurrentMap<Integer, Vector<Vector<JLabel>>>roomToPictures;
    public PictureManage(){
        roomToPictures = new ConcurrentHashMap<>();//생성
    }
}
