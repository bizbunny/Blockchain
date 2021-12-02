package K;

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
//import statements list
import java.util.*;
import java.net.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;

//dealing with exceptions
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

//gson stuff
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
//*****************************************************/

public class BlockK {//start the process(s) and stuff

	public static void main(String[] args) {
		
		SimpleDateFormat dateFormat=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");//to format our timeStamps
        
		if(args.length!=1) {//make sure we do have a an argument being passed in when running BlockK
            System.out.println("Sorry, please call StartProcess with int processNum as argument");
        } else {
            try {
            	int pNum = Integer.parseInt(args[0]);//pNum is process number being passed in
            	
            	BlockHandler pH=new BlockHandler(pNum);
            	BlockMethod_utilities u=pH.getBlockChainUtility();
                String fileName="";  // file name for data import
                int blocks=0;//blocks to keep track of
                String prevHashId="";
                //retrieving from Files---------------------------------------------------------------------------------------------------------------------------------------
                switch(pNum) {//depending on the process number we put in as argument, 0, 1, or 2, this will tell the program which text file to retrieve. 
                case 0:
                    fileName="BlockInput0.txt";
                    break;
                case 1:
                    fileName="BlockInput1.txt";
                    break;
                case 2:
                    fileName="BlockInput2.txt";
                    break;
                }
                //retrieving from Files---------------------------------------------------------------------------------------------------------------------------------------
                try {
                	File input_main_file = new File(fileName);//take in the file
                	FileReader fileReader = new FileReader(input_main_file);//read in the file  
                    BufferedReader buffread = new BufferedReader(fileReader);            
                    String line_read;//stores the specific line we read from file
                    
                    try {
                    	while((line_read = buffread.readLine()) != null) {//use loop to go through each line of the file and split them into arrays
                    		String[] data=line_read.split("\\s+");
                            int blockNo=pH.nextBlock();
                            pH.setBlockNum(blockNo, new BlockMaker("Block "+pNum+"-"+blocks, prevHashId, data, blocks,
                                data[0], data[1], data[2], data[5], data[3], data[4], data[6], 1,
                                UUID.randomUUID(), dateFormat.format(new Date())));
                            prevHashId=pH.getBlockNum(blockNo).getHashID();
                            System.out.println("Added block "+blockNo+": "+pH.getBlockNum(blockNo).getBlockID()+" "+
                            pH.getBlockNum(blockNo).getFirstName()+" "+pH.getBlockNum(blockNo).getLastName());
                            blocks++;
                    	}
                    }
                    catch(IOException e) {
                    	e.printStackTrace();
                    }
                    fileReader.close();//close fileReader when done
                }
                catch(IOException e) {
                	e.printStackTrace();
                }
                
                //where waiting happens. We wait until Process 2 gets online. Multicasting then happens to public keys
                boolean proceed_forward=(pNum==BlockMethod_utilities.maxProcesses-1);//flag to tell me whether I should wait() or continue forward
                if(!proceed_forward) {
                    proceed_forward=pH.wait_For_Last_Process_Typed_In_From_arg();
                }
                if(proceed_forward) {
                    u.sendMcast(u.publicKeySocket, pNum, u.convertToJson(u.encodedPublicKey()));
                    for(int i=0;i<blocks;i++) {//i will tell me block number
                        u.sendMcast(u.unverifiedBlockSocket, pNum, u.convertToJson(pH.getBlockNum(i)));
                    }
                }
            }
            catch(NumberFormatException e) {
                e.printStackTrace();
            }
        }//else
	}//main
	
}//BlockK

class BlockMaker {
    private String[] transactions;
    private String prevHashID;
    private String firstName;
    private String lastName;
    private String dob;
    private String Rx;
    private String ssNumber;
    private String treatment;
    private String illness;

    private int nonce_randomSeed;
    private int proof_of_work;
    private int blockNumber = 1;
    private String hashID;
    private UUID uuid;
    private String BlockID;
    private String timeStamp;

    private boolean verified;

