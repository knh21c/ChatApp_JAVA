import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import javax.swing.JTextArea;

public class Server {
	private Server_View exView;
	private int PORT = 15001;
	private ServerSocket sSocket;
	static JTextArea logText;
	public static Vector<User_Info> totalUsers = new Vector<User_Info>();
	public static Vector<ChatRoom> rooms = new Vector<ChatRoom>();

	public static void main(String[] args) {
		Server ex = new Server();
		ex.init();
	}

	public void init() {
		exView = new Server_View();
		logText = exView.getLogText();

		try {
			sSocket = new ServerSocket(PORT);
			logText.append("��Ʈ: " + PORT + " ���� ����\n");
		} catch (IOException e) {
			logText.append("��Ʈ: " + PORT + " ���� ����\n");
			e.printStackTrace();
		}
		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					try {
						Socket cSocket = sSocket.accept();
						logText.append("���ӿ�û...\n");
						User_Info user = new User_Info(cSocket);
						totalUsers.add(user);
						user.start();
						logText.append(user.getNick() + " ���� ����\n");

					} catch (Exception e) {
						e.printStackTrace();
						logText.append("Runnable���� ���� �߻�\n");
						break;
					}
				}
			}
		});
		th.start();
	}
	
	public static void addText(String msg){
		logText.append(msg);
		logText.moveCaretPosition(logText.getText()
				.length() - 1);
	}
}
