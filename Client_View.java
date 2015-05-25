import java.awt.FlowLayout;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Client_View extends JFrame{
	private JLabel portLabel, ipLabel, nickLabel;
	private JTextField portTextF, ipTextF, nickTextF;
	private JButton connectBtn;
	private JPanel portPanel, ipPanel, nickPanel;
	
	public Client_View(){
		setSize(220, 200);
		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Client Example #5");
		setResizable(false);
		
		JPanel container = (JPanel) getContentPane();
		
		nickLabel = new JLabel("Nickname: ");
		portLabel = new JLabel("PORT: ");
		ipLabel = new JLabel("IP: ");
		
		nickTextF = new JTextField(10);
		portTextF = new JTextField(10);
		ipTextF = new JTextField(10);
		portTextF.setText("15001");
		ipTextF.setText("localhost");
		
		connectBtn = new JButton("¿¬°á");
		
		nickPanel = new JPanel();
		portPanel = new JPanel();
		ipPanel = new JPanel();
		
		nickPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
		portPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10+13, 5));
		ipPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 21+13, 5));
		
		nickPanel.add(nickLabel); nickPanel.add(nickTextF);
		portPanel.add(portLabel); portPanel.add(portTextF);
		ipPanel.add(ipLabel); ipPanel.add(ipTextF);
		
		
		container.add(nickPanel);
		container.add(portPanel);
		container.add(ipPanel);
		container.add(connectBtn);
		
		setVisible(true);
	}
	
	public JButton getConnectBtn(){
		return connectBtn;
	}
	
	public JTextField getPortTextF(){
		return portTextF;
	}
	
	public JTextField getIPTextF(){
		return ipTextF;
	}
	
	public String getNick(){
		return nickTextF.getText().trim();
	}
	
	public JTextField getNickTextF(){
		return nickTextF;
	}
}
