//package com.Manage;
//
//import com.CommunicateObject.*;
//
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.Socket;
//import java.util.Collection;
//
//public class Client extends Thread{
//    ObjectOutputStream out;
//    ObjectInputStream in;
//    ObjectMsg msg;
//    ObjectMsg outMsg;
//    Socket socket;
//    public Client(Socket sc){
//        socket = sc;
//        try {
//            out = new ObjectOutputStream(socket.getOutputStream());
//            in = new ObjectInputStream(socket.getInputStream());
//        } catch (IOException ignored) {}
//    }
//
//    @Override
//    public void run() {
//        super.run();
//        while(true){
//            try {
//                msg = (ObjectMsg) in.readObject();
//                if(msg == null)continue;
//                switch (msg.getMsgMode()){
//                    case ObjectMsg.LOGIN_MODE -> {
//                        User temp = (User)msg;
//                        outMsg = new MsgMode(UserData.Login(temp));
//                        out.writeObject(outMsg);
//                    }
//                    case ObjectMsg.REGISTER_MODE -> {
//                        User temp = (User)msg;
//                        outMsg = new MsgMode(UserData.Register(temp));
//                        out.writeObject(outMsg);
//                    }
//                    case ObjectMsg.ROOM_MAKE_MODE -> {
//                        User Tempuser = (User)msg;
//                        Room TempRoom = (Room)msg;
//                        outMsg = new MsgMode(RoomData.makeRoom(Tempuser, TempRoom));
//                        out.writeObject(outMsg);//방 만들기 성공
//                    }
//                    case ObjectMsg.ROOM_VIEW -> {
//                        Collection<Room> outData = RoomData.getIdRoom().values();
//                        outMsg = new StringMsg(new MsgMode(ObjectMsg.MSG_MODE),outData.size()+"");
//                        out.writeObject(outMsg);//방의 갯수 먼저 보내주기
//
//                        for(Room temp : outData){
//                            temp.setMsgMode(ObjectMsg.ROOM_VIEW);
//                            outMsg = temp;
//                            out.writeObject(outMsg);
//                        }
//                    }
//
//                }
//            } catch (IOException | ClassNotFoundException err) {
//                System.err.println(err);
//                break;
//            }
//        }
//    }
//}
