import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


public class Client_RoomView extends JFrame{
	private JPanel contentPanel, inputPanel;
	private JScrollPane scroll1, scroll2;
	private JTextArea chatArea;
	private JList<User_Info> userList;
	private JTextField inputMsgF;
	private JButton submitBtn, exitBtn;
	private DefaultListModel<User_Info> data;
	private JPopupMenu popup;
	private JMenuItem whisperMenu, kickOutMenu;
	private User_Info me, whisperTo;
	private String nick;
	
	public Client_RoomView(int roonNum, Vector<User_Info> users, String roomName, String nick){
		this.nick = nick;
		setTitle("방번호: " + roonNum + " 방제목: " + roomName + "  사용자: " + this.nick);
		setSize(500, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		Container contentPane = this.getContentPane();
		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		
		inputMsgF = new JTextField(30);
		submitBtn = new JButton("Submit");
		exitBtn = new JButton("Exit");
		
		contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
	
		chatArea = new JTextArea(15, 30);
		chatArea.setText("Welcome\n");
		chatArea.setLineWrap(true);
		chatArea.setEditable(false);
		scroll1 = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
				, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		userList = new JList<User_Info>();
		scroll2 = new JScrollPane(userList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		data = new DefaultListModel<User_Info>();
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
		if(!(users.isEmpty())){
			for(User_Info temp: users)
				data.addElement(temp);
		}
		userList.setModel(data);
		
		for(int i = 0; i<data.size(); i++){
			User_Info tmp = data.get(i);
			if(tmp.getJustNick().equals(this.nick))
				me = tmp;
		}
		
		popup = new JPopupMenu();
		whisperMenu = new JMenuItem("귓속말");
		kickOutMenu = new JMenuItem("강퇴");
		
		popup.add(whisperMenu);
		popup.add(kickOutMenu);
		kickOutMenu.setEnabled(false);
		if(me.amIOwnerOfRoom()){
			kickOutMenu.setEnabled(true);
		}
		
		userList.setComponentPopupMenu(popup);
		//userList.addMouseListener(new myMouseListener());
		
		contentPanel.add(scroll1);
		contentPanel.add(scroll2);
		
		inputPanel.add(inputMsgF);
		inputPanel.add(submitBtn);
		inputPanel.add(exitBtn);
		
		contentPane.add(contentPanel);
		contentPane.add(inputPanel);
		inputMsgF.grabFocus();
		setVisible(true);
	}
	
	public JList<User_Info> getUserList(){
		return userList;
	}
	
	public JMenuItem getWhisperMenu(){
		return whisperMenu;
	}
	
	public JMenuItem getKickOutMenu(){
		return kickOutMenu;
	}
	
	public JTextField getInputMsgF(){
		return inputMsgF;
	}
	
	public JButton getSubmitBtn(){
		return submitBtn;
	}
	
	public JButton getExitBtn(){
		return exitBtn;
	}
	
	public JTextArea getChatArea(){
		return chatArea;
	}
	
	public void addUser(User_Info user){
		data.addElement(user);
	}
	
	public void removeUser(User_Info user){
		for(int i=0; i<data.size(); i++){
			if(user.getJustNick().equals(data.get(i).getJustNick()))
				data.remove(i);
		}
	}
	
	public User_Info getWhisperTo(){
		return whisperTo;
	}
	
	public void setData(Vector<User_Info> users){
		data.clear();
		for(User_Info user : users)
			data.addElement(user);
	}
	
	public class myMouseListener implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			if(e.isPopupTrigger())
				popup.show(e.getComponent(), e.getX(), e.getY());
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			if(e.isPopupTrigger())
				popup.show(e.getComponent(), e.getX(), e.getY());
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
