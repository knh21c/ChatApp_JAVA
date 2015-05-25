import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

public class ChatRoom implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Vector<User_Info> users = new Vector<User_Info>();
	public static int num = 0;
	private int maxNum;
	private int curNum;
	private String name;
	private int roomNum;
	
	public ChatRoom(String name, int maxNum, User_Info user){
		users.add(user);
		this.maxNum = maxNum;
		this.name = name;
		roomNum = num;
		num++;
	}
	
	public String toString(){
		curNum = users.size();
		return "规锅龋: " + roomNum + "  规力格: " + name + "  (" + curNum + "/" + maxNum + ")";
	}
	
	public Vector<User_Info> getUsers(){
		return users;
	}
	
	public int getRoomNum(){
		return roomNum;
	}
	
	public void addUser(User_Info user){
		users.add(user);
	}
	
	public void setUser(Vector<User_Info> users){
		this.users = users;
	}
	
	public String getRoomName(){
		return name;
	}
	
	public int getCurNum(){
		return users.size();
	}
	
	public void setCurNum(int num){
		
	}
	
	public int getMaxNum(){
		return maxNum;
	}
	
	public void removeUser(User_Info user){
		for(Iterator<User_Info> iterator = users.iterator(); iterator.hasNext();){
			User_Info tmp = iterator.next();
			if(tmp.getJustNick().equals(user.getJustNick()))
				iterator.remove();
		}
	}
}
