package chatServer;

import java.sql.*;
import java.util.*;
import java.io.*;

public class ServerDB implements Observer{
Connection dbCon;
Statement stmt;
String driver="",url="";
static Integer tryCount=1,num=0;
Counter aCounter=new Counter();

//counter observe
class Counter extends Observable{
    public void doQueryCount(){
	   tryCount++;
	   num++;
	   System.out.println("sql try:"+num);
	   if(tryCount<0){
		   tryCount=1;
		   setChanged();
		   notifyObservers();
	   }
   } 
}
@Override
public void update(Observable o, Object arg) {
	// TODO Auto-generated method stub
	if((o instanceof Counter)){	
	   System.out.println("time to make a change");
	   closeDB();
	   InitDB();
	}
	
}
private void closeDB(){
	try {
		stmt.close();
		dbCon.close();
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	tryCount=0;
}
private void InitDB(){
	String path="Data//setup.txt";
	String usrID,usrCode;
	try {
		BufferedReader bfr=new BufferedReader(new FileReader(path));
		driver=bfr.readLine();
		url=bfr.readLine();
		usrID=bfr.readLine();
		usrCode=bfr.readLine();
		bfr.close();
		driver=driver.substring(12, driver.length());
		url=url.substring(9, url.length());
		usrID=usrID.substring(7);
		usrCode=usrCode.substring(9);
		System.out.println("driver:"+driver);
		System.out.println("url:"+url);
		//System.out.println(usrID+":"+usrCode);
		Class.forName(driver);
		dbCon=DriverManager.getConnection(url,usrID,usrCode);
		stmt=dbCon.createStatement();
		System.out.println("connect successful");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		System.out.println("DB Init Error");
		e.printStackTrace();
	}
}
public ServerDB() {
	
	InitDB();
}
public void finalize(){
	closeDB();
	
	try {
		super.finalize();
	} catch (Throwable e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	System.out.println("Closed a used DB");
}
public boolean Login(String req){
	String s[]=req.split(":");
	String user=s[0];
	String code=s[1];
	user="'"+user+"'";
	try {
		
		ResultSet rs=stmt.executeQuery("select UserID,UserCode from UserInfo where UserID=+"+user+";");
		if(rs.next()==false) {
			rs.close();
			return false;
		}
		if(code.equals(rs.getString("UserCode"))==false){
		    rs.close();
			return false;
		}
		rs.close();
		
		stmt.executeUpdate("update Status set isOnline="+true+" where UserID="+user+";");
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return true;
}
public void Logout(String user){
	user="'"+user+"'";
	try {
		//aCounter.doQueryCount();
		stmt.executeUpdate("update Status set isOnline="+false+" where UserID="+user+";");
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
public boolean isOnline(String user){
	boolean online=false;
	try {
		//aCounter.doQueryCount();
		ResultSet rs=stmt.executeQuery("select * from Status where UserID='"+user+"';");
		if(rs.next())
		online=rs.getBoolean("isOnline");
		rs.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	return online;
}
public synchronized void Writecache(String user,String s){
	
	try {
		//aCounter.doQueryCount();
		ResultSet rs= stmt.executeQuery("select userCache from Status where UserID='"+user+"';");
		if(rs.next()==false) return ;
		String sc=rs.getString("userCache");
		if(sc==null)sc="";
		sc=sc+s;
		//System.out.println("Cache in:"+sc);
		//aCounter.doQueryCount();
		stmt.executeUpdate("update Status set userCache='"+sc+"' where UserID='"+user+"';");
		rs.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		System.out.println("Cache E :W");
		e.printStackTrace();
	}
}
public synchronized String ReadCache(String user){
	
	String s="";
	try {
		//aCounter.doQueryCount();
		ResultSet rs=stmt.executeQuery("select userCache from Status where UserID='"+user+"';");
		if(rs.next())
		   s=rs.getString("userCache");
		rs.close();
		//aCounter.doQueryCount();
		stmt.executeUpdate("update Status set userCache='' where UserID='"+user+"';");
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		//System.out.println("Cache E :R");
		e.printStackTrace();
	}
	if(s==null||s.equals(""))
		s=null;
	return s;
}
public UserInfo getUser(String userID){
	UserInfo user=new UserInfo();
	try {
		//aCounter.doQueryCount();
		ResultSet rs=stmt.executeQuery("select * from UserInfo where UserID='"+userID+"';");
		if(rs.next()==false) return null;
		user.Sex=rs.getString("Sex");
		user.UserCode=rs.getString("UserCode");
		user.UserName=rs.getString("UserName");
		user.UserID=userID;
		rs.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		System.out.println("getInfo E");
		e.printStackTrace();
	}
	return user;
}
public synchronized Vector<String> getPersons(String user,String match){
	Vector<String> rslist=new Vector<String>();
	match="'%"+match+"%'";
	user="'"+user+"'";
	try {
		ResultSet rs=stmt.executeQuery("select UserName,UserID from UserInfo where UserName like "+match+" and UserName!="+user+";");
		while(rs.next()){
		String s=rs.getString("UserName")+":"+rs.getString("UserID");
		rslist.add(s);
		}
		return rslist;
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return null;
	}
	
}
public Vector<String> getFriends(String user){
	Vector<String> flist=new Vector<String>(),userfriends=new Vector<String>();
	try {
		//aCounter.doQueryCount();
		ResultSet rs=stmt.executeQuery("select * from Friends where userID='"+user+"';");
		rs.next();
		for(int i=0;i<UserInfo.nfriedns;i++){
			String fID=rs.getString("f"+(i+1));
			if(fID!=null&&!fID.equals("")){
				flist.add(fID);
			}
		}
		for(int i=0;i<flist.size();i++){
			//aCounter.doQueryCount();
			rs=stmt.executeQuery("select UserName from UserInfo where UserID='"+flist.get(i)+"';");
			rs.next();
			String id=flist.get(i),name=rs.getString("UserName");
			userfriends.add(name+":"+id+":"+"friend");
		}
		
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return userfriends;
}
public synchronized boolean removeUser(String user){
	if(getUser(user)==null) return false;
	try {
		//aCounter.doQueryCount();
		stmt.execute("delete from Status where UserID='"+user+"';");
		aCounter.doQueryCount();
		stmt.execute("delete from Friends where UserID='"+user+"';");
		aCounter.doQueryCount();
		stmt.execute("delete from UserInfo where UserID='"+user+"';");
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		System.out.println("removeInfo E");
		e.printStackTrace();
	}
	return true;
}

public synchronized boolean addUser(UserInfo user){
	if(getUser(user.UserID)!=null) return false;
	//aCounter.doQueryCount();
	String record="'"+user.UserID+"','"+user.UserName+"','"+user.UserCode+"','"+user.Sex+"'";
	try {
		//aCounter.doQueryCount();
		stmt.executeUpdate("insert into UserInfo VALUES("+record+");");
		//aCounter.doQueryCount();
		stmt.executeUpdate("insert into Status VALUES('"+user.UserID+"',"+true+",'');");
		String fs="'"+user.UserID+"',"+0;
		for(int i=0;i<UserInfo.nfriedns;i++){
			fs=fs+",''";
		}
		//aCounter.doQueryCount();
		stmt.executeUpdate("insert into Friends VALUES("+fs+");");
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		System.out.println("addInfo E");
		e.printStackTrace();
	}
	return true;
}
public  int numFriends(String user){
	int n=0;
	try {
		//aCounter.doQueryCount();
		ResultSet rs=stmt.executeQuery("select Total from Friends where UserID='"+user+"';");
		if(rs.next())
		n=rs.getInt("Total");
		rs.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		System.out.println("Friends:count");
		e.printStackTrace();
	}
	return n;
}
public boolean isFriend(String user, String f){
	boolean tag=false;
	try {
		//aCounter.doQueryCount();
		ResultSet rs=stmt.executeQuery("select * from Friends where UserID='"+user+"';");
		rs.next();
		for(int i=0;i<UserInfo.nfriedns;i++){
			String s=rs.getString("f"+(i+1));
			if(s!=null&&s.equals(f)){
				tag=true;
				break;
			}
		}
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	return tag;
}
public synchronized boolean deleteFriend(String user,String f){
	if(isFriend(user,f)==false) return false;
	try {
		//aCounter.doQueryCount();
		ResultSet rs=stmt.executeQuery("select * from Friends where UserID='"+user+"';");
		rs.next();
			for(int i=0;i<UserInfo.nfriedns;i++){
				String frd="f"+(i+1);
				if(rs.getString(frd)!=null&&rs.getString(frd).equals(f)){
					//aCounter.doQueryCount();
					stmt.executeUpdate("update Friends set "+frd+"='' where UserID='"+user+"';");
					break;
				}
			}
			rs.close();
		//aCounter.doQueryCount();
		rs=stmt.executeQuery("select * from Friends where UserID='"+f+"';");
		rs.next();
		for(int i=0;i<UserInfo.nfriedns;i++){
			String urd="f"+(i+1);
			if(rs.getString(urd)!=null&&rs.getString(urd).equals(user)){
				//aCounter.doQueryCount();
				stmt.executeUpdate("update Friends set "+urd+"='' where UserID='"+f+"';");
				break;
			}
		}
		rs.close();

	} catch (SQLException e) {
		// TODO Auto-generated catch block
		System.out.println("friends E:del");
		e.printStackTrace();
		
		return false;
	}
	
	return true;
}
public synchronized boolean addFriend(String user,String f){
	if(isFriend(user,f)) return false;
	try {
		//aCounter.doQueryCount();
		ResultSet rs=stmt.executeQuery("select * from Friends where UserID='"+user+"';");
		rs.next();
			for(int i=0;i<UserInfo.nfriedns;i++){
				String frd="f"+(i+1);
				if(rs.getString(frd)==null||rs.getString(frd).equals("")){
					//aCounter.doQueryCount();
					stmt.executeUpdate("update Friends set "+frd+"='"+f+"' where UserID='"+user+"';");
					break;
				}
			}
		rs.close();
		//aCounter.doQueryCount();
		rs=stmt.executeQuery("select *from Friends where UserID='"+f+"';");
		rs.next();
		for(int i=0;i<UserInfo.nfriedns;i++){
			String urd="f"+(i+1);
			if(rs.getString(urd)==null||rs.getString(urd).equals("")){
				//aCounter.doQueryCount();
				stmt.executeUpdate("update Friends set "+urd+"='"+user+"' where UserID='"+f+"';");
				break;
			}
		}
		rs.close();
			
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		System.out.println("friends E:add");
		e.printStackTrace();
		return false;
	}
	
	return true;
}

}
