package com.Manage;

import com.CommunicateObject.*;
import com.Main.ServerProcessing;

import javax.swing.*;
import java.awt.*;
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
    public boolean IDSet=false;
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
                System.out.println(msg+ "메세지가 들어왔습니다." + this);
                switch (msg.getMOD()){
                    case LOGIN_MODE -> {
                        User u = (User)msg;
                        boolean flag = main.um.login(u);
                        MOD outMsg = new MOD(FAILED);
                        if(flag){
                            outMsg.setMod(MODE.SUCCESSED);
                            ID = new User(u);//현재 클라이언트의 아이디 설정
                            System.out.println(this + "ID 설정 완료");
                            //설정하고 나서 ID에 맞는 클라이언트를 서버에 저장
                            IDSet=true;
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
                    case GAME_START_MODE, GAME_TWO_CHOICE,GAME_ONE_CHOICE -> {//게임 시작하면 다른 사람들에게도 게임 시작했다고 알려줘야 됨.
                        //Room mode에서 방정보를 알게 되었으니 그 방에 대한 서버 정보를 토대로
                        //같은 방 유저에게 다시 보내준다.
                        if(msg.getMOD() == GAME_TWO_CHOICE){
                            main.rm.setChoice(room,2);
                        }else if(msg.getMOD() == GAME_ONE_CHOICE){
                            main.rm.setChoice(room,1);
                        }else{//게임 시작
                            Room r = main.rm.getRoom(room);
                            main.pm.newAlbum(r);
                            main.rm.newWait(r);
                            main.rm.RoomToggle(r);//토글해주기
                            //서버에서 방에 맞게 해당 앨범을 새로 만든다.
                        }
                        SameRoomBroadCast(msg);//
                    }
                    case REPAINT_NOTIFY -> {//리페인트가 붙었는지 확인
                        User u = (User)msg;
                        ID = new User(u);
                        boolean flag = main.cm.repaintAdd(this);
                        MOD outMsg = new MOD(FAILED);
                        if(flag)outMsg.setMod(SUCCESSED);
                        outputStream.writeObject(outMsg);
                    }
                    case PICTURE_INFO->{//그림 데이터 주세요
                        //현재 이 클라이언트가 갖고 있는 방 데이터를 통해 서버에 저장
                        //현재 ID에다가 ROUND수를 더한 값을 설정한다.
                        Room r= main.rm.getRoom(room); //해당 방
                        Vector<Picture>data = main.pm.getAlbum(r); //해당 방에 모든 그림 파일들
                        Vector<Integer>part = r.getParticipant();//같은 방 안에 있는 데이터
                        for(int i=0;i<part.size();i++){
                            if(part.get(i) == ID.getId()){//이 인덱스에다가
                                MOD outMsg = new Picture(data.get((i+r.getRound()-1)%part.size()));//이 인덱스에다가 현재 진행 중인 인덱스 더해주기
                                outMsg.setMod(PICTURE_MODE);
                                outputStream.writeObject(outMsg);//데이터 보내기
                                System.out.println(part.get(i)+"에게 그림데이터를 보냈습니다.");
                            }
                        }
                    }
                    case PICTURE_MODE -> {//클라에서 사진 데이터를 보낸 거임
                        Room r = main.rm.getRoom(room);
                        Picture data = (Picture) msg;
                        System.out.println(data.getFiles().get(r.getRound()-1).getIcon());
                        main.pm.setRoundAlbum(r,data,ID);//해당하는 라운드에 맞는 데이터를 넣기
                    }
                    case NEXT_ROUND -> {//다음 라운드로 넘기기
                        System.out.println("다음 라운드로 넘어갑니다.");
                        main.rm.nextRound(room);//한 방에 한 번만 시행되어야 함.
                    }
                    case TEMP,WAITING->{//게임 끝?
                        //해당 모든 방에 아이들이 끝인지 확인
                        //일단 끝났는지 true
                        new ServerProcessing.RoomNext(room).start();
                        main.rm.WaitIncrease(room);//내 거 하나 증가
                    }
                    case GAME_END -> {
                        main.rm.WaitReset(room);//해당 방 리셋시켜놓기
                        main.rm.EndGame(room);
                        Vector<Picture>P= new Vector<>(main.pm.getAlbum(room));
                        Room r = main.rm.getRoom(room);
                        for(Picture temP : P){
                            MOD outMsg = new Picture(temP);
                            outputStream.writeObject(outMsg);
                        }
                        MOD outMsg = new MOD(SUCCESSED);
                        outputStream.writeObject(outMsg);
                    }
                    case ENDING_START_MODE,ENDING_NEXT_MODE,ENDING_PREV_MODE,RETURN_GAMEROOM -> {
                        //같은 방에 있는 사람들에게 첫번 째 앨범 시작하라고 보내기
                        if(msg.getMOD() == RETURN_GAMEROOM){
                            Room r = main.rm.getRoom(room);
                            main.rm.RoomToggle(r);//토글해주기
                        }
                        SameRoomBroadCast(msg);
                    }
                    case RETURN_WAITROOM  -> {
                        Room r = main.rm.getRoom(room);
                        boolean flag =main.rm.exitRoom(r,ID);
                        MOD outMsg = new MOD(FAILED);
                        if(flag){
                            outMsg.setMod(SUCCESSED);
                            if(r.getAdminId() == ID.getId()){//아이디가 같으므로 삭제
                                main.rm.remove(r);//방삭제하기
                            }
                            this.room = null;//널 주기
                        }
                        outputStream.writeObject(outMsg);
                        BroadRepaint();
                    }
                    case CATEGORY_ONE->{
                        main.rm.setTopic(room,1);
                        SameRoomBroadCast(msg);
                    }
                    case CATEGORY_TWO->{
                        main.rm.setTopic(room,2);
                        SameRoomBroadCast(msg);
                    }
                    case CATEGORY_THREE->{
                        main.rm.setTopic(room,3);
                        SameRoomBroadCast(msg);
                    }case CATEGORY_FOUR->{
                        main.rm.setTopic(room,4);
                        SameRoomBroadCast(msg);
                    }case CATEGORY_FIVE ->{
                        main.rm.setTopic(room,5);
                        SameRoomBroadCast(msg);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if(main.cm.Exit(this)) System.out.println(this+"가 성공적으로 삭제되었습니다.");
                if(room!=null && main.rm.exitRoom(room,ID)) {
                    if(room.getAdminId() == ID.getId()){
                        if(main.rm.remove(room))System.out.println("방이 삭제되었습니다.");
                        main.pm.removeAlbum(room);
                    }
                    System.out.println(this+"가 방에서 나왔습니다.");
                }
                if(main.cm.repaintClient.containsKey(this)){
                    main.cm.repaintClient.remove(this);
                    System.out.println("성공적으로 리페인트 객체도 삭제하였습니다.");
                }
                if(main.um.logout(ID))System.out.println("성공적으로 "+ ID +" 로그아웃합니다.");
                System.out.println(this + "클라이언트 종료하였습니다");
                BroadRepaint();//다른 애들에게 다시 그리라고 말해줘야됨
                break;
            }

        }
    }



    public void SameRoomBroadCast(MOD outMsg){//방장이 보내는 거임
        Room r = main.rm.getRoom(this.room);//현재 방정보를 토대로 서버에 저장된 방 갖고 오기
        while(r.getMOD()==FAILED)r = main.rm.getRoom(this.room);//현재 방 갖고 오기
        Vector<Integer>list = r.getParticipant();
        for(int i=0;i<list.size();i++){
            int id=list.get(i);
            if(!main.cm.IDtoClient.containsKey(id)) System.out.println("엥 해당 아이디에 맞는 클라없음");
            else {//클라가 있을 때 그 클라
                Client c = main.cm.IDtoClient.get(id);
                Client repaint = main.cm.repaintClient.get(c);
                if(repaint==null)continue;//
                try{
                    repaint.outputStream.writeObject(outMsg);//게임 시작하라고 보내주기
                } catch (IOException e) {
                    System.out.println("같은 방에 있는 유저들에게 보내지 못 하였습니다.");
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
