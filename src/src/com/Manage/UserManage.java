package com.Manage;

import com.CommunicateObject.User;

public class UserManage extends Manage{

    @Override
    public boolean isContain(Object o) {
        if(o instanceof User u){
            for(Object temp:data){
                if(!(temp instanceof User))continue;
                if(u.equals(temp))return true;
            }
        }
        return false;
    }

    @Override
    public boolean add(Object o) {
        if(o instanceof User u){
            data.add(u);
            return true;
        }
        return false;
    }

    public boolean register(User u){
        if(isContain(u))return false;//실패
        return add(u);//현재 데이터 넣어주기;
    }
    public boolean login(User u){
        return isContain(u);
    }
}
