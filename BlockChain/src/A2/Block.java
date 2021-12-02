package A2;
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

import java.util.*;
import java.io.*;
import java.security.*;
//import java.sql.*;
public class Block{
    //private static final long makeSerialVID = 1L;
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

    public Block(String prevHashID, String[] transactions, int blockNumber, String firstName, String lastName, String dob, String Rx, String ssNumber, String illness, String treatment, int proof_of_work, UUID uuid, String timeStamp){
         this.proof_of_work = proof_of_work;

         this.prevHashID = prevHashID;
         this.transactions = transactions;
         this.blockNumber = blockNumber;//to identify which block it is
         this.nonce_randomSeed = nonce_randomSeed;
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
    public boolean checkSitDummySolver(){//The mechanism to check the puzzle
        this.hashID = calcHashID();
        while(!checkSigDummyMethod(this.hashID)){//if puzzle conditions not satisfied, keep going
            this.nonce_randomSeed++;
            this.hashID = calcHashID();
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
    public String calcHashID(){
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

        // for(String tr : transactions){
        //     builder.append(tr);
        // }

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
    // public long getTimeStamp() {
	//     return (java.util.Calendar.getInstance().getTimeInMillis());
	// }
    //ArrayList<Block> blockchain = new ArrayList<>();
    
    // public static void run(){
    // }
    public static void main(String[] args){
                
        int blockNum = 0;

        int proofOFWORK = 1;

        Date date = new Date();

        StringBuilder builder = new StringBuilder();

        String tS = String.format("%1$s %2$tF.%2$tT", "", date);

        UUID dummyB_UUID = UUID.randomUUID();
        UUID B1_UUID = UUID.randomUUID();
        UUID B2_UUID = UUID.randomUUID();
        UUID B3_UUID = UUID.randomUUID();
        //dummy block
        String[] dummy_transactions = { "A"};//to throw away later
        Block dummyBlock =new Block("0", dummy_transactions, blockNum, "Satoshi", "Kon", "1963.12.08", "Xanax", "123-45-6789", "flu", "bedrest", proofOFWORK, dummyB_UUID, tS);
        
        blockNum = blockNum+1;//for block1
        String[] block1Trans = {"B"};//to throw away later
        Block block1 = new Block(dummyBlock.getHashID(), block1Trans, blockNum, "Maya", "Rudolph", "1999.12.12", "Steroids", "098-76-5432", "Virus", "Exercise", proofOFWORK, B1_UUID, tS);

        blockNum = blockNum+1;//for block2
        String[] block2Trans = {"C"};//to throw away later
        Block block2 = new Block(block1.getHashID(), block2Trans, blockNum, "JJ", "Morimoto", "1986.03.01","Tilenol", "832-54-1849", "Headache", "Hydration", proofOFWORK, B2_UUID, tS);

        blockNum = blockNum+1;//for block3
        String[] block3Trans = {"D"};//to Throw away later
        Block block3 = new Block(block2.getHashID(), block3Trans, blockNum, "Mitchel", "Russo", "2000.01.11", "Menthol", "493-29-9998", "Bad Breath", "BrushTeeth", proofOFWORK, B3_UUID, tS);

        System.out.println("Hash of block 0: "+dummyBlock.getHashID());//debug
        System.out.println("PrevHash of block 0: "+dummyBlock.getPrevHashID());//debug
        System.out.println("BLocknumber for block0: "+dummyBlock.getBlockNumber());//debug
        System.out.println("First name for block0: "+dummyBlock.getFirstName());//debug
        System.out.println("Last name for block0: "+dummyBlock.getLastName());//debug
        System.out.println("DOB for block0: "+dummyBlock.getDOB());//debug
        System.out.println("Rx for block0: "+dummyBlock.getRx());//debug
        System.out.println("SSN: "+dummyBlock.getSSNum());//debug
        System.out.println("Illness: "+dummyBlock.getIllness());//debug
        System.out.println("Treatment: "+dummyBlock.getTreatment());//debug
        System.out.println("Is block0 verified?: "+dummyBlock.verifyBlockSignature()+"\n");//debug

        System.out.println("Hash of block 1: "+block1.getHashID());//debug
        System.out.println("PrevHash of block 1: "+block1.getPrevHashID());//debug
        System.out.println("BLocknumber for block1: "+block1.getBlockNumber());//debug
        System.out.println("First name for block1: "+block1.getFirstName());//debug
        System.out.println("Last name for block1: "+block1.getLastName());//debug
        System.out.println("DOB for block1: "+block1.getDOB());//debug
        System.out.println("Rx for block1: "+block1.getRx());//debug
        System.out.println("SSN: "+block1.getSSNum());//debug
        System.out.println("Illness: "+block1.getIllness());//debug
        System.out.println("Treatment: "+block1.getTreatment());//debug
        System.out.println("Is block1 verified?: "+block1.verifyBlockSignature()+"\n");//debug

        System.out.println("Hash of block 2: "+block2.getHashID());//debug
        System.out.println("PrevHash of block 2: "+block2.getPrevHashID());//debug
        System.out.println("BLocknumber for block2: "+block2.getBlockNumber());//debug
        System.out.println("First name for block2: "+block2.getFirstName());//debug
        System.out.println("Is block2 verified?: "+block2.verifyBlockSignature()+"\n");//debug

        System.out.println("Hash of block 3: "+block3.getHashID());//debug
        System.out.println("PrevHash of block 3: "+block3.getPrevHashID());//debug
        System.out.println("BLocknumber for block3: "+block3.getBlockNumber());//debug
        System.out.println("First name for block3: "+block3.getFirstName());//debug
        System.out.println("Is block3 verified?: "+block3.verifyBlockSignature()+"\n");//debug

    }
}