/*login
 * register
 * main function: read data and get service port
 * */
package UI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.awt.Component;
import javax.swing.JLabel;
import java.awt.Window.Type;
import javax.swing.JRadioButton;

public class Login implements ActionListener, ItemListener{

	private chatUI chat=null;
	private JDialog dlg=null;
	private final JPanel contentPanel = new JPanel();
    private JTextField UserIDText;
    private JTextField UserCodeText;
    private JTextField UserNameText;
    //
    private JLabel lblTips;
    private JPanel UserNamePane;
    private JRadioButton Male,Female;
    //
	private boolean sex=true;
	public Login(JFrame j) {
		chat=(chatUI)j;
		dlg=new JDialog(j,"Login/Register",true);
		dlg.setResizable(false);
		windowInit();
		
	}

	//login
	private boolean tryLogin(){
		int count=0;
		//
		InetAddress addr;
		String res="",req="";
		int port;
		DatagramSocket connect=null;
		
		try {
			connect=new DatagramSocket();
			while(count++<3){
			//read setup.txt
			port=cmdConnect.servicePort;
			addr = InetAddress.getByName(cmdConnect.hostStr);
			
			connect.setSoTimeout(cmdConnect.timeout);
			
			//user request
			String code=UserCodeText.getText(),userID=UserIDText.getText();
			req=cmdConnect.userLogin+userID+":"+code;
			byte[] buf=req.getBytes();
			DatagramPacket packet=new DatagramPacket(buf,buf.length,addr,port);
			
			connect.send(packet);
			//server response
			buf=new byte[256];
			packet=new DatagramPacket(buf,buf.length);
			connect.receive(packet);
			res=new String(packet.getData());
			res=res.trim();
			System.out.println(res);
			String s=cmdConnect.sysLogin+userID+":"+code+":";
			if(res.substring(0, s.length()) .equals(s)){
				if(Integer.parseInt(res.substring(s.length()))==cmdConnect.badPort){
					System.out.println("bad port");
					return false;
				}
				System.out.println("OK LOGIN");
				chat.portCon=Integer.parseInt(res.substring(s.length()));
				chat.host=cmdConnect.hostStr;
				chat.chatOK=true;
				chat.userCode=UserCodeText.getText();
				chat.userID=UserIDText.getText();
				
				return true;
			}
			}
			connect.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Login Error");
			e.printStackTrace();
			
			return false;
		}
		
		return false;
	}
	//register
	private boolean tryRegister(){
		int count=0;
		//
		InetAddress addr;
		String res="",req="";
		int port;
		DatagramSocket connect=null;

		
		try {
			connect=new DatagramSocket();
			while(count++<3){
			//read setup.txt
			port=cmdConnect.servicePort;
			addr = InetAddress.getByName(cmdConnect.hostStr);
			
			connect.setSoTimeout(3*cmdConnect.timeout);
			//user request
			String code=UserCodeText.getText(),userID=UserIDText.getText(),
					name=UserNameText.getText();
			req=cmdConnect.userReg+userID+":"+code+":"+name;
			String Sex="";
			if(sex)
				Sex="male";
			else
				Sex="female";
			req+=":"+Sex;
			byte[] buf=req.getBytes();
			DatagramPacket packet=new DatagramPacket(buf,buf.length,addr,port);
			connect.send(packet);
			//server response
			buf=new byte[256];
			packet=new DatagramPacket(buf,buf.length);
			connect.receive(packet);
			res=new String(packet.getData());
			res=res.trim();
			System.out.println(res);
			String s=cmdConnect.sysReg+userID+":"+code+":"+name+":"+Sex+":";
			if(res.substring(0, s.length()) .equals(s)){
				if(res.substring(s.length()).equals(cmdConnect.badReq)){
					System.out.println("bad reg");
					connect.close();
					return false;
				}
				else if(res.substring(s.length()).equals(cmdConnect.goodReq)){
				   System.out.println("OK Reg");
				   connect.close();
				   return true;
				}
				else {
					connect.close();
					return false;
				}
			}
			System.out.println(s+cmdConnect.goodReq+"/"+cmdConnect.badReq+" <-vs-> "+res);
		}
			connect.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Reg Error");
			e.printStackTrace();
			return false;
		}
		return false;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getActionCommand().equals("Login")){
			if(UserNamePane.isEnabled()){
			UserNamePane.setEnabled(false);
			UserNamePane.setVisible(false);
			lblTips.setText("TIPS:Login");
			}
			else{
			   if(tryLogin())
			   dlg.dispose();
			   else{
				   lblTips.setText("Login Error");
			   }
			}
		}
		else if(arg0.getActionCommand().equals("Cancel")){
			//serverinfo.port=-1;
			//serverinfo.OK=true;
			dlg.dispose();
		}
		else if(arg0.getActionCommand().equals("Register")){
			if(UserNamePane.isEnabled()==false){
			UserNamePane.setEnabled(true);
			UserNamePane.setVisible(true);
			lblTips.setText("TIPS:Register");
			}
			else{
				if(tryRegister()==false){
					UserNameText.setText("");
					UserCodeText.setText("");
					UserIDText.setText("");
					lblTips.setText("Register error");
				}
				else{
					lblTips.setText("Register successful->please try login");
				}
			}
		}

	}
	@Override
	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getSource()==Male)
			sex=true;
		if(arg0.getSource()==Female)
			sex=false;
	}
	/**
	 * Create the dialog.
	 */
	private void windowInit(){
		dlg.setBounds(100, 100, 412, 213);
		dlg.getContentPane().setLayout(new BorderLayout());
		contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		dlg.getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridLayout(4, 1,1,10));
		{
			JPanel TipsPane = new JPanel();
			contentPanel.add(TipsPane);
			{
				lblTips = new JLabel("TIPS:Login");
				TipsPane.add(lblTips);
			}
		}
		{
			JPanel UserIDPane = new JPanel();
			contentPanel.add(UserIDPane);
			{
				JLabel lblNewLabel = new JLabel("UserID  ");
				UserIDPane.add(lblNewLabel);
			}
			{
				UserIDText = new JTextField();
				UserIDPane.add(UserIDText);
				UserIDText.setColumns(25);
			}
		}
		{
			JPanel UserCodePane = new JPanel();
			contentPanel.add(UserCodePane);
			{
				JLabel lblUsercode = new JLabel("UserCode");
				UserCodePane.add(lblUsercode);
			}
			{
				UserCodeText = new JTextField();
				UserCodePane.add(UserCodeText);
				UserCodeText.setColumns(25);
			}
		}
		{
			UserNamePane = new JPanel();
			contentPanel.add(UserNamePane);
			UserNamePane.setEnabled(false);
			UserNamePane.setVisible(false);
			{
				JLabel lblUsername = new JLabel("UserName");
				UserNamePane.add(lblUsername);
			}
			{
				UserNameText = new JTextField();
				UserNamePane.add(UserNameText);
				UserNameText.setColumns(15);
			}
			{
				Male = new JRadioButton("male");
				Male.setSelected(true);
				Male.addItemListener(this);
				UserNamePane.add(Male);
			}
			{
				Female = new JRadioButton("female");
				Female.setSelected(false);
				Female.addItemListener(this);
				UserNamePane.add(Female);
			}
			
			ButtonGroup bg=new ButtonGroup();
			bg.add(Male);
			bg.add(Female);

		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			dlg.getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton LoginButton = new JButton("Login");
				LoginButton.setActionCommand("Login");
				LoginButton.addActionListener(this);
				{
					JButton RegisterButton = new JButton("Register");
					RegisterButton.setActionCommand("Register");
					buttonPane.add(RegisterButton);
					RegisterButton.addActionListener(this);
				}
				buttonPane.add(LoginButton);
				dlg.getRootPane().setDefaultButton(LoginButton);
			}
			{
				JButton CancelButton = new JButton("Cancel");
				CancelButton.setActionCommand("Cancel");
				CancelButton.addActionListener(this);
				buttonPane.add(CancelButton);
			}
		}
		//dlg.setModal(true);
		dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dlg.setVisible(true);
	}



}
