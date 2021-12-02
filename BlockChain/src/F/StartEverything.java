package F;

//using https://github.com/hhohho/Learning-Blockchain-in-Java/blob/master/Learning-Blockchain-in-Java-Chapter2-src.rar as reference

//a lot of the code is heavily based on Block java file
//download and extract the RAR file to see associated files

//also using https://www.youtube.com/watch?v=baJYhYsHkLM&t=304s as reference
//a lot of it is also heavily based on Block and Main java files used

/*
* Other web sources:

https://mkyong.com/java/how-to-parse-json-with-gson/
http://www.java2s.com/Code/Java/Security/SignatureSignAndVerify.htm
https://www.mkyong.com/java/java-digital-signatures-example/ (not so clear)
https://javadigest.wordpress.com/2012/08/26/rsa-encryption-example/
https://www.programcreek.com/java-api-examples/index.php?api=java.security.SecureRandom
https://www.mkyong.com/java/java-sha-hashing-example/
https://stackoverflow.com/questions/19818550/java-retrieve-the-actual-value-of-the-public-key-from-the-keypair-object
https://www.java67.com/2014/10/how-to-pad-numbers-with-leading-zeroes-in-Java-example.html
*
* */

import java.io.*;
import java.net.*;

public class StartEverything {
	public static void main(String args[]) {
		if(args.length != 1) {
			System.out.println("Sorry, please call StartProcess with an int argument please");
		}
		else {//run stuff
			try {
				int pNum = Integer.parseInt(args[0]);//get the int arguemnt
				ProcessMaker sock = new ProcessMaker(pNum);
				sock.start();
			}
			catch(NumberFormatException e) {
				System.out.println("Sorry, arg must be numeric");
			}
		}
	}
}
class ProcessMaker extends Thread {//can run on different shells, will accept multicast messages from common machine socket
	//Extends Thread to get needed methods to manipulate threads
	//will work as a worker class of sorts
	
	//"Hello from Process [A]
	//indepdent code that demonstrate 3 processes
	private int processNum;//process number
    private String mcastAddrName="228.5.5.5"; //multicast groups are class D IP addrs (224.0.0.1 to 239.255.255.255) according to https://www.sciencedirect.com/topics/computer-science/multicasting
    	//for pushing dat to multiple places at once
    private String networkInterface_name="bge0";//arbitrary ish name to ID the interface to better use mCast Group
    private int port=6789;//arbitrary port num
    private InetSocketAddress group;
    private MulticastSocket sock;   //socket for Multicast UDP datagram communication
    
    //constructor
    ProcessMaker (int processNum){//constructor that needs to take in processNum as arg
    	this.processNum = processNum;
    	try {
    		sock = new MulticastSocket(port);
    	} catch(IOException e) { System.out.println("IOException in new new MulticastSocket"); }
    	try {
    		group = new InetSocketAddress(InetAddress.getByName(mcastAddrName), port);
    	} catch(UnknownHostException e) { System.out.println("UnknownHostException in InetAddress.getByName"); }
    	
    	try {
    		NetworkInterface NT = NetworkInterface.getByName(networkInterface_name);
    		try {
    			sock.joinGroup(group, NT);
    		} catch(IOException e) {
    			System.out.println("IOException in joinGroup");
    		}
    	} catch(SocketException e) {
    		System.out.println("SocketException in NetworkInterface.getByName");
    	}
    }
    public void sendMulticast(String message) {
    	//turn string into datagram packet to make things more streamlined
    	//bytes will make things go faster
    	byte[] messageInBytes = message.getBytes();
    	DatagramPacket dgpckt = new DatagramPacket(messageInBytes, messageInBytes.length, group);
    	try {
    		sock.send(dgpckt);
    	} catch(IOException e) {
    		System.out.println("IOException in send(dgpckt)");
    	}
    }
    public String receiveMulticast() {
    	byte[] buffer = new byte[1000];
    	DatagramPacket dgpckt = new DatagramPacket(buffer, buffer.length);
    	try {
    		sock.receive(dgpckt);
    	} catch(IOException e) {
    		System.out.println("IOException in receive(dgpckt)");
    	}
    	return new String(buffer);
    }
    public void run() {
    	System.out.println("Hello from process "+processNum);//indicate connection was indeed made
    	
    	//send or receive multicast connection
    		for(int i = 0; i < 3; i++) {
    			BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        		System.out.println("Press enter to start multicasting");
        		System.out.flush();//get rid of the rest, flush it down a toilet
        		
        		try{ in.readLine(); } 
        		catch(IOException e) { System.out.println("IOException in readLine()"); }
        		
        		//Send the multicast stuff to other process
        		sendMulticast("Hello Process "+processNum);
        		System.out.println("Multicast sent");
        		System.out.println("Received multicast: "+receiveMulticast());
    		}
    	
    	//we're done so we exit
    	sock.close();
    	System.out.println("Bye from Process "+processNum);
    	
    }
}
