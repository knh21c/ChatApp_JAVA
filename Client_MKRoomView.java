import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class Client_MKRoomView extends JFrame{
	JLabel nameLabel, maxLabel;
	JTextField name, maxNum;
	JButton mkBtn, cancelBtn;
	JPanel namePanel, numPanel, btnPanel;
	
	/*public static void main(String[] args){
		new Ex_Client_5_MkRoomView();
	}*/
	
	public Client_MKRoomView(){
		setSize(300, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		setResizable(false);
		
		JPanel container = (JPanel)getContentPane();
		
		nameLabel = new JLabel("规力格: ");
		maxLabel = new JLabel("弥措 牢盔: ");
		
		name = new JTextField(15);
		maxNum = new JTextField(15);
		
		mkBtn = new JButton("积己");
		cancelBtn = new JButton("秒家");
		
		namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		numPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		namePanel.add(nameLabel);
		namePanel.add(name);
		
		numPanel.add(maxLabel);
		numPanel.add(maxNum);
		
		btnPanel.add(mkBtn);
		btnPanel.add(cancelBtn);
		
		container.add(namePanel);
		container.add(numPanel);
		container.add(btnPanel);
		
		setVisible(true);
	}
	
	public String getRoomName(){
		return name.getText();
	}
	
	public String getMaxNum(){
		return maxNum.getText();
	}
	
	public JButton getMkBtn(){
		return mkBtn;
	}
	
	public JButton getCancelBtn(){
		return cancelBtn;
	}
	
	public JTextField getNameTextF(){
		return name;
	}
	
	public JTextField getMaxNumTextF(){
		return maxNum;
	}
}
