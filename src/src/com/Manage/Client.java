package com.Manage;

import com.CommunicateObject.MOD;
import com.CommunicateObject.MODE;
import com.CommunicateObject.Room;
import com.CommunicateObject.User;
import com.Main.ServerProcessing;

import java.io.*;
import java.net.Socket;
import java.util.Vector;

import static com.CommunicateObject.MODE.*;

public class Client extends Thread{
    public Socket socket;
    public User ID;
    public Room room;
    public ObjectOutputStream outputStream;
    public ObjectInputStream inputStream;
    public ServerProcessing main;
    public Client(Socket socket, ServerProcessing main){
        this.socket = socket;
        this.main = main;
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream  = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        super.run();
        while(true){
            try {
                MOD msg = (MOD)inputStream.readObject();
                System.out.println(msg+ "메세지가 들어왔습니다.");
                switch (msg.getMOD()){
                    case LOGIN_MODE -> {
                        User u = (User)msg;
                        boolean flag = main.um.login(u);
                        MOD outMsg = new MOD(FAILED);
                        if(flag){
                            outMsg.setMod(MODE.SUCCESSED);
                            ID = new User(u);//현재 클라이언트의 아이디 설정
                            //설정하고 나서 ID에 맞는 클라이언트를 서버에 저장
                            main.cm.IDtoClient.put(ID.getId(), this);//ID에 맞게 클라 연결
                        }
                        outputStream.writeObject(outMsg);
                    }
                    case REGISTER_MODE -> {//회원가입 로직 관리
                        User u = (User)msg;
                        boolean flag = main.um.register(u);
                        MOD outMsg = new MOD(FAILED);
                        if(flag) outMsg.setMod(MODE.SUCCESSED);
                        outputStream.writeObject(outMsg);
                    }
                    case ROOM_INFO -> {//방 정보 주세요
                        Vector<Object>data = main.rm.getData();//방의 수를 준다.
                        for(Object r:data){
                            Room outMsg = new Room((Room)r);
                            outputStream.writeObject(outMsg);
                        }
                        MOD outMsg = new MOD(MODE.SUCCESSED);
                        outputStream.writeObject(outMsg);
                    }
                    case ROOM_MAKE_MODE -> {
                        Room r = (Room)msg;
                        boolean flag = main.rm.makeRoom(r.getRoomSize(),ID);
                        MOD outMsg = new MOD(FAILED);
                        if(flag)outMsg.setMod(MODE.SUCCESSED);
                        outputStream.writeObject(outMsg);

                        if(flag){//성공했을 시 다시 방을 던져준다.
                            Room ret = main.rm.getRoom(new Room(ID.getId(),r.getRoomSize()));
                            room = new Room(ret);
                            outputStream.writeObject(ret);
                            BroadRepaint();
                        }
                    }
                    case ROOM_MODE -> {//방 들어가기
                        Room r = (Room)msg;
                        boolean flag = main.rm.enterRoom(r,ID);
                        MOD outMSG = new MOD(FAILED);
                        if(flag)outMSG.setMod(MODE.SUCCESSED);
                        outputStream.writeObject(outMSG);
                        if(flag){
                            Room ret = main.rm.getRoom(r);
                            room = new Room(ret);//현재 방 설정
                            outputStream.writeObject(ret);
                            //다른 모든 유저들에게는 현재 그림을 다시 그리라고 전달해줘야 됨.
                            BroadRepaint();
                        }
                    }
                    case ROOM_VIEW -> {
                        Room r = (Room)msg;
                        Room ret = main.rm.getRoom(r);
                        outputStream.writeObject(ret);//방 정보 주기
                    }
                    case GAME_START_MODE -> {//게임 시작하면 다른 사람들에게도 게임 시작했다고 알려줘야 됨.
                        //Room mode에서 방정보를 알게 되었으니 그 방에 대한 서버 정보를 토대로
                        //같은 방 유저에게 다시 보내준다.
                        SameRoomGameStart();//
                    }
                    case REPAINT_NOTIFY -> {//리페인트가 붙었는지 확인
                        User u = (User)msg;
                        ID = new User(u);
                        boolean flag = main.cm.repaintAdd(this);
                        MOD outMsg = new MOD(FAILED);
                        if(flag)outMsg.setMod(SUCCESSED);
                        outputStream.writeObject(outMsg);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if(main.cm.Exit(this)) System.out.println(this+"가 성공적으로 삭제되었습니다.");
                if(room!=null && main.rm.exitRoom(room,ID)) {
                    if(room.getAdminId() == ID.getId()){
                        if(main.rm.remove(room))System.out.println("방이 삭제되었습니다.");
                    }
                    System.out.println(this+"가 방에서 나왔습니다.");
                }
                if(main.cm.repaintClient.containsKey(this)){
                    main.cm.repaintClient.remove(this);
                    System.out.println("성공적으로 리페인트 객체도 삭제하였습니다.");
                }
                System.out.println(this + "클라이언트 종료하였습니다");
                BroadRepaint();//다른 애들에게 다시 그리라고 말해줘야됨
                break;
            }

        }
    }
    public void SameRoomGameStart(){//방장이 보내는 거임
        Room r = main.rm.getRoom(this.room);//현재 방정보를 토대로 서버에 저장된 방 갖고 오기
        while(r.getMOD()==FAILED)r = main.rm.getRoom(this.room);//현재 방 갖고 오기
        //이 방
        if(main.rm.RoomToggle(r)){
            //해당 방 지금 들어가지 못하도록 만들기
            //제대로 됐는지 확인
            Room t = main.rm.getRoom(r);
            System.out.println(t.getInto());
        }
        MOD outMsg = new MOD(GAME_START_MODE);
        for(int id : r.getParticipant()){//방에 있는 모든 아이디
            if(!main.cm.IDtoClient.containsKey(id)) System.out.println("엥 해당 아이디에 맞는 클라없음");
            else {//클라가 있을 때 그 클라
                Client c = main.cm.IDtoClient.get(id);
                Client repaint = main.cm.repaintClient.get(c);
                try{
                    repaint.outputStream.writeObject(outMsg);
                } catch (IOException e) {
                    System.out.println("다른 유저들에게 게임 시작하라고 보내지 못 하였습니다.");
                }
            }
        }
    }
    public void BroadRepaint() {
        for(Client repaint : main.cm.repaintClient.values()){//
            if(repaint.ID.equals(this.ID))continue;//아이디가 같으면 보내지 않아도 됨.
            MOD outMsg = new MOD(REPAINT_MODE);
            try {
                repaint.outputStream.writeObject(outMsg);
                System.out.println("메세지는 보냈습니다.");
            } catch (IOException e) {
                System.out.println("REPAINT 메세지를 제대로 보내지 못했습니다.");
            }
        }
    }
}