    public BlockMaker(String BlockID, String prevHashID, String[] transactions, int blockNumber, String firstName, String lastName,
        String dob, String Rx, String ssNumber, String illness, String treatment, int proofOfWORK, UUID uuid, String timeStamp) {
        this.proof_of_work = proofOfWORK;

        this.prevHashID = prevHashID;
        this.transactions = transactions;
        this.blockNumber = blockNumber;//to identify which block it is
        this.nonce_randomSeed = 0;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.Rx = Rx;
        this.ssNumber = ssNumber;
        this.treatment = treatment;
        this.illness = illness;
        this.uuid = uuid;
        this.BlockID = BlockID;
        this.timeStamp = timeStamp;

        this.hashID = calcHashID();
    }
    //VERIFY------------------
    public boolean verifyBlockSignature() {//The umbrella method that returns the puzzle answer
        return(this.hashID.equals(calcHashID()));
    }
    public boolean checkSigDummySolver(){//The mechanism to check the puzzle
        this.hashID = calcHashID();
        while(!checkSigDummyMethod(this.hashID)){//if puzzle conditions not satisfied, keep going
            this.nonce_randomSeed++;
            this.hashID = calcHashID();
            System.out.println("Block "+blockNumber+": "+hashID);
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                System.out.println("InterruptedException on sleep in checkSigDummySolver");
            }
        }
        return true;
    }
    public boolean checkSigDummyMethod(String hash){//The condition checker to see if puzzle meets condition
        char[] checker = hash.toCharArray();
        for(int i = 0; i < this.proof_of_work; i++){
            if(checker[i] != '0'){
                return false;
            }
        }
        System.out.println("verified block!");//debug
        return true;
    }
    
    //VERIFY END------------------
    public String getFirstName(){
        return firstName;
    }
    public String getLastName(){
        return lastName;
    }
    public String getDOB(){
        return dob;
    }
    public String getRx(){
        return Rx;
    }
    public String getSSNum(){
        return ssNumber;
    }
    public String getTreatment(){
        return treatment;
    }
    public String getIllness(){
        return illness;
    }
    public int getNonce_RS(){
        return this.nonce_randomSeed;
    }
    public int setNonce_RS(int nc_rs){
        return this.nonce_randomSeed = nc_rs;
    }
    public String getPrevHashID(){
        return prevHashID;
    }
    public String[] getTransactions(){
        return transactions;
    }
    public String getHashID(){
        return hashID;
    }
    public String getuuid(){
        return this.uuid.toString();
    }
    //SHA-256 digest of the block to get hash--------
    public String calcHashID(){//generate a hash id using all the block's given properties
        StringBuilder builder = new StringBuilder();
        builder.append(this.prevHashID);
        builder.append(this.nonce_randomSeed);
        builder.append(this.transactions);
        builder.append(this.hashID);
        builder.append(this.blockNumber);
        builder.append(this.firstName);
        builder.append(this.lastName);
        builder.append(this.dob);
        builder.append(this.Rx);
        builder.append(this.ssNumber);
        builder.append(this.illness);
        builder.append(this.treatment);
        builder.append(this.uuid);
        builder.append(this.BlockID);
        builder.append(this.timeStamp);

        byte byteData[] = mDigest_SHA256_to_toBytes(builder.toString());

        //convert to hex to make the bytes a bit more readable
        StringBuffer buff = new StringBuffer();
        for(int i = 0; i < byteData.length; i++){
            buff.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        String SHA256_String = buff.toString();
        return SHA256_String;
    }
    
    public static String mDigest_SHA256_to_toString(String msg){
        return Base64.getEncoder().encodeToString(mDigest_SHA256_to_toBytes(msg));
    }
    public static byte[] mDigest_SHA256_to_toBytes(String msg){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(msg.getBytes());//update() only accepts byte arrays
            return md.digest();
        }
        catch(java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
     //SHA-256 digest of the block to get hash END--------

    public int getBlockNumber(){
        return blockNumber;
    }
    public void setBlockNumber(int block_num){
        this.blockNumber = block_num;
    }
    public String getBlockID(){
        return this.BlockID;
    }
    public void setBlockVerified(boolean verified) {
        this.verified=verified;
    }
    public boolean getBlockVerified() {
        return(verified);
    }
}

class PublicKey_Receiver_class extends Thread {//receiver class for public keys
    private BlockHandler pH;
    private BlockMethod_utilities uT;
    private int sourceProcessNum;

    PublicKey_Receiver_class(BlockHandler pH,int fromProcessNum) {
        this.pH=pH;
        uT=pH.getBlockChainUtility();
        sourceProcessNum=fromProcessNum;
    }

    public void run() {
        System.out.println("Waiting to receive public key for process "+sourceProcessNum);//DEBUG
        String msg=uT.receiveMcast(uT.publicKeySocket, sourceProcessNum);
        pH.receiveKey(sourceProcessNum, uT.createPublicKey(uT.byteArrayFromJson(msg)));
        System.out.println("Recorded public key for process "+sourceProcessNum); //DEBUG
    }
}

class Unverified_BlockReceiver_class extends Thread {//handles the unverified blocks
    private BlockHandler pH;
    private BlockMethod_utilities uT;
    private int sourceProcessNum;

    Unverified_BlockReceiver_class(BlockHandler pH,int fromProcessNum) {
        this.pH=pH;
        uT=pH.getBlockChainUtility();
        sourceProcessNum=fromProcessNum;
    }

    public void run() {
    	
        System.out.println("Waiting to receive unverified blocks for process "+sourceProcessNum); //DEBUG
        
        while(true) {
            String msg=uT.receiveMcast(uT.unverifiedBlockSocket, sourceProcessNum);
            int blockNum=pH.nextBlock();
            pH.setBlockNum(blockNum,uT.blockFromJson(msg)); //decode JSON message
            										//That way, we can get the unverified block
            System.out.println("Received block "+blockNum+" from process "+sourceProcessNum+": "+
                pH.getBlockNum(blockNum).getBlockID()+" "+pH.getBlockNum(blockNum).getFirstName()+" "+pH.getBlockNum(blockNum).getLastName());
        }
    }
}

class BlockHandler {
    private int processNumber;
    private BlockMaker[] block=new BlockMaker[36];//multiple of 12 to deal with the 3 same input files
    private int nextBlockNum;

    private PublicKey[] receivedPublicKey=new PublicKey[BlockMethod_utilities.maxProcesses];//public key goes to ALL processes here
    private boolean[] publicKeyReceived=new boolean[BlockMethod_utilities.maxProcesses];//flag tells us key was received
    private BlockMethod_utilities u;
    private boolean isRunning;
    private PublicKey_Receiver_class[] receiverThread=new PublicKey_Receiver_class[BlockMethod_utilities.maxProcesses];
    private Unverified_BlockReceiver_class[] unverifiedBlockThread=new Unverified_BlockReceiver_class[BlockMethod_utilities.maxProcesses];

    BlockHandler(int processNum) {
        processNumber=processNum;
        nextBlockNum=0;
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
                unverifiedBlockThread[pNum]=new Unverified_BlockReceiver_class(this, pNum);
                unverifiedBlockThread[pNum].start();
            }
        }
    }

    public synchronized int processNum() {
        return(processNumber);
    }

    public synchronized PublicKey publicKey(int processNum) {
        return(receivedPublicKey[processNum]);
    }

    public synchronized BlockMethod_utilities getBlockChainUtility() {
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
        System.out.println("Received public key from process "+pNum); //DEBUG
        notify();
    }

    public synchronized boolean allKeysReceived() {
        boolean allReceived=true;
        for(int i=0;i<BlockMethod_utilities.maxProcesses;i++) {//i will tell be process number
            if(!publicKeyReceived[i]) {
                allReceived=false;
                break;
            }
        }
        return(allReceived);
    }
