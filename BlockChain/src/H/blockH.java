package H;

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

//*****************************************************/
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

//gson stuff
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.Random;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import com.google.gson.JsonSyntaxException;
//*****************************************************/

public class blockH {//start the process(s) and stuff
	public static void main(String args[]) {
		if(args.length != 1) {
			System.out.println("Sorry, please call StartProcess with an int argument please");
		}
		else {//run stuff
			try {
				int pNum = Integer.parseInt(args[0]);//get the int arguemnt
				
				BlockHandler pH= new BlockHandler(pNum);
				BlockMethod_utilities uT = pH.getBlockMethod_utilities();
				
				//Wait for last process to multicast, then we get to multicast the public key
				if(pNum==BlockMethod_utilities.maxProcesses-1) {
					uT.sendMcast(uT.publicKeySocket, pNum, uT.convertToJson(uT.encodedPublicKey()));
				}
				else if(pH.wait_For_Last_Process_Typed_In_From_arg()) {
					uT.sendMcast(uT.publicKeySocket, pNum, uT.convertToJson(uT.encodedPublicKey()));
				}
				else {
					System.out.println("Process requested to end");
				}
				if(pH.waitForKeys()) {
					for(int i = 0; i < BlockMethod_utilities.maxProcesses; i++) {//i tells me the process number
						System.out.println("Key for process number: "+i);
						System.out.println(Arrays.toString(uT.encodedPublicKey(pH.publicKey(i))));
					}
				}
			}
			catch(NumberFormatException e) {
				System.out.println("Sorry, arg must be numeric");
			}
		}
	}
}
class PublicKey_Receiver_class extends Thread {
    private BlockHandler pH;
    private BlockMethod_utilities uT;
    private int source_ProcessNumber;

    PublicKey_Receiver_class(BlockHandler p,int from_ProcessNumber) {
        this.pH=p;
        uT=p.getBlockMethod_utilities();
        source_ProcessNumber=from_ProcessNumber;
    }

    public void run() {
        System.out.println("Waiting to receive the PUBLIC KEY for process: "+source_ProcessNumber);//troubleshooting purposes
        String msg=uT.receiveMcast(uT.publicKeySocket, source_ProcessNumber);
        pH.receiveKey(source_ProcessNumber, uT.createPublicKey(uT.byteArrayFromJson(msg)));
        System.out.println("Recorded the PUBLIC KEY for process: "+source_ProcessNumber);//troubleshooting purposes
    }
}

class BlockHandler {
    private int processNumber;
    private PublicKey[] receivedPublicKey=new PublicKey[BlockMethod_utilities.maxProcesses];//the BIG public key for ALL THE PROCESSES
    private boolean[] publicKeyReceived=new boolean[BlockMethod_utilities.maxProcesses];//flag tells us key has been received
    private BlockMethod_utilities u;
    private boolean isRunning;//flag tells us process is running and active
    private PublicKey_Receiver_class[] receiverThread=new PublicKey_Receiver_class[BlockMethod_utilities.maxProcesses];

    BlockHandler(int processNUM) {
        processNumber=processNUM;
        System.out.println("***** Console for process "+processNumber+" *****");
        isRunning=true;
        u=new BlockMethod_utilities(512,processNumber);
        for(int pNum=0;pNum<BlockMethod_utilities.maxProcesses;pNum++) {
            if(pNum==processNumber) {
                receivedPublicKey[pNum]=u.publicKey();
                publicKeyReceived[pNum]=true;
            } else {
                publicKeyReceived[pNum]=false;
                receiverThread[pNum]=new PublicKey_Receiver_class(this, pNum);
                receiverThread[pNum].start();
            }
        }
    }

    public int processNUMBER() {
        return(processNumber);
    }

    public PublicKey publicKey(int processNum) {
        return(receivedPublicKey[processNum]);
    }

    public BlockMethod_utilities getBlockMethod_utilities() {
        return(u);
    }

    public synchronized boolean running() {
        return(isRunning);
    }

    public synchronized void shutDown() {
        isRunning=false;
        notify();
    }

    public synchronized void receiveKey(int pNum,PublicKey publicKey) {
        receivedPublicKey[pNum]=publicKey;
        publicKeyReceived[pNum]=true;
        System.out.println("Received PUBLIC KEY from process: "+pNum);//DEBUG
        notify();
    }

