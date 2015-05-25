import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JTextArea;

public class User_Info extends Thread implements Serializable {
	private static final long serialVersionUID = 1L;
	private transient InputStream is;
	private transient OutputStream os;
	private transient DataInputStream dis;
	private transient DataOutputStream dos;
	private transient ObjectOutputStream oos;
	private transient ObjectInputStream ois;
	private transient BufferedWriter bw;
	private transient BufferedReader br;
	private transient Socket cSocket;
	private String nick;
	private String msg;
	private String roomName;
	private int maxNum, roomNum, userState, type;
	private transient ChatRoom room, curRoom;
	private transient Vector<User_Info> users;
	private boolean isOwnerOfRoom = false;
	public final int IN_ROOM_LIST = 0x8374817;
	public final int IN_CHAT_ROOM = 0x4938278;
	public final int MAKING_ROOM = 0x9437266;
	public final int MAKE_ROOM = 0x9984949;
	public final int INTO_ROOM = 0x9484332;
	public final int CONTINUE = 0x9738273;
	public final int BREAKINTO = 0x9384723;

	public User_Info(Socket cSocket) throws SocketException {
		this.cSocket = cSocket;
		this.cSocket.setKeepAlive(true);
		setStream();

		// 클라이언트로부터 닉네임 받아옴
		try{
			while (true) {
				this.nick = dis.readUTF();
				if (checkNick(nick)) {
					break;
				} else
					continue;
			}
		}catch(Exception e){
			Server.totalUsers.remove(this);
			disconnect();
		}
	}

	public boolean checkNick(String nick) throws IOException {

		for (User_Info tmpUser : Server.totalUsers) {
			if (nick.equals(tmpUser.getJustNick())) {
				dos.writeBoolean(false);
				dos.flush();
				return false;
			}
		}
		dos.writeBoolean(true);
		dos.flush();
		return true;
	}

	public String toString() {
		return this.getNick();
	}

	public void sendMsg(String msg) {
		try {
			dos.writeUTF(msg);
			dos.flush();
		} catch (IOException e) {
			Server.totalUsers.remove(this);
			disconnect();
		}
	}

	public void sendObject(Object obj) {
		try {
			oos.reset();
			oos.writeObject(obj);
			oos.flush();
			// oos.reset();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Server.totalUsers.remove(this);
			disconnect();
		}
	}

	public void sendInt(int i) {
		try {
			dos.writeInt(i);
			dos.flush();
		} catch (IOException e) {
			Server.totalUsers.remove(this);
			disconnect();
		}
	}

	public void broadCast(String msg) {
		for (int i = 0; i < users.size(); i++) {
			User_Info tmpUser = (User_Info) users.elementAt(i);
			if ((tmpUser.getNick()).equals(this.getNick()))
				this.sendMsg("나: " + msg);
			else
				tmpUser.sendMsg(nick + ": " + msg);
		}
	}

	public void interruptBroadCast(String msg) {
		for (int i = 0; i < users.size(); i++) {
			User_Info tmpUser = (User_Info) users.elementAt(i);
			tmpUser.sendMsg(msg);
		}
	}

	public void setStream() {
		try {
			is = cSocket.getInputStream();
			os = cSocket.getOutputStream();
			br = new BufferedReader(new InputStreamReader(is));
			bw = new BufferedWriter(new OutputStreamWriter(os));
			dis = new DataInputStream(is);
			dos = new DataOutputStream(os);
			dos.flush();
			oos = new ObjectOutputStream(dos);
			oos.flush();
			ois = new ObjectInputStream(dis);
			
		} catch (IOException e) {
			Server.addText("스트림 연결 실패\n");
			Server.totalUsers.remove(this);
			disconnect();
		}
	}