//-----------waiting methods-----------
    public synchronized boolean wait_For_Last_Process_Typed_In_From_arg() {//wait and check notifications; returns true when received key from last process (usually Process 2 since we usually type in 2 last)
                                      				//returns isRunning as false
    												//isRunning set to false before received key
        System.out.println("Waiting for process "+(BlockMethod_utilities.maxProcesses-1));//DEBUG
        while(isRunning && !publicKeyReceived[BlockMethod_utilities.maxProcesses-1]) {
            try {
                wait();
            } catch(InterruptedException e) {
                System.out.println("InterruptedException in waitForLastProcess");
            }
        }
        System.out.println("Wait finished");//DEBUG
        return(isRunning);                                      
    }

    public synchronized boolean waitForKeys() {//wait and check for notifications; this method returns true when ALL keys are received;
                                  			//returns isRunning as false
    										//isRunning set to false before all keys are received
        System.out.println("Waiting for public keys"); //DEBUG
        while(isRunning && !allKeysReceived()) {//while process(s) is running and not all keys have been received
            try {
                wait();
            } 
            catch(InterruptedException e) {
            	e.printStackTrace();//Exception of sorts in this this method. Caused by interuption
            }
        }
        System.out.println("Wait finished"); //DEBUG
        return(isRunning);
    }

    public synchronized void setBlockNum(int blockNum,BlockMaker newBlock) {
        block[blockNum]=newBlock;
        notify();
    }

    public synchronized BlockMaker getBlockNum(int blockNum) {
        return(block[blockNum]);
    }

    public synchronized void notifyBlockChange(int blockNum) {
        notify();
    }

    public synchronized int nextBlock() {
        int currentBlockToLookAt=nextBlockNum;
        nextBlockNum++;
        notify();
        return(currentBlockToLookAt);
    }
}

