/*
 * mainUI interface
 */
package UI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.BoxLayout;
import java.awt.Color;

public class chatUI extends JFrame implements ActionListener, ListSelectionListener{

	//private JFrame frame;
	private JList list;
	private JTextField textField;
	private JTextArea textArea;
	private JButton btnNewButton;
	private JMenuItem mntmLogout;
	private JMenuItem mntmLogin ;
	private Login login;
	private Offline logout;
//data
	private JButton AddfriendButton;
	private msgDisplay msgShow;
	private Vector<String> flist=new Vector<String>();
	private SendMsg sender;
	private ReceiveMsg receiver;
	private Socket connect;
	String host="";
	int portCon=1030,portMulti=4446;
	boolean chatOK=false;
	UserInfo Thisuser=new UserInfo();
	String userID="",userCode="",userName="";
	String ReceivePerson="";
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		new cmdConnect();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					chatUI window = new chatUI();
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public chatUI() {
		setTitle("HiChat");
		initialize();
		//tryConnect();
		msgShow=new msgDisplay(textArea);
		
	}

	//datainit
	private boolean tryConnect(){
		boolean tag=false;
		try {
			BufferedWriter os;
			ObjectInputStream is;
			BufferedReader is1;
			InetAddress addr=InetAddress.getByName(host);
			connect=new Socket(addr,portCon);
			os=new BufferedWriter(new OutputStreamWriter(connect.getOutputStream()));
			is=new ObjectInputStream(connect.getInputStream());
			is1=new BufferedReader(new InputStreamReader(connect.getInputStream()));
			os.write(userID);os.newLine();
			os.flush();
			Thisuser.parseUser(is1.readLine());
			//Object obj=is.readObject();
			//System.out.println(obj.getClass());
			//Thisuser=(UserInfo)is.readObject();
			flist=(Vector<String>)is.readObject();
			list.removeAll();
			list.setListData(flist);
			//for(String fs:flist)
			//	System.out.println(fs);
			//connect.shutdownInput();
			//connect.shutdownOutput();
			tag=true;
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		sender=new SendMsg(connect);
		receiver=new ReceiveMsg(connect,msgShow);
		receiver.start();
		return tag;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("act:"+arg0.getActionCommand());
		//login
		if(arg0.getActionCommand()=="Login"){
			login=new Login(this);
			if(chatOK){
				if(this.tryConnect()){
				btnNewButton.setEnabled(true);
				mntmLogin.setEnabled(false);
				mntmLogout.setEnabled(true);
				AddfriendButton.setEnabled(true);
				}
				else{
					textArea.setText("SYS Connect BreakDown");
					btnNewButton.setEnabled(false);
					mntmLogin.setEnabled(true);
					mntmLogout.setEnabled(false);
					AddfriendButton.setEnabled(false);
				}
			}
			return;
		}
		//logout
		if(arg0.getActionCommand().equals("Logout")){
			logout=new Offline(userID,userCode);
			btnNewButton.setEnabled(false);
			if(logout.Close()){
				AddfriendButton.setEnabled(false);
				mntmLogin.setEnabled(true);
				mntmLogout.setEnabled(false);
				btnNewButton.setEnabled(false);
			    msgShow.setMsgDisplay("SYS:Offline!");
				try {
					connect.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				btnNewButton.setEnabled(true);
				AddfriendButton.setEnabled(true);
			}
			return;
		}
		//send
		if(arg0.getActionCommand().equals("Send")){
		if(ReceivePerson.equals("")||ReceivePerson==null)
			return;
		String s=textField.getText();
		String header="";
		SimpleDateFormat sdf=new SimpleDateFormat("MM-DD HH:MM");
		header=Thisuser.UserName+"->"+ReceivePerson;
		s="["+sdf.format(new Date())+"] "+s;
		sender.sendmsg(header+"@"+s+"ENDSTR");
		header="to "+ReceivePerson;
		msgShow.setMsgDisplay(header+s);
		textField.setText("");
		return;
		}
		//addfriend
		if(arg0.getActionCommand().equals("AddFriend")){
			searchFriends srcf=new searchFriends(userID,flist,list);
			return;
		}
	}
	//closewindow
	public void dispose(){
		logout=new Offline(userID,userCode);
		logout.Close();
		try {
			connect.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.dispose();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		this.setResizable(false);
		this.setBounds(100, 100, 505, 395);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		
		JMenu mnMenu = new JMenu("Menu");
		menuBar.add(mnMenu);
		
		mntmLogin = new JMenuItem("Login");
		mntmLogin.setActionCommand("Login");
		mntmLogin.setEnabled(true);
		mnMenu.add(mntmLogin);
		mntmLogin.addActionListener(this);
		
		mntmLogout = new JMenuItem("Logout");
		mntmLogout.setActionCommand("Logout");
		mntmLogout.addActionListener(this);
		mntmLogout.setEnabled(false);
		mnMenu.add(mntmLogout);
		this.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		this.getContentPane().add(panel, BorderLayout.NORTH);
		
		JLabel lblChatarea = new JLabel("ChatArea     ");
		lblChatarea.setForeground(Color.GREEN);
		lblChatarea.setFont(new Font("ËÎÌו", Font.PLAIN, 19));
		panel.add(lblChatarea);
		
		JLabel lblHichat = new JLabel("Hichat");
		lblHichat.setForeground(Color.CYAN);
		lblHichat.setFont(new Font("ËÎÌו", Font.BOLD, 25));
		panel.add(lblHichat);
		
		JLabel lblFreinds = new JLabel("     Freinds");
		lblFreinds.setForeground(Color.PINK);
		lblFreinds.setFont(new Font("ËÎÌו", Font.PLAIN, 19));
		panel.add(lblFreinds);
		
		AddfriendButton = new JButton("AddFriend");
		AddfriendButton.addActionListener(this);
		AddfriendButton.setEnabled(true);
		AddfriendButton.setEnabled(false);
		panel.add(AddfriendButton);
		
		JPanel panel_1 = new JPanel();
		this.getContentPane().add(panel_1, BorderLayout.SOUTH);
		
		textField = new JTextField();
		panel_1.add(textField);
		textField.setColumns(20);
		
		btnNewButton = new JButton("Send");
		btnNewButton.setEnabled(false);
		panel_1.add(btnNewButton);
		btnNewButton.addActionListener(this);
		
		JPanel panel_2 = new JPanel();
		this.getContentPane().add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));
		
		JScrollPane scrollPane = new JScrollPane();
		panel_2.add(scrollPane);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setColumns(30);
		textArea.setRows(15);
		scrollPane.setViewportView(textArea);
		
		JLabel lblI = new JLabel("   ");
		lblI.setForeground(Color.GREEN);
		lblI.setBackground(Color.CYAN);
		panel_2.add(lblI);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		panel_2.add(scrollPane_1);
		
		list = new JList(flist);
		list.setForeground(Color.GREEN);
		scrollPane_1.setRowHeaderView(list);
		list.addListSelectionListener(this);
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		// TODO Auto-generated method stub
		
		ReceivePerson=list.getSelectedValue().toString();
		String s[]=ReceivePerson.split(":");
		if(s.length<3)
			AddfriendButton.setEnabled(true);
		else
			AddfriendButton.setEnabled(true);
	}

}
class msgDisplay{
	JTextArea display;
	public msgDisplay(JTextArea disp){
		display=disp;
	}
    public synchronized void setMsgDisplay(String s){
    	int colleng=50;
    	int seg=s.length()/colleng;
    	String text="";
    	int i=0;
    	for(i=0;i<seg;i++){
    		String str=s.substring(i*colleng, (i+1)*colleng);
    		text+=str+"\n";
    	}
    	text+=s.substring(i*colleng)+"\n";
    	text=display.getText()+text;
		display.setText(text);
    }
}