	public void run() {
		sendRoom();
		beforeRun();

		while (true) {
			try {
				msg = dis.readUTF();
				if (msg.contains("/귓속말/")) {
					String[] tmpArray;
					tmpArray = msg.split("/");
					
					if(tmpArray.length == 4){
						int cnt = 0;
						for (User_Info tmp : users) {
							if (tmpArray[2].equals(tmp.getJustNick())) {
								tmp.sendMsg("귓속말(" + nick + "->" + "나): "
										+ tmpArray[3]);
								this.sendMsg("귓속말(" + "나" + "->" + tmpArray[2]
										+ "): " + tmpArray[3]);
								cnt++;
							}
						}
						if (cnt == 0) {
							this.sendMsg("그런 사용자가 존재하지 않습니다.");
						}
					}
				} else if (msg.equals("INTERRUPT_KICK_OUT")){
					String kickedUser = dis.readUTF();
					for(Iterator<User_Info> iterator = users.iterator(); iterator.hasNext();){
						User_Info tmp = iterator.next();
						if(tmp.getJustNick().equals(kickedUser))
							tmp.sendMsg(msg);
					}
				} else if (msg.equals("INTERRUPT_SOMEONE_EXIT")) {
					boolean isKickedOut = dis.readBoolean();
					users.remove(this);
					
					if(users.size() == 0){
						
						Server.rooms.remove(curRoom);
						toggleIsOwner();
						for(User_Info user : Server.totalUsers){
							if((user.getUserState() == IN_ROOM_LIST)){
								user.sendBoolean(true);
								user.sendMsg("INTERRUPT_ROOM_IS_REMOVED");
								user.sendObject(Server.rooms);
							}else if(user.getUserState() == IN_CHAT_ROOM){
								user.sendMsg("INTERRUPT_ROOMS_IS_UPDATED");
								user.sendObject(Server.rooms);
							}
						}
					}else{
						
						if(isOwnerOfRoom){
							toggleIsOwner();
							users.get(0).toggleIsOwner();
						}
						
						for (User_Info tmp : users) {
							tmp.sendMsg(msg);
							tmp.sendMsg(this.nick + "님이 퇴장하셨습니다.");
							tmp.sendObject(users);
						}
						if(isKickedOut){
							for(User_Info tmp : Server.totalUsers){
								if(tmp.getUserState() == IN_ROOM_LIST){
									tmp.sendBoolean(true);
									tmp.sendMsg("INTERRUPT_ROOM_IS_UPDATED");
									tmp.sendInt(curRoom.getRoomNum());
									tmp.sendObject(curRoom);
								}else if(tmp.getUserState() == IN_CHAT_ROOM && !(tmp.getJustNick().equals(this.getJustNick()))){
									tmp.sendMsg("INTERRUPT_ROOMS_IS_UPDATED");
									tmp.sendObject(Server.rooms);
								}else
									tmp.sendObject(Server.rooms);
							}
						}else{
							for(User_Info tmp : Server.totalUsers){
								if(tmp.getUserState() == IN_ROOM_LIST){
									tmp.sendBoolean(true);
									tmp.sendMsg("INTERRUPT_ROOM_IS_UPDATED");
									tmp.sendInt(curRoom.getRoomNum());
									tmp.sendObject(curRoom);
								}else if(tmp.getUserState() == IN_CHAT_ROOM){
									tmp.sendMsg("INTERRUPT_ROOMS_IS_UPDATED");
									tmp.sendObject(Server.rooms);
								}
							}
						}
					}
					
					beforeRun();
				} else {
					broadCast(msg);
				}
			} catch (Exception eee) {
				Server.totalUsers.remove(this);
				users.remove(this);
				if(users.size() == 0)
					Server.rooms.remove(curRoom);
				else{
					if(isOwnerOfRoom){
						toggleIsOwner();
						users.get(0).toggleIsOwner();
					}
					
					for (User_Info tmp : users) {
						tmp.sendMsg("INTERRUPT_SOMEONE_EXIT");
						tmp.sendMsg(this.nick + "님이 퇴장하셨습니다.");
						tmp.sendObject(users);
					}
					
					for(User_Info tmp : Server.totalUsers){
						if(tmp.getUserState() == IN_ROOM_LIST){
							tmp.sendBoolean(true);
							tmp.sendMsg("INTERRUPT_ROOM_IS_UPDATED");
							tmp.sendInt(curRoom.getRoomNum());
							tmp.sendObject(curRoom);
						}else if(tmp.getUserState() == IN_CHAT_ROOM){
							tmp.sendMsg("INTERRUPT_ROOMS_IS_UPDATED");
							tmp.sendObject(Server.rooms);
						}
					}
				}
				disconnect();
				break;
			}
		}
	}
	
	public void sendRoom(){
		sendObject(Server.rooms);
	}