    public synchronized boolean allKeysReceived() {
        boolean allReceived=true;
        for(int pNum=0;pNum<BlockMethod_utilities.maxProcesses;pNum++) {
            if(!publicKeyReceived[pNum]) {
                allReceived=false;
                break;
            }
        }
        return(allReceived);
    }
  //-----------waiting methods-----------
    public synchronized boolean wait_For_Last_Process_Typed_In_From_arg() {//wait and check for notifications; returns true when received key from last process (usually Process 2 since we usually type in 2 last)
                                      				//returns isRunning as false
    												//isRunning set to false before received key
        System.out.println("Waiting for process "+(BlockMethod_utilities.maxProcesses-1));//DEBUG
        while(isRunning && !publicKeyReceived[BlockMethod_utilities.maxProcesses-1]) {
            try {
                wait();
            } catch(InterruptedException e) {
            	e.printStackTrace();//Exception of sorts in this this method. Caused by interuption
            }
        }
        System.out.println("Wait finished");//DEBUG
        return(isRunning);                                      
    }

    public synchronized boolean waitForKeys() {//wait and check for notifications; this method returns true when ALL keys are received;
                                  			//returns isRunning as false
    										//isRunning set to false before all keys are received
        System.out.println("Waiting for public keys");//DEBUG
        while(isRunning && !allKeysReceived()) {//while process(s) is running and not all keys have been received
            try {
                wait();
            } catch(InterruptedException e) {
            	e.printStackTrace();//Exception of sorts in this this method. Caused by interuption
            }
        }
        System.out.println("Wait finished");//DEBUG
        return(isRunning);
    }
}

class BlockMethod_utilities {
    public final static int maxProcesses=3;//public constant, keeping it at 3 because of the 3 text files BlockInput 0,1,2

    //for ciphering purposes
    private KeyPair keyPair;
    private Cipher cipher;
    private KeyFactory keyFactory;

    //for GSon and random generator purposes
    private Gson gson;
    private Random randomGenerator;

    //for communicating purposes
    private final String mcastAddrName="228.5.6.7";//Multicast group - must be D IP addr (224.0.0.1 to 239.255.255.255)
    private final String netIFName="bge0";//Network Interface - as seen in Java Oracle Docs
    public final int publicKeySocket=0;
    public final int unverifiedBlockSocket=1;
    public final int updatedBlockChainSocket=2;
    private final int[] defaultPort={4710,4820,4930};
    private MulticastSocket mcastSocket[][]=new MulticastSocket[3][maxProcesses];//Multicast sockets to send and to receive
    private InetSocketAddress mcastGroup[][]=new InetSocketAddress[3][maxProcesses];//Multicast groups to send and to receive multicasts

    BlockMethod_utilities(int keySize, int pNum) {//constructor with basic parameters for key size and process number
        //cipher objects init
        SecureRandom secureRandom=new SecureRandom();//create secure random generator
        try {
            KeyPairGenerator kpg=KeyPairGenerator.getInstance("RSA");//create keypair generator
            kpg.initialize(keySize,secureRandom);//init keypair generator with the secureRandom
            for(int i=0;i<=pNum;i++) {
                keyPair=kpg.generateKeyPair();
            }
        } catch(NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException on KeyPairGenerator.getInstance(RSA)");
        }
        try {
            cipher=Cipher.getInstance("RSA");//create cipher object to encrypt or decrypt
        } catch(NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException on Cipher.getInstance(RSA)");
        } catch(NoSuchPaddingException e) {
            System.out.println("NoSuchPaddingException on Cipher.getInstance(RSA)");
        }
        try {
            keyFactory=KeyFactory.getInstance("RSA");
        } catch(NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException on KeyFactory.getInstance(RSA)");
        }     
        
        gson=new GsonBuilder().setPrettyPrinting().create();//create our gson object

        //random generator init
        ByteBuffer bBuf=ByteBuffer.wrap(SecureRandom.getSeed(4));//get a 4 bytes seed from secureRandom
        randomGenerator=new Random(bBuf.getInt());               //init randomGenerator

        //Communication sockets init
        for(int socketType=publicKeySocket;socketType<=updatedBlockChainSocket;socketType++) {
            for(int processNum=0;processNum<maxProcesses;processNum++) {
                int port=defaultPort[socketType]+processNum;
                try {
                    mcastSocket[socketType][processNum]=new MulticastSocket(port);
                } catch(IOException e) {
                    System.out.println("IOException creating MulticastSocket "+port);
                }
                try {
                    mcastGroup[socketType][processNum]=new InetSocketAddress(InetAddress.getByName(mcastAddrName), port);
                } catch(UnknownHostException e) {
                    System.out.println("UnknownHostException creating InetSocketAddress "+port);
                }
                try {
                    NetworkInterface netIF=NetworkInterface.getByName(netIFName);
                    try {
                        mcastSocket[socketType][processNum].joinGroup(mcastGroup[socketType][processNum], netIF);
                    } catch(IOException e) {
                        System.out.println("IOException in joinGroup "+port);
                    }
                } catch(SocketException e) {
                    System.out.println("SocketException creating NetworkInterface "+port);
                }
            }
        }
    }

