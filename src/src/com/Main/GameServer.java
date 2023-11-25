package com.Main;

import com.CommunicateObject.*;
import com.Manage.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
/*
*  1.서버에서 해줘야 되는 것들
	-1.  회원정보 갖고 있기
		-1. Id
		-2. Pw
		-3. (업데이트)닉네임
	-2. 방 정보 갖고 있어야 됨.
		-1. 방 이름
		-2. 방장
		-3. 방 안에 id들
		-4. 방 인원(vector로 관리?)
	-3. 시간 관리
	-4. 각 client의 스레드 관리
	*
	*
	*
* */
public class GameServer extends JFrame {
    private ServerSocket SS;//ServerSocket;
    private final int Port;
//    private final Vector<ClientManage> clients;
    private Thread temp;
    private final UserManage UserData;
    private final RoomManage RoomData;
    private TextArea log_display;
    GameServer(int port) {//현재 프레임에 서버 시작 버튼 만들기
//        clients = new Vector<>();
        UserData = new UserManage();
        RoomData = new RoomManage();

        setSize(500,300);
        setDefaultLookAndFeelDecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);


        this.Port = port;
        //버튼 객체
        JButton b_start = ButtonInit("서버켜기");
        JButton b_stop = ButtonInit("서버끄기");

        //시간
        LocalDateTime date = LocalDateTime.now();
        JLabel time_display = new JLabel(date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 E h시 m분 s초")));
        time_display.setHorizontalAlignment(JLabel.CENTER);
        time_display.setFont(new Font("맑은 고딕",Font.BOLD,18));


        JPanel center = new JPanel(new BorderLayout());
        log_display = new TextArea();
        log_display.setEnabled(false);//건들지 못 하게
        center.add(time_display,BorderLayout.NORTH);
        center.add(log_display,BorderLayout.CENTER);





        add(b_start, BorderLayout.WEST);
        add(b_stop,BorderLayout.EAST);
        add(center,BorderLayout.CENTER);
        setVisible(true);

        //시간 업데이트
        new Thread(){
            @Override
            public void run() {
                super.run();
                while(true){
                    time_display.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 E h시 m분 s초")));
                    repaint();
                    try {sleep(1000);}
                    catch (InterruptedException ignored) {}
                }
            }
        }.start();
    }

    public void StopServer() {
        try {
            temp=null;//ss가 닫혔으니 더이상 그만 받아.
            Thread.sleep(500);
            if(SS!=null) SS.close();
            SS = null;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    public JButton ButtonInit(String Insert){
        JButton temp = new JButton(Insert);
        temp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(Insert.equals("서버켜기"))StartServer();
                else StopServer();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
                if(Insert.equals("서버켜기"))StartServer();
                else StopServer();
            }
        });
        return temp;
    }
    public void StartServer() {
        try{
            if(SS!=null){throw new IOException();}
            SS=new ServerSocket();
            String IP = "172.20.10.12";
            SS.bind(new InetSocketAddress(IP,this.Port));
            JOptionPane.showMessageDialog(this, "서버가 시작되었습니다.");
            temp = new Thread(){
                @Override
                public void run() {
                    super.run();
                    while (true) {
                        try {
                            Socket sc = SS.accept();
                            log_display.append(sc.getPort() + "가 새로 접속하였습니다.\n");
                            new Client(sc).start();//접속자 받기
                        } catch (IOException ignored) {break;}
                    }
                    JOptionPane.showMessageDialog(GameServer.this, "서버가 닫혔습니다.", "", JOptionPane.ERROR_MESSAGE);
                }
            };
            temp.start();

        }catch(IOException ignored){}
    }
    class Client extends Thread {
        ObjectOutputStream out;
        ObjectInputStream in;
        ObjectMsg msg;
        ObjectMsg outMsg;
        Socket socket;

        public Client(Socket sc) {
            socket = sc;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ignored) {
            }
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    msg = (ObjectMsg) in.readObject();
                    if (msg == null) continue;
                    System.out.println(msg);
                    switch (msg.getMsgMode()) {
                        case ObjectMsg.LOGIN_MODE -> {
                            User temp = (User) msg;
                            outMsg = new MsgMode(UserData.Login(temp));
                            out.writeObject(outMsg);
                        }
                        case ObjectMsg.REGISTER_MODE -> {
                            User temp = (User) msg;
                            outMsg = new MsgMode(UserData.Register(temp));
                            out.writeObject(outMsg);
                        }
                        case ObjectMsg.ROOM_MAKE_MODE -> {
                            User Tempuser = ((User) msg);
                            Room TempRoom = (Room)Tempuser.getObj();
                            outMsg = new MsgMode(RoomData.makeRoom(Tempuser, TempRoom));
                            out.writeObject(outMsg);//방 만들기 성공
                        }
                        case ObjectMsg.ROOM_VIEW -> {
                            Collection<Room> outData = RoomData.getIdRoom().values();
                            outMsg = new StringMsg(new MsgMode(ObjectMsg.MSG_MODE), outData.size() + "");
                            out.writeObject(outMsg);//방의 갯수 먼저 보내주기

                            for (Room temp : outData) {
                                temp.setMsgMode(ObjectMsg.ROOM_VIEW);
                                outMsg = temp;
                                out.writeObject(outMsg);
                            }
                        }
                        case ObjectMsg.ROOM_MODE ->{//해당하는 클라이언트 말고 다른 모든 클라이언트한테 지금 그림을 다시 그리라고 해줘야됨.
                            User Tempuser = ((User) msg);
                            Room TempRoom = (Room)Tempuser.getObj();
                            outMsg = new MsgMode(RoomData.enterRoom(Tempuser, TempRoom));
                            out.writeObject(outMsg);//방 만들기 성공
                        }
                    }
                } catch (IOException | ClassNotFoundException err) {
                    System.err.println(err);
                    break;
                }
            }
        }
    }
}
