import java.io.Serializable;
import java.util.Vector;


public class ChatRoomData implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Vector<ChatRoom> rooms;
	
	public ChatRoomData(Vector<ChatRoom> rooms){
		this.rooms = rooms;
	}
	
	public Vector<ChatRoom> getRooms(){
		return this.rooms;
	}
}
