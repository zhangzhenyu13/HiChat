package chatServer;
import java.net.*;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import chatServer.ServerDB.Counter;

import java.io.*;
public class ServerNet implements Observer{
int portNum,servicePort,searchPort;
ServerSocket server;
DatagramSocket datasocket;
ServerDB AccessDB;
Vector<searchListener> searchManager=new Vector<searchListener>();
Vector<LoginListener> loginManager=new Vector<LoginListener>(); 
void initBGK(){	
	try {
		serviceProvider sprd=new serviceProvider(servicePort++,AccessDB);
		sprd.start();
		LoginListener login=new LoginListener(portNum,servicePort,sprd,AccessDB);
		login.loginhelper.addObserver(this);
		loginManager.add(login);
		login.start();
		searchListener search=new searchListener(searchPort,AccessDB,this);
		searchManager.add(search);
		search.start();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
void loadData(){
	try {
		BufferedReader bfr=new BufferedReader(new FileReader("Data//setup.txt"));
		String s="";
		for(int i=0;i<6;i++)
			bfr.readLine();
		s=bfr.readLine();
		s=s.substring(11, s.length());
		portNum=Integer.parseInt(s);
		s=bfr.readLine();
		s=s.substring(13, s.length());
		servicePort=Integer.parseInt(s);
		s=bfr.readLine();
		s=s.substring(12);
		searchPort=Integer.parseInt(s);
		System.out.println("Login Service Port:"+portNum);
		System.out.println("talking Service Port start from:"+servicePort);
		System.out.println("Search Service Port start from:"+searchPort);
		bfr.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
public ServerNet(ServerDB db){
	AccessDB=db;
	loadData();
	initBGK();
}
public void Watch(){
	
	while(true){
		
	}
}
public void finalize(){
	System.out.println("Server Closed");
	try {
		super.finalize();
	} catch (Throwable e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

public void update(Observable o, Object arg) {
	// TODO Auto-generated method stub
	searchListener search=new searchListener(searchPort++,AccessDB,this);
	searchManager.add(search);
	search.start();
	
}
}



