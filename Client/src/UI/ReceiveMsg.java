/*receive msg
 * runs in backgroud
 */
package UI;
import java.net.*;
import java.awt.Component;
import java.io.*;
import java.util.*;

import javax.swing.JTextArea;
public class ReceiveMsg extends Thread{
Socket connect;
msgDisplay display;
boolean running=true;
public ReceiveMsg(Socket socket,msgDisplay display){
	connect=socket;
	this.display=display;
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
	info=header.split(":");
	header="from "+info[0];
	return header+data;
}
public void run(){
	
		BufferedReader is=null;
		String line="";
		try {
			 is=new BufferedReader(new InputStreamReader(connect.getInputStream()));
			 
			while(running){
			  line=is.readLine();
			  //System.out.println("receiver running");
			  if(line!=null){
			      line=parseMsg(line);
				  display.setMsgDisplay(line);
			  }
			}
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
}
public void Close(){
	running=false;
	
}
}
