package chatServer;

public class ChatSever {
public static void main(String[] args){
	ServerDB sdb=new ServerDB();
	ServerNet snet=new ServerNet(sdb);
	snet.Watch();
	//sdb.addUser(new UserInfo("2013010703","Boby","123","male"));	
	//sdb.removeUser("2013010703");
}
}
