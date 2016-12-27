package UI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import javax.swing.JLabel;
public class searchFriends extends JDialog implements ActionListener {
    private Socket connect=null;
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	JList list ,JFlist;
	JButton addButton,searchButton;
	private String userID="";
    private Vector<String> flist,searchResult=new Vector<String>();//usrName+":"+usrID+[:friends]
    private BufferedReader is;
    private PrintStream os;
    private ObjectInputStream obis; 
	/**
	 * init UI.
	 */
    private void initUI(){
    	//
		setTitle("Add Friends");
		setBounds(100, 100, 222, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane);
			{
				list = new JList(searchResult);
				scrollPane.setViewportView(list);
			}
		}
		{
			addButton = new JButton("Add This Friend");
			addButton.addActionListener(this);
			addButton.setActionCommand("Add");
			contentPanel.add(addButton, BorderLayout.SOUTH);
			addButton.setEnabled(false);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.NORTH);
			{
				textField = new JTextField();
				buttonPane.add(textField);
				textField.setColumns(10);
			}
			{
				searchButton = new JButton("Search");
				searchButton.addActionListener(this);
				searchButton.setActionCommand("Search");
				buttonPane.add(searchButton);
				getRootPane().setDefaultButton(searchButton);
				searchButton.setEnabled(true);
			}
		}
		
    }
	/**
	 * Create the dialog.
	 */
	public searchFriends(String id,Vector<String> fvect,JList fl) {
		try {
			connect=new Socket(cmdConnect.hostStr,cmdConnect.searchPort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			os=new PrintStream(connect.getOutputStream());
			is=new BufferedReader(new InputStreamReader(connect.getInputStream()));
			obis=new ObjectInputStream(connect.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JFlist=fl;
		flist=fvect;
		userID=id;
		initUI();
		System.out.println("add open");
		//this.setModal(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getActionCommand().equals("Search")){
		    if(trySearch()){
		    	addButton.setEnabled(true);
		    	for(int i=0;i<searchResult.size();i++){
		    		if(flist.contains(searchResult.get(i)))
		    			searchResult.remove(i--);
		    	}
		    	list.removeAll();
		    	list.setListData(searchResult);
		    	textField.setText("These are Results!");
		    }
		    else{
		    	textField.setText("No Result!");
		    	addButton.setEnabled(false);
		    }
			return;
		}
		if(arg0.getActionCommand().equals("Add")){
			if(tryAdd()){
				flist.add((String)list.getSelectedValue());
				JFlist.removeAll();
				JFlist.setListData(flist);
				textField.setText("Friends Added!");
			}
			else{
				textField.setText("Friends Adding Error!");
			}
			return;
		}
	}

	
	private boolean trySearch(){
		String req=cmdConnect.searchFriend+textField.getText()+":"+userID;
		os.println(req);
		os.flush();
		try {
			String res=is.readLine();
			if(res.equals(cmdConnect.searchFriend+cmdConnect.goodReq)==false){
			    System.out.println("BAD response->"+res);
			    System.out.println("needed->"+cmdConnect.searchFriend+cmdConnect.goodReq);
				return false;
			}
			Vector<String> tmrs=searchResult;
			tmrs=(Vector<String>)obis.readObject();
			if(tmrs==null)
				return false;
			else
				searchResult=tmrs;
			return true;
			}
		catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	private boolean tryAdd(){
		if(searchResult.isEmpty()||list.isSelectionEmpty())
			return false;
		String s=(String)list.getSelectedValue();
		String[] info=s.split(":");
		String req=cmdConnect.addFriend+info[1]+":"+userID;
        try {
			os.println(req);
			os.flush();
			System.out.println(req);
			String res=is.readLine();
			if(res.equals(cmdConnect.addFriend+cmdConnect.badReq))
				return false;
			if(res.equals(cmdConnect.addFriend+cmdConnect.goodReq))
				return true;
			else
				return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
public void finalize(){
	try {
		is.close();
		os.close();
		connect.close();
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	try {
		super.finalize();
	} catch (Throwable e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}
