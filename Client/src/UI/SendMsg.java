/*
 * send msg
 * respond the action of chatUI
 */
package UI;
import java.io.*;
import java.net.*;
import java.util.*;
public class SendMsg{
	Socket connect;
	PrintStream os;
	SendMsg(Socket socket){
		connect=socket;
		try {
			os=new PrintStream(connect.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void sendmsg(String s){
		os.println(s);
		os.flush();
    }
	public void Close(){
		os.close();
	}
}
