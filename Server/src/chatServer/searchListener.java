package chatServer;
import java.io.*;
import java.net.*;
import java.util.*;

public class searchListener extends Thread{
static final int Capacity=100;
ListenerCheck aListener=new ListenerCheck();
boolean running=true;
private int port;
private ServerDB myDB;
private ServerSocket sServer;
private Vector<String> Users=new Vector<String>();
class ListenerCheck extends Observable{
	private int count=0;
	public void doAddUser(){
		count++;
		if(count>=Capacity)
			this.notifyObservers();
	}
}
public void addUser(String usr){
	if(Users.contains(usr)==false){
	Users.add(usr);
	aListener.doAddUser();
	}
}
public void removeUser(String usr){
	if(Users.contains(usr)){
		Users.remove(usr);
		aListener.doAddUser();
	}
}
public searchListener(int rsport,ServerDB aDB,ServerNet snet){
	myDB=aDB;
	port=rsport;
	this.aListener.addObserver(snet);
	try {
		sServer=new ServerSocket(port);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
public void run(){
	Socket socket;
	while(running){
		if(Users.size()>=Capacity)
			continue;
		try {
			socket = sServer.accept();
			new OneListener(Users,socket,myDB).start();
			System.out.println("a new Seacher running");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

}
class OneListener extends Thread{
	private Vector<String> Users;
	private ServerDB myDB;
	private Socket socket;
	boolean running=true;
	private BufferedReader is;
	private PrintStream os;
	private ObjectOutputStream obos;
	public OneListener(Vector<String> Users,Socket socket,ServerDB aDB){
		this.socket=socket;
		myDB=aDB;
		this.Users=Users;
	}
	public void finalize(){
		Users.remove(Users.size()-1);
		System.out.println("a Searcher Stopped");
		try {
			super.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void run(){
		
		String rec="";
		Vector<String> rslist=null;
			try {
				is=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				os=new PrintStream(socket.getOutputStream());
				obos=new ObjectOutputStream(socket.getOutputStream());
				while(running){
				rec=is.readLine();
				System.out.println("searchReq: "+rec);
				int cas=switchOP(rec);
				if(cas==0)
					continue;
				if(cas==1){
					if(Adding(rec)){
						System.out.println("AddFriends@OK");
						os.println("AddFriends@OK");
					}
					else{
						System.out.println("AddFriends@DENY");
						os.println("AddFriends@DENY");
					}
				}
				if(cas==2){
					rslist=getSearchResult(rec);
					if(rslist==null){
						System.out.println("SearchFriends@DENY");
						os.println("SearchFriends@badReq");
					}
					else{
						System.out.println("SearchFriends@OK");
						os.println("SearchFriends@OK");
					}
					obos.writeObject(rslist);
					obos.flush();
				}
				os.flush();
			   }
				is.close();
				os.close();
			    obos.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				running=false;
				System.out.println("searching service:"+running);
				System.out.println(e.getMessage());
				//e.printStackTrace();
			}
		
	}
	public int switchOP(String req){//0 continue, 1 add, 2 search
		String[] s=req.split("@");
		if(s.length<2){
			System.out.println("length not match:"+s.length);
			return -1;
		}
		String header=s[0],data=s[1];
		if(header.equals("AddFriends")){
			s=data.split(":");
			return 1;
		}
		if(header.equals("SearchFriends"))
			return 2;
		System.out.println("header ERROR: "+header);
		return 0;
	}
	public boolean Adding(String res){

		String[]s=res.split("@");
		String data=s[1];
		s=data.split(":");
		String userID=s[1],fID=s[0];
		if(myDB.addFriend(userID, fID)==false)
		   return false;
		
		return true;
	}
	public Vector<String> getSearchResult(String rec){
		Vector<String> rslist=null;
		String[] s=rec.split("@");
		String data=s[1];
		s=data.split(":");
		String userID=s[1],match=s[0];
		rslist=myDB.getPersons(userID, match);
		if(rslist==null)
			System.out.println("null friends error:");
		
		return rslist;
	}

}
