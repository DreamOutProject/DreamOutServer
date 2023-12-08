package com.Manage;

import com.CommunicateObject.Picture;
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
        for(Object ID:data){
            if(u.equals(ID)){
                if(((User)ID).isLogin())return false;//이미 로그인 돼있으면 안됨.
                ((User)ID).setLogin(true);//로그인했다고 하기
                return true;
            }
        }
        return false;
    }
    public boolean logout(User u){
        for(Object ID:data) {
            if (u!=null && u.equals(ID)) {
                ((User) ID).setLogin(false);//로그인했다고 하기
                return true;
            }
        }
        return false;
    }

}
