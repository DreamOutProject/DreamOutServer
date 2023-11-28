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
    private Thread temp;
    private final UserManage UserData;
    private final RoomManage RoomData;
    private final ConcurrentHashMap<Integer,Vector<Client>>UserClient;//각 유저에 맞는 클라이언트 놔두기
    private final TextArea log_display;
    GameServer(int port) {//현재 프레임에 서버 시작 버튼 만들기
        UserData = new UserManage();
        RoomData = new RoomManage();
        UserClient= new ConcurrentHashMap<>();
        UserData.addUser(new User(null,1,1));
        UserData.addUser(new User(null,2,2));
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

    class Client extends Thread { //각 클라이언트
        ObjectOutputStream out;
        ObjectInputStream in;
        ObjectMsg msg;
        ObjectMsg outMsg = new MsgMode(ObjectMsg.MSG_MODE);
        Socket socket;
        User my=null;//각 클라이언트에 해당하는 아이디를 저장할 수 있음
        Room myroom=null;//

        public void BroadCastRepaint(int ObjectMsgMode) throws IOException {
            ObjectMsg msg = new MsgMode(ObjectMsgMode);
            Enumeration<Integer> iter = UserClient.keys();
            while(iter.hasMoreElements()){
                Integer id = iter.nextElement();
                if(my!=null && my.getId() != id){
                    Client repaintT = UserClient.get(id).get(1);
                    repaintT.out.writeObject(msg);
                }
            }
        }

        public Client(Socket sc) {
            socket = sc;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ignored) {}
        }
        public void debug(ObjectMsg m){
            System.out.println(MsgMode.ToString(m.getMsgMode()));
        }
        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    msg = (ObjectMsg) in.readObject();
                    if (msg == null) {
                        System.out.println("소켓이 종료합니다.");
                        break;
                    }
                    int id=-1;
                    if(my!=null)id=my.getId();
                    else id=socket.getPort();
                    log_display.append(id+  "가 " + MsgMode.ToString(msg.getMsgMode())+"\n");
                    switch (msg.getMsgMode()) {
                        case ObjectMsg.LOGIN_MODE -> {
                            User temp = (User) msg;
                            outMsg = new MsgMode(UserData.Login(temp));
                            //해당 유저의 client를 등록
                            if(UserClient.containsKey(temp.getId())){//이미 접속한 적 있었으면 해당하는 0번째에 다시 덮어쓰기
                                UserClient.get(temp.getId()).set(0,this);//현재 접속한 것으로 덮어쓰기
                            }else{
                                UserClient.put(temp.getId(),new Vector<>());
                                UserClient.get(temp.getId()).add(this);//현재 것 넣어주기
                            }
                            this.my = temp;
                            out.writeObject(outMsg);
                            out.flush();
                        }
                        case ObjectMsg.REGISTER_MODE -> {
                            User temp = (User) msg;
                            outMsg = new MsgMode(UserData.Register(temp));
                            out.writeObject(outMsg);
                            out.flush();
                        }
                        case ObjectMsg.ROOM_MAKE_MODE -> {//해당 유저가 방을 만든다.
                            IntMsg roomsize = (IntMsg) msg; //방 사이즈만 알면 됨.
                            outMsg = RoomData.makeRoom(this.my,roomsize.getNumber());
                            out.writeObject(outMsg);//방 만들기 성공
                            out.flush();
                            BroadCastRepaint(ObjectMsg.REPAINT_MODE);
                        }
                        case ObjectMsg.ROOM_VIEW -> {//대기방일 때 방 정보 주세요.
                            Collection<Room> outData = RoomData.getIdRoom().values();
                            outMsg = new IntMsg(new MsgMode(ObjectMsg.MSG_MODE), outData.size());
                            out.writeObject(outMsg);//방의 갯수 먼저 보내주기
                            out.flush();
                            for (Room temp : outData) {
                                out.writeObject(temp);
                                out.flush();
                            }
                        }
                        case ObjectMsg.ROOM_INFO -> {//게임 방 들어갔을 떄 해당 방의 정보 주세요
                            IntMsg roomid = (IntMsg)msg;
                            Room rooms = RoomData.getRoom(roomid.getNumber());
                            System.out.println(my.getId()+"에게" +rooms.getUsers().size()+"만큼의 사람을 보냅니다.");
                            rooms.setMsgMode(ObjectMsg.REGISTER_MODE);
                            out.writeObject(rooms);
                            out.flush();
                        }
                        case ObjectMsg.ROOM_MODE ->{//대기실에 떠있는 방 중에서 클릭하여 방에 들어갈 떄
                            IntMsg roomid = (IntMsg)msg;
                            outMsg = new MsgMode(RoomData.enterRoom(this.my, roomid.getNumber()));
                            out.writeObject(outMsg);//방 들어가기 성공
                            out.flush();
                            BroadCastRepaint(ObjectMsg.REPAINT_MODE);
                        }
                        case ObjectMsg.GAME_START_MODE -> {//GameStartRoom
                            //1. 해당하는 방에 접속한 모든 유저들에게 모두 게임 시작하라고 하기
                            //해당하는 방장이 게임을 시작하겠다고 했음
                            Room TempRoom = (Room)msg;
                            BroadCastRepaint(ObjectMsg.GAME_START_MODE);
                        }
                        case ObjectMsg.TEMP->{//해당 user의 repaint 연결
                            User temp = (User)msg;
                            outMsg.setMsgMode(ObjectMsg.FAILED);
                            if(temp==null) System.out.println("null인뎁쇼..?");
                            else if(!UserClient.containsKey(temp.getId())) System.out.println("어? 해당 유저 없는데?");
                            else{
                                if(UserClient.get(temp.getId()).size() == 2){
                                    UserClient.get(temp.getId()).set(1,this);//현재 클라 추가하기
                                }else{
                                    UserClient.get(temp.getId()).add(this);//현재 클라 추가하기
                                }
                                this.my = new User(null,temp.getId(),0);
                                outMsg.setMsgMode(ObjectMsg.SUCESSED);
                            }
                            out.writeObject(outMsg);//성공 실패 알려주기
                            out.flush();
                        }
                        default -> {
                            System.out.println("이상한 데이터 들어왔어요");
                        }
                    }
                } catch (IOException | ClassNotFoundException error) {
                    System.out.println(this.socket.getPort()+"해당 클라이언트가 에러발생");
                    System.out.println(error);
                    break;
                }
            }
            try {
                socket.close();
                UserClient.remove(my.getId());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
