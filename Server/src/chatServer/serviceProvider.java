package chatServer;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Observable;
import java.util.Vector;
public class serviceProvider extends Thread{
static final int Capacity=4;
int port ;
boolean running=true;
ServerDB dataBase;
ServerSocket server;
Vector<String> Users=new Vector<String>();
HashMap<String,NetManager> Unet=new HashMap<String,NetManager>();
//LoginCheck	
	class LoginCheck extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("Login Check Service is running");
			while(true){
				//chekc online users every 5 minutes
				try {
					Thread.sleep(5*60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//remove those offline
				for(int i=0;i<Users.size();i++){
					NetManager nm=Unet.get(Users.get(i));
					if(nm==null){
					    Users.remove(i);
					    System.out.println("remove a login jam error User");
					    i--;
					}
					else{
						if(nm.sed.running==false||nm.rec.running==false){
							removeUser(Users.get(i));
							System.out.println("remove an offline User");
							i--;
						}
					}
					
				}
			}
		}
		
	}
//netStack
class NetManager{
	sender sed;
	receiver rec;
	Socket socket;
	NetManager(sender sd1,receiver rc1,Socket s1){
		sed=sd1;
		rec=rc1;
		socket=s1;
	}
}
//
public  boolean removeUser(String user){
	if(Users.contains(user)){
		Users.remove(user);
		NetManager netm=Unet.get(user);
		netm.rec.running=false;
		netm.sed.running=false;
		try {
			netm.socket.shutdownInput();
			netm.socket.shutdownOutput();
			netm.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Unet.remove(user);
	}
	else
		return false;
	
	return true;
}
public  boolean addUser(String user){
	
	//System.out.println("p trying to add usr");
	if(Users.contains(user)==false){
		Users.add(user);
		//System.out.println("serviceProvider("+port+") add user: "+user);
	}
	else
		return false;
	
	return true;
}
public  boolean isAble(){
	if(Users.size()<Capacity)
		return true;
	else
		return false;
}
public serviceProvider(int port,ServerDB db){
	this.port=port;
	dataBase=db;
	try {
		server=new ServerSocket(port);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		
	}
	LoginCheck lc=new LoginCheck();
	lc.setDaemon(true);
	lc.start();
}
private synchronized boolean acceptUser(){
	Socket service;
	try {
		BufferedReader is;
		BufferedWriter print;
		ObjectOutputStream os;
		service = server.accept();
		is=new BufferedReader(new InputStreamReader(service.getInputStream()));
		os=new ObjectOutputStream(service.getOutputStream());
		print=new BufferedWriter(new OutputStreamWriter(service.getOutputStream()));
		String userID=is.readLine();
		UserInfo auser=dataBase.getUser(userID);
		print.write(auser.toString());
		print.newLine();
		print.flush();
		//os.writeObject(dataBase.getUser(userID));
		//os.flush();
		os.writeObject(dataBase.getFriends(userID));
		os.flush();
		//
		if(Users.contains(userID)==false){
			is.close();
			os.close();
		    service.close();
			return false;
		}
		//is.close();
		
		sender aSender=new sender(service,dataBase,userID);
		receiver aRec=new receiver(service,dataBase,userID);
		NetManager netm=new NetManager(aSender,aRec,service);
		Unet.put(userID, netm);
		aSender.start();
		aRec.start();
		System.out.println("port("+port+")accept a user:"+userID);
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return false;
	}
	return true;
}

public void run(){

	while(running){
	   if(Users.size()<Capacity)
	       System.out.println("port "+port+" is waiting for service:"+Users.size()+"/"+Capacity);
	   else{
		   running=false;
		   continue;
	   }
	   acceptUser();
	}
	try {
		server.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}

}

//receiver
class receiver extends Thread {
	ServerDB myDB;
	String userID="";
	boolean running=true;
	Socket socket;
	BufferedReader is;
	PrintWriter os;
	public receiver(Socket so,ServerDB db,String user){
		userID=user;
		socket =so;
		myDB=db;
		try {
			is=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			os=new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			running=false;
			System.out.println("receiver Init error:"+e.getMessage());
			e.printStackTrace();
		}
		
	}
	private String parseMsg(String s){
		String info[]=s.split("@");
		if(info.length<2){
			//System.out.println("length of split="+info.length);
		    return s;	
		}
		//System.out.println("can be parsed");
		String header=info[0];
		String data=s.substring(header.length());
		info=header.split("->");
		header=info[0];
		userID=info[1];
		userID=userID.split(":")[1];
		header="from "+header.split(":")[0];
		
		return header+data;
	}
	
	public void run(){
		try {
			while(running){
			String msg=is.readLine();
			//os.println(msg);	
			//os.flush();
			if(msg!=null){
			//System.out.println("received "+msg);
			msg=parseMsg(msg);
			myDB.Writecache(userID, msg);
			}
			}
			is.close();
		    os.close();  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			running=false;
			System.out.println("now receiver running:"+running);
			System.out.println("receiver error:"+e.getMessage());
			e.printStackTrace();
			
		}
		
	}
}
//sender
class sender extends Thread{
	ServerDB myDB;
	String userID="";
	boolean running=true;
	Socket socket;
	BufferedReader is;
	PrintWriter os;
	public String[] parseMsg(String str){
		String[] s=str.split("ENDSTR");
		return s;
	}
	public sender(Socket so,ServerDB db,String user){
		userID=user;
		socket =so;
		myDB=db;
		try {
			is=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			os=new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			running=false;
			System.out.println("sender Init error:"+e.getMessage());
			e.printStackTrace();
		}
	}
	public void run(){
		Integer delay=1;
		try {
			while(running){
				
			//String msg=is.readLine();
			Thread.sleep(500*delay);
			//System.out.println("hi running the receiver.");
			//os.println("Hi we are System.");
			//os.flush();
			String s=myDB.ReadCache(userID);
			if(s!=null){
			if(delay>1)
				delay--;
			for(String str:parseMsg(s)){
				os.println(str);
				os.flush();
				//System.out.println(str+" was sent");
			}
			}
			else{
				if(delay<10)
				delay++;
			}
			//System.out.println("we send HiSys");
			}
			is.close();
		    os.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			running=false;
			System.out.println("now sender running:"+running);
			System.out.println("sender error:"+e.getMessage());
			e.printStackTrace();
		}
		
	}
}
