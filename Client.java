import java.awt.RenderingHints.Key;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client {
	private JButton connectBtn;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private BufferedWriter bw;
	private BufferedReader br;
	private int port;
	private String host, nick;
	private JTextField portTextF, ipTextF, nickTextF;
	private JTextField nameTextF, maxNumTextF;
	private Socket cSocket;
	private Client_View exView;
	private String msg;
	private Client_SecondView chatListView;
	private Vector<ChatRoom> rooms = new Vector<ChatRoom>();
	private Vector<User_Info> users = new Vector<User_Info>();
	private Client_RoomView roomView;
	private ChatRoom curRoom;
	private JButton submitBtn, exitBtn;
	private JTextArea chatArea;
	private JTextField inputMsgF;
	private boolean isInRoomList = false;
	private boolean isInChatRoom = false;
	private JButton mkRoomBtn, intoRoomBtn;
	private Thread thread, chatRoomThread, kickOutThread;
	private String maxNum;
	private String roomName;
	private Client_MKRoomView mkRoomView;
	private JList<ChatRoom> chatList;
	private JList<User_Info> userList;
	private JButton mkBtn, cancelBtn;
	private JMenuItem whisperMenu, kickOutMenu;
	public final int MAKE_ROOM = 0x9984949;
	public final int INTO_ROOM = 0x9484332;
	public final int CONTINUE = 0x9738273;
	public final int BREAKINTO = 0x9384723;

	public static void main(String[] args) {
		Client ex = new Client();
		ex.init();
	}

	public void init() {
		exView = new Client_View();
		connectBtn = exView.getConnectBtn();
		portTextF = exView.getPortTextF();
		ipTextF = exView.getIPTextF();
		nickTextF = exView.getNickTextF();
		
		InitViewKeyListener initKeyListener = new InitViewKeyListener();

		portTextF.addKeyListener(initKeyListener);
		ipTextF.addKeyListener(initKeyListener);
		nickTextF.addKeyListener(initKeyListener);
		
		connectBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				port = Integer.parseInt(portTextF.getText().trim());
				host = ipTextF.getText().trim();
				nick = exView.getNick();
				try {
					if (cSocket == null) {
						cSocket = new Socket(host, port);
						cSocket.setKeepAlive(true);
						setStream();

						ipTextF.setEditable(false);
						portTextF.setEnabled(false);
					}
					if (checkNick(nick)) {
						// 채팅방목록이 저장된 객체를 받아옴
						rooms = (Vector<ChatRoom>) ois.readObject();

						exView.dispose();
						setSecondView(rooms);
					}
				} catch (Exception ee) {
					disconnect();
				}
			}
		});
	}

	public boolean checkNick(String nick) throws Exception {
		dos.writeUTF(nick);
		dos.flush();
		if (dis.readBoolean()) {
			return true;
		} else {
			JOptionPane.showMessageDialog(null, "이미 존재하는 닉네임 입니다.", "알림",
					JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
	}

	public void go() {
		submitBtn = roomView.getSubmitBtn();
		exitBtn = roomView.getExitBtn();
		chatArea = roomView.getChatArea();
		inputMsgF = roomView.getInputMsgF();
		whisperMenu = roomView.getWhisperMenu();
		kickOutMenu = roomView.getKickOutMenu();
		userList = roomView.getUserList();
		
		inputMsgF.grabFocus();

		RoomViewBtnListener roomViewBtnListener = new RoomViewBtnListener();
		InputTextFieldListener inputTextFListener = new InputTextFieldListener();
		PopUpActionListener popUpActionListener = new PopUpActionListener();

		whisperMenu.addActionListener(popUpActionListener);
		kickOutMenu.addActionListener(popUpActionListener);
		submitBtn.addActionListener(roomViewBtnListener);
		exitBtn.addActionListener(roomViewBtnListener);
		inputMsgF.addKeyListener(inputTextFListener);
		isInChatRoom = true;
		kickOutThread = new Thread(new KickOutRunnable());
		chatRoomThread = new Thread(new Runnable() {
			@Override
			public void run() {
				boolean isKickedOut = false;
				while (isInChatRoom) {
					try {
						String msg = dis.readUTF();
						if (msg.equals("INTERRUPT_SOMEONE_COME_IN")) {
							msg = dis.readUTF();
							User_Info tmpUser = (User_Info) ois.readObject();
							users.add(tmpUser);
							curRoom.addUser(tmpUser);
							roomView.addUser(tmpUser);
							chatArea.append(msg + "\n");
							chatArea.moveCaretPosition(chatArea.getText()
									.length() - 1);
						} else if (msg.equals("INTERRUPT_SOMEONE_MAKE_ROOM")
								|| msg.equals("INTERRUPT_ROOMS_IS_UPDATED")) {
							rooms = (Vector<ChatRoom>) ois.readObject();
						} else if (msg.equals("INTERRUPT_KICK_OUT")){
							sendMsg("INTERRUPT_SOMEONE_EXIT");
							dos.writeBoolean(true);
							dos.flush();
							isInChatRoom = false;
							isKickedOut = true;
							rooms = (Vector<ChatRoom>) ois.readObject();
							break;
						} else if (msg.equals("INTERRUPT_SOMEONE_EXIT")) {
							msg = dis.readUTF();
							users = (Vector<User_Info>) ois
									.readObject();
							for(User_Info tmp: users){
								if(tmp.getJustNick().equals(nick))
									if(tmp.amIOwnerOfRoom())
										kickOutMenu.setEnabled(true);
							}
							roomView.setData(users);
							chatArea.append(msg + "\n");
							chatArea.moveCaretPosition(chatArea.getText()
									.length() - 1);
						} else if (msg.equals("SKIP")) {
							System.out.println("SKIPPED");
						} else {
							chatArea.append(msg + "\n");
							chatArea.moveCaretPosition(chatArea.getText()
									.length() - 1);
						}
					} catch (Exception e) {
						disconnect();
						break;
					}
				}
				if(isKickedOut){
					kickOutThread.start();
				}
			}
		});
		chatRoomThread.start();
	}

	public class RoomViewBtnListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			try {
				if (e.getSource().equals(submitBtn)) {
					if (inputMsgF.getText().equals(""))
						inputMsgF.grabFocus();
					else {
						String msg = inputMsgF.getText().trim();
						sendMsg(msg);
						inputMsgF.setText("");
						inputMsgF.grabFocus();
					}
				} else if (e.getSource().equals(exitBtn)) {
					isInChatRoom = false;
					sendMsg("INTERRUPT_SOMEONE_EXIT");
					dos.writeBoolean(false);
					dos.flush();
					while (chatRoomThread.isAlive()) {
						
					}
					roomView.dispose();

					setSecondView(rooms);
				}
			} catch (Exception ee) {
				ee.printStackTrace();
				disconnect();
			}
		}
	}

	public class PopUpActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if ((e.getSource()).equals(whisperMenu)) {
				if (userList.getSelectedIndex() != -1) {
					User_Info user = userList.getSelectedValue();
					if (!(user.getJustNick().equals(nick))) {
						String msg = "/귓속말/" + user.getJustNick() + "/ ";

						inputMsgF.setText(msg);
						inputMsgF.grabFocus();
					}
				}
			} else if ((e.getSource()).equals(kickOutMenu)) {
				try{
					if (userList.getSelectedIndex() != -1) {
						User_Info user = userList.getSelectedValue();
						if (!(user.getJustNick().equals(nick))) {
							sendMsg("INTERRUPT_KICK_OUT");
							sendMsg(user.getJustNick());
						}
					}
				}catch(Exception ee){
					ee.printStackTrace();
				}
			}
		}
	}

	public class InputTextFieldListener implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			try {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					if (inputMsgF.getText().equals(""))
						inputMsgF.grabFocus();
					else {
						String msg = inputMsgF.getText().trim();
						sendMsg(msg);
						inputMsgF.setText("");
						inputMsgF.grabFocus();
					}
				}
			} catch (Exception ee) {
				disconnect();
			}
		}

	}

	public void disconnect() {
		try {
			ois.close();
			bw.close();
			br.close();
			dis.close();
			dos.close();
			is.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setStream() throws IOException {
		os = cSocket.getOutputStream();
		is = cSocket.getInputStream();
		bw = new BufferedWriter(new OutputStreamWriter(os));
		br = new BufferedReader(new InputStreamReader(is));
		dos = new DataOutputStream(os);
		dis = new DataInputStream(is);
		ois = new ObjectInputStream(dis);
		oos = new ObjectOutputStream(dos);
	}

	public void sendMsg(String msg) throws Exception {
		try {
			dos.writeUTF(msg);
			dos.flush();

		} catch (IOException e) {
			disconnect();
		}
	}

	public void sendInt(int i) throws Exception {
		try {
			dos.writeInt(i);
			dos.flush();

		} catch (IOException e) {
			disconnect();
		}
	}

	public void setSecondView(Vector<ChatRoom> rooms) {
		chatListView = new Client_SecondView(rooms, nick);

		mkRoomBtn = chatListView.getMkRoomBtn();
		intoRoomBtn = chatListView.getIntoRoomBtn();
		ChatListBtnListener chatListListener = new ChatListBtnListener();

		mkRoomBtn.addActionListener(chatListListener);
		intoRoomBtn.addActionListener(chatListListener);

		chatList = chatListView.getChatList();
		chatList.addMouseListener(new ChatListMouseListener());

		isInRoomList = true;

		thread = new Thread(new InRoomThread());
		thread.start();
		
	}

	public class ChatListBtnListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				JButton tmp = (JButton) e.getSource();
				if ((tmp.getText()).equals("방만들기")) {
					prepareMkRoomView();
				} else if ((tmp.getText()).equals("입장")) {
					intoRoom();
				}
			} catch (Exception ee) {
				disconnect();
			}
		}
	}

	public void prepareMkRoomView() {
		mkRoomView = new Client_MKRoomView();

		nameTextF = mkRoomView.getNameTextF();
		maxNumTextF = mkRoomView.getMaxNumTextF();
		
		MKRoomViewKeyListener mkRoomListener = new MKRoomViewKeyListener();
		nameTextF.addKeyListener(mkRoomListener);
		maxNumTextF.addKeyListener(mkRoomListener);
		
		mkBtn = mkRoomView.getMkBtn();
		cancelBtn = mkRoomView.getCancelBtn();

		mkBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					maxNum = mkRoomView.getMaxNum().trim();
					roomName = mkRoomView.getRoomName().trim();
					if (!maxNum.isEmpty() && !roomName.isEmpty()) {
						isInRoomList = false;
						dos.writeUTF("BREAKINTO");
						dos.flush();
						while (thread.isAlive()) {

						}
						dos.writeUTF("MAKE_ROOM");
						dos.flush();
						dos.writeUTF(roomName);
						dos.flush();
						dos.writeInt(Integer.parseInt(maxNum));
						dos.flush();

						curRoom = (ChatRoom) ois.readObject();
						users = curRoom.getUsers();
						rooms.add(curRoom);
						chatListView.dispose();
						mkRoomView.dispose();
						roomView = new Client_RoomView(curRoom.getRoomNum(),
								users, roomName, nick);
						isInChatRoom = true;
						go();

					} else {
						JOptionPane.showMessageDialog(null,
								"방 제목과 최대 인원을 제대로 입력하십시오.", "알림",
								JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (Exception e1) {
					disconnect();
				}
			}
		});
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mkRoomView.dispose();
			}
		});
	}

	public boolean checkToEnter(int num) throws Exception {
		ChatRoom tmpRoom = rooms.get(num);
		int curNum = tmpRoom.getCurNum();
		int maxNum = tmpRoom.getMaxNum();
		if (curNum < maxNum)
			return true;
		else
			return false;
	}

	public void intoRoom() throws Exception {
		DefaultListModel<ChatRoom> data = chatListView.getRoomListData();
		
		int num = chatList.getSelectedIndex();
		if (num != -1) {
			ChatRoom tmpRoom = data.get(num);
			int roomNum = tmpRoom.getRoomNum();
			System.out.println(roomNum);
			if (checkToEnter(num)) {
				// 서버에 플래그 전달
				isInRoomList = false;
				dos.writeUTF("BREAKINTO");
				dos.flush();
				while (thread.isAlive()) {

				}
				dos.writeUTF("INTO_ROOM");
				dos.flush();
				dos.writeInt(roomNum);
				dos.flush();

				curRoom = (ChatRoom) ois.readObject();
				users = curRoom.getUsers();
				String roomName = curRoom.getRoomName();

				chatListView.dispose();
				roomView = new Client_RoomView(curRoom.getRoomNum(),
						curRoom.getUsers(), roomName, nick);
				isInChatRoom = true;
				go();
			} else {
				JOptionPane.showMessageDialog(null, "방이 가득 찼습니다.", "알림",
						JOptionPane.INFORMATION_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(null, "목록에서 선택하세요.", "알림",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public Object readObject() throws Exception {
		Object tmp = ois.readObject();
		return tmp;
	}

	public class InRoomThread implements Runnable {
		public void run() {
			// TODO Auto-generated method stub
			while (isInRoomList) {
				try {
					boolean flag = dis.readBoolean();
					if (flag) {
						System.out.println(flag);
						String interrupt = dis.readUTF();
						if (interrupt.equals("INTERRUPT_SOMEONE_MAKE_ROOM")) {
							// System.out.println("interrupt is INTERRUPT_SOMEONE_MAKE_ROOM");
							ChatRoom tmpRoom = (ChatRoom) ois.readObject();
							rooms.add(tmpRoom);
							// System.out.println(tmpRoom);
							if (tmpRoom != null) {
								chatListView.addRoom(tmpRoom);
							}
						} else if (interrupt
								.equals("INTERRUPT_ROOM_IS_UPDATED")) {
							// System.out.println("interrupt is INTERRUPT_SOMEONE_INTO_ROOM");
							int num = dis.readInt();
							ChatRoom tmpRoom = (ChatRoom) ois.readObject();
							if (tmpRoom != null) {
								// 서버로부터 변경된 내용이 있는 room클래스르 받아와 갱신
								chatListView.updateRoom(tmpRoom, num);
								// 갱신된 room목록을 client 클래스의 맴버변수 rooms에 반영
								DefaultListModel<ChatRoom> tmpRoomData = chatListView
										.getRoomListData();
								rooms.clear();
								for (int i = 0; i < tmpRoomData.size(); i++)
									rooms.add(tmpRoomData.get(i));
							}
						} else if (interrupt
								.equals("INTERRUPT_ROOM_IS_REMOVED")) {
							rooms = (Vector<ChatRoom>) ois.readObject();
							chatListView.setData(rooms);
						} else {
							// System.out.println(interrupt);
						}
					}
				} catch (Exception e) {
					disconnect();
					break;
				}
			}
			// System.out.println("isInRoomList Thread exit");
		}
	}
	
	public class KickOutRunnable implements Runnable{
		public void run(){
			try{
				JOptionPane.showMessageDialog(roomView, "방장에 의해 강제퇴장 되었습니다.", "알림",
						JOptionPane.INFORMATION_MESSAGE);
				roomView.dispose();
				setSecondView(rooms);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public class InitViewKeyListener implements KeyListener{

		@Override
		public void keyTyped(KeyEvent e) {
			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_ENTER){
				port = Integer.parseInt(portTextF.getText().trim());
				host = ipTextF.getText().trim();
				nick = exView.getNick();
				try {
					if (cSocket == null) {
						cSocket = new Socket(host, port);
						cSocket.setKeepAlive(true);
						setStream();

						ipTextF.setEditable(false);
						portTextF.setEnabled(false);
					}
					if (checkNick(nick)) {
						// 채팅방목록이 저장된 객체를 받아옴
						rooms = (Vector<ChatRoom>) ois.readObject();

						exView.dispose();
						setSecondView(rooms);
					}
				} catch (Exception ee) {
					disconnect();
				}
			}
		}
	}
	
	public class MKRoomViewKeyListener implements KeyListener{

		@Override
		public void keyTyped(KeyEvent e) {
			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_ENTER){
				try {
					maxNum = mkRoomView.getMaxNum().trim();
					roomName = mkRoomView.getRoomName().trim();
					if (!maxNum.isEmpty() && !roomName.isEmpty()) {
						isInRoomList = false;
						dos.writeUTF("BREAKINTO");
						dos.flush();
						while (thread.isAlive()) {
	
						}
						dos.writeUTF("MAKE_ROOM");
						dos.flush();
						dos.writeUTF(roomName);
						dos.flush();
						dos.writeInt(Integer.parseInt(maxNum));
						dos.flush();
	
						curRoom = (ChatRoom) ois.readObject();
						users = curRoom.getUsers();
						rooms.add(curRoom);
						chatListView.dispose();
						mkRoomView.dispose();
						roomView = new Client_RoomView(curRoom.getRoomNum(),
								users, roomName, nick);
						isInChatRoom = true;
						go();
	
					} else {
						JOptionPane.showMessageDialog(null,
								"방 제목과 최대 인원을 제대로 입력하십시오.", "알림",
								JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (Exception e1) {
					disconnect();
				}
			}
		}
	}
	
	public class ChatListMouseListener implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2){
				try {
					intoRoom();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			
		}
	}
}
