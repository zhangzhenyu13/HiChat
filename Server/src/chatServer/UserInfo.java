package chatServer;

import java.io.*;

public class UserInfo implements Serializable{
	static final int nfriedns=20;
	String Sex;
	String UserID;
	String UserName;
	String UserCode;
	public String toString(){
		String user="";
		user=UserID+":"+UserCode+":"+UserName+":"+Sex;
		return user;
	}
	public boolean parseUser(String s){
		String info[]=s.split(":");
		if(info.length!=4)
			return false;
		UserID=info[0];
		UserCode=info[1];
		UserName=info[2];
		Sex=info[3];
		return true;
	}
	public UserInfo(String id,String name,String code,String sex){
		Sex=sex;
		UserID=id;
		UserName=name;
		UserCode=code;
		
	}
	public UserInfo(){
		Sex="male";
		UserID="";
		UserName="";
		UserCode="";
	}
	private void writeObject(java.io.ObjectOutputStream out)throws IOException{
		out.writeInt(UserName.getBytes().length);
		out.writeBytes(UserName);
		out.writeInt(UserID.getBytes().length);
		out.writeBytes(UserID);
		out.writeInt(UserCode.getBytes().length);
		out.writeBytes(UserCode);
		out.writeInt(Sex.getBytes().length);
		out.writeBytes(Sex);
	}
	private void readObject(java.io.ObjectInputStream in)throws IOException, ClassNotFoundException{
			 byte[] buf;
			 int n=0;
			 n=in.readInt();
			 buf=new byte[n];
			 for(int i=0;i<n;i++)
				 buf[i]=in.readByte();
		     UserName=new String(buf);
		     n=in.readInt();
			 buf=new byte[n];
			 for(int i=0;i<n;i++)
				 buf[i]=in.readByte();
			 UserID=new String(buf);
			 n=in.readInt();
			 buf=new byte[n];
			 for(int i=0;i<n;i++)
				 buf[i]=in.readByte();
			 UserCode=new String(buf);
			 n=in.readInt();
			 buf=new byte[n];
			 for(int i=0;i<n;i++)
				 buf[i]=in.readByte();
			 Sex=new String(buf);
	}
}