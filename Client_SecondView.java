import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


public class Client_SecondView extends JFrame{
	private static final long serialVersionUID = 1L;
	private JLabel instChatList;
	private JPanel btnPanel;
	private JButton mkRoomBtn, intoRoomBtn;
	private JScrollPane scroller;
	private JList<ChatRoom> chatList;
	private DefaultListModel<ChatRoom> data;
	

	public Client_SecondView(Vector<ChatRoom> rooms, String nick){
		setTitle("Client Example #5 /" + nick);
		setSize(300, 550);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		setResizable(false);
		
		Container container = getContentPane();
		
		instChatList = new JLabel("채팅방 목록");
		chatList = new JList<ChatRoom>();
		data = new DefaultListModel<ChatRoom>();
		data.addListDataListener(new ListDataListener() {
			
			@Override
			public void intervalRemoved(ListDataEvent e) {
				// TODO Auto-generated method stub
				Graphics g = getGraphics();
				paint(g);
			}
			
			@Override
			public void intervalAdded(ListDataEvent e) {
				// TODO Auto-generated method stub
				Graphics g = getGraphics();
				paint(g);
			}
			
			@Override
			public void contentsChanged(ListDataEvent e) {
				// TODO Auto-generated method stub
				Graphics g = getGraphics();
				paint(g);
			}
		});
		
		if(!(rooms.isEmpty())){
			for(ChatRoom temp: rooms)
				data.addElement(temp);
			
		}
		chatList.setModel(data);
		chatList.invalidate();
		
		
		btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		mkRoomBtn = new JButton("방만들기");
		intoRoomBtn = new JButton("입장");
		
		scroller = new JScrollPane(chatList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		btnPanel.add(mkRoomBtn);
		btnPanel.add(intoRoomBtn);
		
		container.add(instChatList);
		container.add(scroller);
		container.add(btnPanel);
		
		setVisible(true);
	}
	
	public JButton getMkRoomBtn(){
		return mkRoomBtn;
	}
	
	public JButton getIntoRoomBtn(){
		return intoRoomBtn;
	}
	
	public JList<ChatRoom> getChatList(){
		return chatList;
	}
	
	public void setNewRoomList(Vector<ChatRoom> rooms){
		chatList.setListData(rooms);
		chatList.invalidate();
	}
	
	public void reFresh(Vector<ChatRoom> rooms) {
		data.clear();
		if(!(rooms.isEmpty())){
			for(ChatRoom temp: rooms)
				data.addElement(temp);
			if(!(data.isEmpty())){
				chatList.setModel(data);
				Graphics g = getGraphics();
				paintAll(g);
			}
		}
	}
	
	public void addRoom(ChatRoom room){
		data.addElement(room);
	}
	
	public void updateRoom(ChatRoom room, int num){
		for(int i=0; i<data.size(); i++){
			if(data.get(i).getRoomNum() == num)
				data.setElementAt(room, i);
		}
	}
	
	public DefaultListModel<ChatRoom> getRoomListData(){
		return data;
	}
	
	public void setData(Vector<ChatRoom> rooms){
		data.clear();
		for(ChatRoom tmp : rooms){
			data.addElement(tmp);
		}
	}
}

