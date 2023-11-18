import java.util.concurrent.ConcurrentHashMap;

public class RoomManage {
    ConcurrentHashMap<Integer,Room> IdxToRoom;
    ConcurrentHashMap<Room,Integer> RoomToIdx;
    RoomManage(){
        IdxToRoom = new ConcurrentHashMap<>();
        RoomToIdx = new ConcurrentHashMap<>();
    }
    public void addRoom(Room room){
        IdxToRoom.put(room.getRoomId(),room);
        RoomToIdx.put(room,room.getRoomId());
    }

    public String makeRoom(User u,Room room){
        room.addUser(u);
        addRoom(room);
        return "방이 생성되었습니다";
    }
    public String enterRoom(User u,Room room){
        if(!room.addUser(u))return "방이 꽉찼습니다.";
        return "방에 들어갔습니다.";
    }
    public ObjectMsg getRoomIdx(Room room){
        if(!RoomToIdx.containsKey(room))return new ObjectMsg("해당 방이 없습니다");
        return new ObjectMsg(RoomToIdx.get(room));
    }
    public ObjectMsg getRoom(ObjectMsg idx){
        if(idx.getIdx()==null)return new ObjectMsg("해당 방이 없습니다");
        if(!IdxToRoom.containsKey(idx.getIdx()))return new ObjectMsg("해당 방이 없습니다");
        return new ObjectMsg(IdxToRoom.get(idx.getIdx()));
    }
}
