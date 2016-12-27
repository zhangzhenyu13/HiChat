package UI;
import java.io.*;
import java.net.*;

public class Offline{
private DatagramSocket connect=null;
private String userID,userCode;

public Offline(String user,String code){

	userID=user;
	userCode=code;
	try {
		InetAddress addr=InetAddress.getByName(cmdConnect.hostStr);
		connect=new DatagramSocket();
		connect.setSoTimeout(3*cmdConnect.timeout);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		System.out.println("Logout Error:"+e.getMessage());
		e.printStackTrace();
	}
	
}
public boolean Close(){
	int count=0;
	boolean tag=false;
	DatagramPacket packet;
	byte[] buf=new byte[256];
	String res="",req="";
	try{
		while(!tag&&count++<3){
		req=cmdConnect.userLogout+userID+":"+userCode;
		buf=req.getBytes();
		InetAddress addr=InetAddress.getByName(cmdConnect.hostStr);
		packet=new DatagramPacket(buf,buf.length,addr,cmdConnect.servicePort);
		connect.send(packet);
		System.out.println(req);
		//
		buf=new byte[256];
		packet=new DatagramPacket(buf,buf.length);
		
		connect.receive(packet);
		res=(new String(packet.getData())).trim();
		String s=cmdConnect.sysLogout+req.substring(10)+":";
		if(res.substring(0, s.length()).equals(s)){
			if(res.substring(s.length()).equals(cmdConnect.goodReq))
			    tag=true;
			else
				tag=false;
		}
		}
		connect.close();
	}catch(IOException e){
		e.printStackTrace();
		return false;
	}
	return tag;
}
}
