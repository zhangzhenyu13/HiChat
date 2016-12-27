package UI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class cmdConnect {
	static final int timeout=15000,trafficJam=5000;
	static int servicePort,searchPort;
	static String hostStr;
	static String userLogin,userLogout,sysLogin,sysLogout,userReg,sysReg;
	static int badPort;
	static String badReq,goodReq;
	static String searchFriend,addFriend;
	 public cmdConnect(){
	    	BufferedReader bfr;
			try {
				bfr = new BufferedReader(new FileReader("Data//setup.txt"));

				String s="";
				s=bfr.readLine();
				hostStr=s.substring(13,s.length());
				s=bfr.readLine();
				s=s.substring(13, s.length());
				servicePort=Integer.parseInt(s);
				s=bfr.readLine();
				s=s.substring(12);
				searchPort=Integer.parseInt(s);
				
				userLogin=bfr.readLine();
				sysLogin=bfr.readLine();
				userLogout=bfr.readLine();
				sysLogout=bfr.readLine();
				userReg=bfr.readLine();
				sysReg=bfr.readLine();
				s=bfr.readLine();
				badPort=Integer.parseInt(s.substring(8));
				badReq=bfr.readLine().substring(11);
				goodReq=bfr.readLine().substring(12);
				searchFriend=bfr.readLine();
				addFriend=bfr.readLine();
				bfr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	    }
}
