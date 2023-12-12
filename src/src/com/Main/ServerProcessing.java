package com.Main;

import com.CommunicateObject.MOD;
import com.CommunicateObject.Picture;
import com.CommunicateObject.Room;
import com.CommunicateObject.User;
import com.Manage.*;
import com.UI.MainUI;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import static com.CommunicateObject.MODE.PICTURE_MODE;
import static com.CommunicateObject.MODE.SUCCESSED;

public class ServerProcessing {
    public int port =54321;
    public ServerSocket serverSocket=null;
    public JFrame mainFrame;
    public ObjectOutputStream mainOutput;
    public ObjectInputStream mainInput;
    public static RoomManage rm;
    public UserManage um;
    public static ClientManage cm;
    public PictureManage pm;
    public boolean flag =false;
    public Thread clientAccept=null;
    public Thread Roomnext;
    public ServerProcessing(){
        rm = new RoomManage();
        um = new UserManage();
        cm = new ClientManage();
        pm = new PictureManage();
        um.register(new User(1,1));
        um.register(new User(2,2));
        um.register(new User(3,3));


        mainFrame = new JFrame("DreamOut Server");
        mainFrame.setSize(720  ,480);
        mainFrame.setLayout(new GridLayout(0,2));
        mainFrame.setResizable(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        new MainUI(this);
    }
    public void serverOn() {
        try {
            serverSocket = new ServerSocket(port);
            JOptionPane.showMessageDialog(null,"성공적으로 서버를 열었습니다.","성공",JOptionPane.INFORMATION_MESSAGE);
            clientAccept = new Thread(){
                @Override
                public void run() {
                    super.run();
                    while(true){
                        try {
                            Socket s =serverSocket.accept();
                            System.out.println("새로운 클라 접속");
                            Client nc = new Client(s,ServerProcessing.this);
                            cm.add(nc);
                            nc.start();
                        } catch (IOException e) {
                            System.out.println("비 이상적으로 서버가 먼저 닫힘 ㅅㄱ ㅋ");
                        }
                    }
                }
            };
            clientAccept.start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,"이미 서버가 열려있습니다.","실패",JOptionPane.ERROR_MESSAGE);
        }
    }
    public void serverOff() {
        if(serverSocket!=null){
            try{
                serverSocket.close();
                serverSocket = null;
                clientAccept = null;
                JOptionPane.showMessageDialog(null,"서버가 닫혔습니다","정보",JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                System.out.println("서버가 제대로 닫히지 않았습니다.");
            }
        }else{
            JOptionPane.showMessageDialog(null,"서버가 열리지 않았습니다","정보",JOptionPane.WARNING_MESSAGE);
        }
    }
    public static class RoomNext extends Thread{
        Room room;
        public RoomNext(Room r){
            room = rm.getRoom(r);
        }
        @Override
        public void run() {
            super.run();
            int total = rm.getRoom(room).getParticipant().size();
            while(rm.getWait(room)!=total){ //애들 다같이 끝나게 하기
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    System.out.println("뿌려줄까?");
                }
            }
            System.out.println(room +"방 다음으로 넘어가세요");
            RoomSpreadData();
            rm.WaitReset(room);//현재 방 리셋 시작
        }
        private void RoomSpreadData() {//지금 접속한 룸에 있는 모든 사람들에게 뿌려주기
            Room r = rm.getRoom(room);//실시간으로 방 갖고 오고
            //라운드에 따라 하나씩 밀려서 사진 파일 보내기
            Vector<Integer>part = r.getParticipant();
            MOD outMsg = new MOD(SUCCESSED);
            for(int i=0;i< part.size();i++){
                int id = part.get(i);
                Client c = cm.IDtoClient.get(id);//i번째 클라이언트
                try {
                    c.outputStream.writeObject(outMsg);
                    System.out.println(id+"한테 보냈습니다.");
                } catch (IOException e) {
                    System.out.println("모든 클라이언트에게 데이터를 보내지 못 했습니다.");
                }
            }
        }
    }
}
