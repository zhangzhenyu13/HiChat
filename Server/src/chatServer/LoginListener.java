package chatServer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Observable;
import java.util.Vector;


//listener check for online guys
public class LoginListener extends Thread{
	//label
	static final int setUpline=9;
	static int ServicePort=1024;//next useful port
	static String userLogin,userLogout,sysLogin,sysLogout,userReg,sysReg;
	static int badPort;
	static String badReq,goodReq;
	//service
	Vector<serviceProvider> Service;
	boolean running=true;
	DatagramSocket loginCon;
	ServerDB dataBase;
	LoginHelper loginhelper=new LoginHelper();
class LoginHelper extends Observable{
}
	
	//constructor
	public LoginListener(int port,int servport,serviceProvider sprd,ServerDB db){
		if(port>ServicePort)
		ServicePort=port;
		Service=new Vector<serviceProvider>();
		Service.add(sprd);
		dataBase=db;
		//System.out.println("LoginListener:"+dataBase+":"+db);
		try {
			loginCon=new DatagramSocket(ServicePort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServicePort=servport;
		try {
			BufferedReader bfr=new BufferedReader(new FileReader("Data//setup.txt"));
			for(int i=0;i<setUpline;i++)
				bfr.readLine();
			userLogin=bfr.readLine();
			sysLogin=bfr.readLine();
			userLogout=bfr.readLine();
			sysLogout=bfr.readLine();
			userReg=bfr.readLine();
			sysReg=bfr.readLine();
			String s="";
			s=bfr.readLine();
			badPort=Integer.parseInt(s.substring(8));
			badReq=bfr.readLine().substring(11);
			goodReq=bfr.readLine().substring(12);
			bfr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			/*
			System.out.println(userLogin);
			System.out.println(sysLogin);
			System.out.println(userLogout);
			System.out.println(sysLogout);
			System.out.println(userReg);
			System.out.println(sysReg);
			System.out.println(badPort);
			System.out.println(badReq);
			System.out.println(goodReq);
			*/
		}
	}
	//run
	public void run(){
		String rec="";
		while(running){
			try {
			
			//receive login request
			byte[] buf=new byte[256];
			DatagramPacket packet=new DatagramPacket(buf,buf.length);
			loginCon.receive(packet);
			rec=new String(packet.getData());
			rec=rec.trim();
			System.out.println(rec);
			//response login/logout request with proper port
		    
			if(swicthRequest(rec,packet));
			onlineUsers(true);
			}catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();	
			}
		}
	}
	//swicth packet: log(in/out) , register
	private boolean swicthRequest(String s,DatagramPacket packet){//s->request string -1 igonre, 1 login 2 logout 3 resgister 
		int tag=-1;
		String info[]=s.split("@");
		if(info.length<2)
			return false;
		String header=info[0]+"@";
		String data=s.substring(header.length());
		if(data.split(":").length<2)
			return false;
		String userID=data.split(":")[0];
		if(header.equals(userLogin)){
			System.out.println("LOGINREQ");
			if(trylogin(data))
			    tag=1;
			else
				respondLogin(data,packet,false);
		}
		if(header.equals(userLogout)){
			System.out.println("LOGOUTREQ");
			if(trylogout(userID))
			    tag=2;
			else
			    respondLogout(data,packet,false);
		}
		if(header.equals(userReg)){
			System.out.println("REGREQ");
			if(tryregister(data))
			    tag=3;
			else
				respondResgister(data,packet,false);
		}
		switch(tag){
			case 1:
				respondLogin(data,packet,true);
				break;
			case 2:
				respondLogout(data,packet,true);
				break;
			case 3:
				respondResgister(data,packet,true);
				break;
			default:
				break;
		}
		if(tag!=-1)
			return true;
		else
			return false;
	}
	//login
	private boolean trylogin(String s){//s->userID:userCode
		boolean tag=false;
		String sp[]=s.split(":");
		if(sp.length!=2) return false;
		String user=sp[0],code=sp[1];
		
		for(int i=0;i<Service.size();i++){
			if(Service.get(i).Users.contains(user))
				return false;
		}
		tag=dataBase.Login(user+":"+code);
		
		System.out.println("SYS CHEKLOGIN:"+tag);
		return tag;
	}
	//logout
	private boolean trylogout(String s){//s->userID
		boolean tag=false;
		for(int i=0;i<Service.size();i++){
			if(Service.get(i).Users.contains(s)){
				Service.get(i).removeUser(s);
				tag=true;
				break;
			}
		}
		if(tag==false)
			return tag;
		
		if(dataBase.isOnline(s)){
			dataBase.Logout(s);
		}
		System.out.println("SYS CHECKLOGOUT:"+tag);
		return tag;
	}
	//register
	private boolean tryregister(String s){//s->userID:userCode:userName
		boolean tag=false;
		System.out.println("tryReg"+s);
		String info[]=s.split(":");
		if(info.length!=4)
			return false;
		UserInfo aUser=new UserInfo();
		aUser.UserID=info[0];
		aUser.UserCode=info[1];
		aUser.UserName=info[2];
		aUser.Sex=info[3];
		if(dataBase.addUser(aUser))
			tag=true;
		System.out.println("REG CHECKIN:"+tag);
		return tag;
	}
	
	//respond to login
	private void respondLogin(String rec,DatagramPacket packet,boolean OKlogin){//rec->userID:userCode
		//send repond
		
		String res="";
		String userID=rec.split(":")[0];
		byte[] buf;
		int port=packet.getPort();
		InetAddress addr=packet.getAddress();
		if(OKlogin){
			res=sysLogin+rec+":"+goodPort(userID);
		}
		else{
			res=sysLogin+rec+":"+badPort;
		}
		System.out.println(res);
		buf=res.getBytes();
		packet=new DatagramPacket(buf,buf.length,addr,port);
		try {
			loginCon.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//respond logout
	private void respondLogout(String rec,DatagramPacket packet,boolean OKlogout){//rec->userID:userCode
		//send respond
		
		String res="";
		byte[] buf;
		int port=packet.getPort();
		InetAddress addr=packet.getAddress();
		if(OKlogout){
			res=sysLogout+rec+":"+goodReq;
		}
		else{
			res=sysLogout+rec+":"+badReq;
		}
		System.out.println(res);
		buf=res.getBytes();
		packet=new DatagramPacket(buf,buf.length,addr,port);
		try {
			loginCon.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//respond register
	private boolean respondResgister(String rec,DatagramPacket packet,boolean OKreg){//rec->id:code:name
		boolean tag=false;
		String res="";
		byte[] buf;
		int port =packet.getPort();
		InetAddress addr=packet.getAddress();
		if(OKreg){
			res=sysReg+rec+":"+goodReq;
		}
		else{
			res=sysReg+rec+":"+badReq;
		}
		System.out.println(res);
		buf=res.getBytes();
		packet= new DatagramPacket(buf,buf.length,addr,port);
		try {
			loginCon.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tag;
	}
	//count online users
	private Integer onlineUsers(boolean tag){
			Integer count=0;
			String s="";
			for(int i=0;i<Service.size();i++){
				count+=Service.get(i).Users.size();
				s+="port "+Service.get(i).port+" : "+Service.get(i).Users.size()+"/"+serviceProvider.Capacity+"\n";
			}
			System.out.println("current online Users: "+count);
			if(tag=true)
			   System.out.println(s);
			return count;
		}
		//good port
		
	private int goodPort(String user){
			//System.out.println("GoodPort size="+Service.size());
		/*if(true){
		
			Service.get(0).addUser(user);
			return Service.get(0).port;
		}*/
			for(int i=0;i<Service.size();i++){
				//System.out.println("port("+Service.get(i).port+") is ");
				if(Service.get(i).isAble()){
					//System.out.println("trying to ");
					if(Service.get(i).addUser(user)){
						//System.out.println("serve");
						return Service.get(i).port;
					}
				}
				//System.out.println("busy");
			}
			serviceProvider sprd=new serviceProvider(ServicePort,dataBase);
			sprd.start();
			sprd.addUser(user);
			Service.add(sprd);
			return ServicePort++;
		}
}
