package com.Main;

import com.CommunicateObject.*;
import com.Manage.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

* */
public class GameServer extends JFrame {
    private ServerSocket SS;//ServerSocket;
    private final int Port;
//    private final Vector<ClientManage> clients;
    private Thread temp;
    private final UserManage UserData;
    private final RoomManage RoomData;
    private final ConcurrentHashMap<User,Vector<Client>>UserClient;//각 유저에 맞는 클라이언트 놔두기
    private TextArea log_display;
    GameServer(int port) {//현재 프레임에 서버 시작 버튼 만들기
//        clients = new Vector<>();
        UserData = new UserManage();
        RoomData = new RoomManage();
        UserClient= new ConcurrentHashMap<>();
        setSize(720,480);
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
        JScrollPane log_scroll = new JScrollPane(log_display,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        center.add(time_display,BorderLayout.NORTH);
        center.add(log_scroll,BorderLayout.CENTER);


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
            if(SS!=null) SS.close();
            SS = null;
        } catch (IOException ignored) {}
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
            SS=new ServerSocket(this.Port);
            JOptionPane.showMessageDialog(this, "서버가 시작되었습니다.");
            temp = new Thread(){
                @Override
                public void run() {
                    super.run();
                    while (true) {
                        try {
                            Socket sc = SS.accept();
                            log_display.append(sc.getPort() + "가 새로 접속하였습니다.\n");
                            Client newClient = new Client(sc);//접속자 받기
                            newClient.start();//쓰레드 돌리기
                        } catch (IOException ignored) {break;}
                    }
                    JOptionPane.showMessageDialog(GameServer.this, "서버가 닫혔습니다.", "", JOptionPane.ERROR_MESSAGE);
                }
            };
            temp.start();
        }catch(IOException ignored){}
    }
    public void BroadCastRepaint() throws IOException {
        ObjectMsg msg = new MsgMode(ObjectMsg.REPAINT_MODE);
        for(Vector<Client>temp : UserClient.values()){//wait룸에 있는 사람들에게 보내줘야 된다.
            if(temp.size()==2){
                Client rC =temp.get(1);//이 클라한테 다시 그리라고 해야됨
                rC.out.writeObject(msg);
            }
        }
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
            } catch (IOException ignored) {}
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    msg = (ObjectMsg) in.readObject();
                    if (msg == null) continue;
                    log_display.append(socket.getPort() + "가 " + MsgMode.ToString(msg.getMsgMode())+"\n");
                    switch (msg.getMsgMode()) {
                        case ObjectMsg.LOGIN_MODE -> {
                            User temp = (User) msg;
                            outMsg = new MsgMode(UserData.Login(temp));
                            //해당 유저의 client를 등록
                            if(UserClient.containsKey(temp)){//이미 접속한 적 있었으면 해당하는 0번째에 다시 덮어쓰기
                                UserClient.get(temp).set(0,this);//현재 접속한 것으로 덮어쓰기
                            }else{
                                UserClient.put(temp,new Vector<>());
                                UserClient.get(temp).add(this);//현재 것 넣어주기
                            }
                            out.writeObject(outMsg);
                        }
                        case ObjectMsg.REGISTER_MODE -> {
                            User temp = (User) msg;
                            outMsg = new MsgMode(UserData.Register(temp));
                            out.writeObject(outMsg);
                        }
                        case ObjectMsg.ROOM_MAKE_MODE -> {//다른 모든 유저들에게 보내주기
                            outMsg = new MsgMode(RoomData.makeRoom((User) msg, (Room) msg.obj));
                            out.writeObject(outMsg);//방 만들기 성공
                            BroadCastRepaint();
                        }
                        case ObjectMsg.ROOM_VIEW -> {//WaitRoom
                            Collection<Room> outData = RoomData.getIdRoom().values();
                            outMsg = new StringMsg(new MsgMode(ObjectMsg.MSG_MODE), outData.size() + "");
                            out.writeObject(outMsg);//방의 갯수 먼저 보내주기
                            for (Room temp : outData) {
                                temp.setMsgMode(ObjectMsg.ROOM_VIEW);
                                outMsg = temp;
                                out.writeObject(outMsg);
                            }
                        }
                        case ObjectMsg.ROOM_INFO -> {//GameRoom
                            Room TempRoom = (Room)msg;
                            outMsg = RoomData.getRoom(TempRoom);
                            out.writeObject(outMsg);
                        }
                        case ObjectMsg.ROOM_MODE ->{//GameRoom 들어가기 직전
                            User Tempuser = ((User) msg);
                            Room TempRoom = (Room)Tempuser.getObj();
                            outMsg = new MsgMode(RoomData.enterRoom(Tempuser, TempRoom));
                            out.writeObject(outMsg);//방 만들기 성공
                            BroadCastRepaint();
                        }
                        case ObjectMsg.GAME_START_MODE -> {//GameStartRoom
                            //1. 해당하는 방에 접속한 모든 유저들에게 모두 게임 시작하라고 하기
                            //해당하는 방장이 게임을 시작하겠다고 했음

                        }
                    }
                } catch (IOException | ClassNotFoundException error) {throw new RuntimeException(error);}
            }
        }
    }
}
