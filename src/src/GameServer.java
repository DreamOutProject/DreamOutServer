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
    private final Vector<ClientManage> clients;
    private Thread temp;
    private final UserManage UserData;
    private final RoomManage RoomData;
    private TextArea log_display;
    GameServer(int port) {//현재 프레임에 서버 시작 버튼 만들기
        clients = new Vector<>();
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
            SS=new ServerSocket(this.Port);
            JOptionPane.showMessageDialog(this, "서버가 시작되었습니다.");
            temp = new Thread(){
                @Override
                public void run() {
                    super.run();
                    while (true) {
                        try {
                            new ClientManage(SS.accept()).start();//접속자 받기
                        } catch (IOException ignored) {break;}
                    }
                    JOptionPane.showMessageDialog(GameServer.this, "서버가 닫혔습니다.", "", JOptionPane.ERROR_MESSAGE);
                }
            };
            temp.start();

        }catch(IOException ignored){}
    }

    class ClientManage extends Thread{
        ObjectOutputStream out;
        ObjectInputStream in;
        ObjectMsg msg;
        Socket socket;
        ClientManage(Socket sc){
            socket = sc;
            clients.add(this);//현재 클래스를 계속 추가해준다
            log_display.append(sc.getPort() +"가 접속하였습니다.\n");
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ignored) {}
        }
        @Override
        public void run() {//서버에서는 처음에 어떤 동작을 해야 되는지 기술
            super.run();
            while(true){
                try {
                    msg = (ObjectMsg) in.readObject();
                    if(msg == null)break;
                    System.out.println(msg);
                    switch (msg.getMsg()) {
                        case "시간" -> out.writeObject(new ObjectMsg(getTime()));//시간 정보 보내주기
                        case "로그인" -> out.writeObject(new ObjectMsg(UserData.Login(msg.getUser())));//로그인 정보 알아내기
                        case "회원가입" -> out.writeObject(new ObjectMsg(UserData.Register(msg.getUser())));
                        case "방생성" -> out.writeObject(new ObjectMsg(RoomData.makeRoom(msg.getUser(),msg.getRoom())));
                        case "방정보" -> out.writeObject(RoomData.getRoom(msg));
                        case "방접근" ->{
                            String message = RoomData.enterRoom(msg.getUser(),RoomData.getRoom(new ObjectMsg(msg.getRoom().getRoomId())).getRoom());
                            if(message.equals("방이 꽉찼습니다."))out.writeObject(new ObjectMsg(message));
                            else out.writeObject(RoomData.getRoom(new ObjectMsg(msg.getRoom().getRoomId())));
                        }
//                        case "방" -> out.writeObject();
                    }
                } catch (IOException | ClassNotFoundException err) {
                    System.err.println(err);
                    break;
                }
            }
        }
        //시간 알려주기
        public String getTime(){return new Date().toString();}
    }


}