    //cipher utilities
    public PublicKey publicKey() {//retrieves binary form of public key for encrypting or decrypting
        return(keyPair.getPublic());
    }

    public byte[] encodedPublicKey() {//retrieves byte array form of public key
        return(keyPair.getPublic().getEncoded());
    }

    public byte[] encodedPublicKey(PublicKey publicKey) {
        return(publicKey.getEncoded());
    }

    public PublicKey createPublicKey(byte[] encodedKey) {
        X509EncodedKeySpec newKeySpec=new X509EncodedKeySpec(encodedKey);
        try {
            return(keyFactory.generatePublic(newKeySpec));
        } catch(InvalidKeySpecException e) {
            System.out.println("InvalidKeySpecException on KeyFactory.generatePublic");
        }
        return(null);//if try block fails, we returns null
    }

    public PrivateKey privateKey() {//retrieves byte array form of private key
        return(keyPair.getPrivate());
    }

    public byte[] encrypt(String msg,Key encryptKey) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, encryptKey);
        } catch(InvalidKeyException e) {
            System.out.println("InvalidKeyException on cipher.init for encrypt");
        }
        try {
            return(cipher.doFinal(msg.getBytes()));         
        } catch(IllegalBlockSizeException e) {
            System.out.println("IllegalBlockSizeException on cipher.doFinal for encrypt");
        } catch(BadPaddingException e) {
            System.out.println("BadPaddingException on cipher.doFinal for encrypt");
        }
        return("".getBytes());//default return if return in try block doesn't succeed
    }

    public String decrypt(byte[] msg, Key decryptKey) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, decryptKey);
        } catch(InvalidKeyException e) {
            System.out.println("InvalidKeyException on cipher.init for decrypt");
        }
        try {
            return(new String(cipher.doFinal(msg)));         
        } catch(IllegalBlockSizeException e) {
            System.out.println("IllegalBlockSizeException on cipher.doFinal for decrypt");
        } catch(BadPaddingException e) {
            System.out.println("BadPaddingException on cipher.doFinal for decrypt");
        }
        return("");//default return if return in try block doesn't succeed
    }

    public String convertToJson(Object o) {
        return(gson.toJson(o));
    }

    public byte[] byteArrayFromJson(String s) {
        try {
            return(gson.fromJson(s.trim(),byte[].class));
        } catch(JsonSyntaxException e) {
            System.out.println("JsonSyntaxException on fromJson");
        }
        return(null);
    }

    public long randomInt(int fromInt, int ToInt) {
        return(fromInt+Math.round(randomGenerator.nextDouble()*(ToInt-fromInt)));
    }

    void sendMcast(int socketType,int processNum, String msg) {
        byte[] msgBytes=msg.getBytes();
        DatagramPacket dgPacket=new DatagramPacket(msgBytes, msgBytes.length, mcastGroup[socketType][processNum]);
        try {
            mcastSocket[socketType][processNum].send(dgPacket);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    String receiveMcast(int socketType,int processNum) {
        byte[] buf=new byte[1000];
        DatagramPacket dgPacket=new DatagramPacket(buf, buf.length);
        try {
            mcastSocket[socketType][processNum].receive(dgPacket);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return(new String(buf));
    }
}