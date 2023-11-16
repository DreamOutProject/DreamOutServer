
public class User {
	private int id;
	private int pw;
	//나중에 업데이트 사항 private String nick;

	User(int id, int pw){
		this.id = id;
		this.pw = pw;
	}
	//해당하는 것은 비밀번호를 1234로 생성
	User(int id){new User(id,1234);}
	public int getId(){return this.id;}
	public int getPw(){return this.pw;}
	public boolean equals(User us) {//같다는 것 재정의
        return this.id == us.id && this.pw == us.pw;
    }
	public boolean IsPw(User u){
		return u.getPw() == this.pw;
	}
}
