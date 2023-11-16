import java.util.Vector;

public class Room {
    private int roomId;//방 번호
    private int adminId;//방장 아이디
    private final Vector<User>users;//방 안에 있는 사람들
    Room(int roomId,int adminId){
        users=new Vector<>();
        this.adminId = adminId;
        this.roomId = roomId;
    }

}
