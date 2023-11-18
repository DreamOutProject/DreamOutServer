import java.io.Serializable;
import java.util.Vector;

public class UserManage implements Serializable {
    private final Vector<User> data;
    UserManage(){
        data = new Vector<>();
    }
    public void addUser(User user){data.add(user);}
    public boolean isContainId(int id){//해당 아이디가 있는지 먼저 판단
        for(User us:data){
            if(us.getId() == id)return true;
        }
        return false;
    }
    private int getIdx(User u){//선행 되어야 하는것은 isContainId이다.
        for(int i=0;i<data.size();i++){
            if(data.get(i).equals(u))return i;
        }
        return -1;
    }
    public String Login(User u){//로그인 절차
        //1. 아이디 있는지 판단
        if(isContainId(u.getId()))return "해당하는 아이디가 없습니다.";//없으면 false 보내기

        //2. 해당하는 아이디의 비밀번호가 맞는지 판단
        int idx=getIdx(u);
        if(!data.get(idx).IsPw(u))return "비밀번호가 틀렸습니다.";

        //3.최종적으로 로그인 성공으로 true 보내주기;
        return "환영합니다.";
    }
    public String Register(User u){//회원가입
        //1. 아이디가 있는지 판단
        if(isContainId(u.getId()))return "이미 있는 아이디 입니다.";
        addUser(u);
        return "회원가입 성공";
    }
}