class BlockMethod_utilities {//go from java to json in  this class
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
    
    //in the following: 
    	//1st dimension: for the socketType 
    		//(publicKeySocket, unverifiedBlockSocket and updatedBlockChainSocket)
    // in 2nd dimension: the originating process
    private MulticastSocket mcastSocket[][]=new MulticastSocket[3][maxProcesses];//Multicast sockets to send and to receive
    private InetSocketAddress mcastGroup[][]=new InetSocketAddress[3][maxProcesses];//Multicast groups to send and to receive multicasts

    BlockMethod_utilities(int keySize, int pNum) {//constructor with basic parameters for key size and process number
    	//cipher objects init
        SecureRandom secureRandom=new SecureRandom();//create secure random generator
        try {
            KeyPairGenerator keyPG=KeyPairGenerator.getInstance("RSA");//create keypair generator
            keyPG.initialize(keySize,secureRandom);//init keypair generator with the secureRandom
            for(int i=0;i<=pNum;i++) {
                keyPair=keyPG.generateKeyPair();
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
        
        //gson object init
        gson=new GsonBuilder().setPrettyPrinting().create();//give it a better look

        //random generator init
        ByteBuffer bBuf=ByteBuffer.wrap(SecureRandom.getSeed(4));//get a 4 bytes seed from secureRandom
        randomGenerator=new Random(bBuf.getInt());               //initialize randomGenerator

        //Communication sockets init
        for(int socketType=publicKeySocket;socketType<=updatedBlockChainSocket;socketType++) {
            for(int processNum=0;processNum<maxProcesses;processNum++) {
                int port=defaultPort[socketType]+processNum;
                try {
                    mcastSocket[socketType][processNum]=new MulticastSocket(port);
                } 
                catch(IOException e) {
                    System.out.println("IOException creating MulticastSocket "+port);
                }
                try {
                    mcastGroup[socketType][processNum]=new InetSocketAddress(InetAddress.getByName(mcastAddrName), port);
                } 
                catch(UnknownHostException e) {
                    System.out.println("UnknownHostException creating InetSocketAddress "+port);
                }
                try {
                    NetworkInterface netIF=NetworkInterface.getByName(netIFName);
                    try {
                        mcastSocket[socketType][processNum].joinGroup(mcastGroup[socketType][processNum], netIF);
                    } 
                    catch(IOException e) {
                        System.out.println("IOException in joinGroup "+port);
                    }
                } 
                catch(SocketException e) {
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

    public byte[] encodedPublicKey(PublicKey publicKey) {//AUX method with parameter instead to deal with keys entering encryption 
        return(publicKey.getEncoded());
    }

    public PublicKey createPublicKey(byte[] encodedKey) {//makes a standard public key
        X509EncodedKeySpec newKeySpec=new X509EncodedKeySpec(encodedKey);
        try {
            return(keyFactory.generatePublic(newKeySpec));
        } catch(InvalidKeySpecException e) {
            System.out.println("InvalidKeySpecException on KeyFactory.generatePublic");
        }
        return(null);//if try block doesn't succeed, returns null
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
            return(cipher.doFinal(msg.getBytes()));//finishes up encryption       
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

    public BlockMaker blockFromJson(String s) {
        try {
            return(gson.fromJson(s.trim(),BlockMaker.class));
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
            System.out.println("IOException sending multicast message "+socketType+"/"+processNum+":\n"+msg);
        }
    }

    String receiveMcast(int socketType,int processNo) {
        byte[] buf=new byte[1000];
        DatagramPacket dgPacket=new DatagramPacket(buf, buf.length);
        try {
            mcastSocket[socketType][processNo].receive(dgPacket);
        } catch(IOException e) {
            System.out.println("IOException receiving multicast message"+socketType+"/"+processNo);
        }
        return(new String(buf));
    }
}