	public void beforeRun() {
		try {
			setUserState(IN_ROOM_LIST);
			String type = dis.readUTF();
			if (type.equals("BREAKINTO")) {
				sendBoolean(false);
				String flag = dis.readUTF();
				int num = 0;
				if (flag.equals("MAKE_ROOM")) {
					makeRoom();
				} else if (flag.equals("INTO_ROOM")) {
					num = dis.readInt();
					intoRoom(num);
				}
				
				setUserState(IN_CHAT_ROOM);
			}
		} catch (IOException e) {
			Server.totalUsers.remove(this);
			disconnect();
		}
	}

	public void disconnect() {
		try {
			ois.close();
			oos.close();
			bw.close();
			br.close();
			dos.close();
			dis.close();
			is.close();
			os.close();
			cSocket.close();
			if (users != null)
				Server.addText(this.getJustNick() + " 사용자 연결 해제\n");
			users.remove(this);
		} catch (Exception e) {
			
		}
	}

	public void makeRoom() {
		try {
			setUserState(IN_CHAT_ROOM);
			roomName = dis.readUTF();
			maxNum = dis.readInt();
			room = new ChatRoom(roomName, maxNum, this);
			curRoom = room;
			roomNum = room.getRoomNum();
			// 새로만든 방을 방목록에 추가
			Server.rooms.add(room);
			// 유저리스트를 room 클래스로부터 받아옴
			users = room.getUsers();
			isOwnerOfRoom = true;
			sendObject(room);
			Server.addText(roomName + " 방만들기 성공\n");

			for (User_Info tmpUser : Server.totalUsers) {
				if (tmpUser.getUserState() == IN_ROOM_LIST) {
					tmpUser.sendBoolean(true);
					tmpUser.sendMsg("INTERRUPT_SOMEONE_MAKE_ROOM");
					tmpUser.sendObject(room);
				}else if((tmpUser.getUserState() == IN_CHAT_ROOM)/* && !(tmpUser.getJustNick().equals(this.getJustNick()))*/){
					tmpUser.sendMsg("INTERRUPT_SOMEONE_MAKE_ROOM");
					tmpUser.sendObject(Server.rooms);
				}
			}
		} catch (IOException e) {
			Server.totalUsers.remove(this);
			disconnect();
		}
	}

	public int getUserState() {
		return userState;
	}

	public void setUserState(int state) {
		userState = state;
	}

	public void intoRoom(int num) throws IOException {
		for(ChatRoom tmp : Server.rooms){
			if(num == tmp.getRoomNum())
				room = tmp;
		}
		curRoom = room;
		users = room.getUsers();
		if (users.add(this)) {
			room.setUser(users);
			sendObject(room);

			setUserState(IN_CHAT_ROOM);

			for (User_Info tempUser : users) {
				if (!(tempUser.equals(this))) {
					tempUser.sendMsg("INTERRUPT_SOMEONE_COME_IN");
					tempUser.sendMsg(this.nick + "님이 입장하셨습니다.");
					tempUser.sendObject(this);
				}
			}
			for (User_Info tmpUser : Server.totalUsers) {
				if (tmpUser.getUserState() == IN_ROOM_LIST) {
					tmpUser.sendBoolean(true);
					tmpUser.sendMsg("INTERRUPT_ROOM_IS_UPDATED");
					tmpUser.sendInt(num);
					tmpUser.sendObject(room);
				}else if((tmpUser.getUserState() == IN_CHAT_ROOM) && !(tmpUser.getJustNick().equals(nick))){
					tmpUser.sendMsg("INTERRUPT_ROOMS_IS_UPDATED");
					tmpUser.sendObject(Server.rooms);
				}
			}
		}
	}

	public String getNick() {
		if (isOwnerOfRoom)
			return this.nick + " *방장*";
		else
			return this.nick;
	}

	public String getJustNick() {
		return this.nick;
	}

	public int getRoomNum() {
		return roomNum;
	}

	public boolean amIOwnerOfRoom() {
		if (isOwnerOfRoom == true)
			return true;
		else
			return false;
	}
	
	public void sendBoolean(boolean a){
		try {
			dos.writeBoolean(a);
			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Server.totalUsers.remove(this);
			disconnect();
		}
	}
	
	public void toggleIsOwner(){
		if(isOwnerOfRoom)
			isOwnerOfRoom = false;
		else
			isOwnerOfRoom = true;
	}
}
