package com.Main;

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

public class ServerProcessing {
    public int port =54321;
    public ServerSocket serverSocket=null;
    public JFrame mainFrame;
    public ObjectOutputStream mainOutput;
    public ObjectInputStream mainInput;
    public RoomManage rm;
    public UserManage um;
    public ClientManage cm;
    public PictureManage pm;
    public boolean flag =false;
    public Thread clientAccept=null;
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
}
