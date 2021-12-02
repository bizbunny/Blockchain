package B;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

//for the mini projects
public class Block {
  
  public static void main(String[] args) {

      StringBuilder builder = new StringBuilder();
      int blockNum = 0;
      Date date = new Date();
      String tS = String.format("%1$s %2$tF.%2$tT", "", date);
      UUID dummyB_UUID = UUID.randomUUID();
      UUID B1_UUID = UUID.randomUUID();
      UUID B2_UUID = UUID.randomUUID();
      UUID B3_UUID = UUID.randomUUID();

      int proofOFWORK = 1;
      // dummyblock
      String[] dummy_transactions = { "AA" };
 
      BlockMaker dummyBlock = new BlockMaker("0", dummy_transactions, blockNum, tS, proofOFWORK, "Satoshi", "Kon", "1963.12.08",
              "flu", "bedrest", "Xanax", "123-45-6789", dummyB_UUID.toString(), dummyB_UUID);
      dummyBlock.puzzleSolver();
      dummyBlock.WriteJSON("blockDummy.json");
      
      // block1
      String[] block1Trans = { "BB" };
      blockNum += 1;
      BlockMaker block1 = new BlockMaker(dummyBlock.getHashID(), block1Trans, blockNum, tS, proofOFWORK, "Maya ", "Rudolph",
              "1999.12.12", "Virus", "Exercise", "Steroids", "123-45-6888", B1_UUID.toString(), B1_UUID);
      block1.puzzleSolver();
      block1.WriteJSON("block1.json");
      
      // block2
      String[] block2Trans = { "CC" };
      blockNum += 1;
      BlockMaker block2 = new BlockMaker(block1.getHashID(), block2Trans, blockNum, tS, proofOFWORK, "Julie", "Morimoto",
              "1986.03.01", "Insomnia", "Exercise", "HotPeppers", "123-45-6999", B2_UUID.toString(), B2_UUID);
      block2.puzzleSolver();
      block2.WriteJSON("block2.json");
      
      // block3
      String[] block3Trans = { "DD" };
      blockNum += 1;
      BlockMaker block3 = new BlockMaker(block2.getHashID(), block3Trans, blockNum, tS, proofOFWORK, "JJ", "Russo",
              "2000.01.11", "Measles", "WaitToGetBetter", "CodLiverOil", "123-45-6777", B3_UUID.toString(), B3_UUID);
      block3.puzzleSolver();
      block3.WriteJSON("block3.json");

      System.out.println("Hash of block 0: " + dummyBlock.getHashID());// debug
      System.out.println("PrevHash of block 0: " + dummyBlock.getPrevHashID());// debug
      System.out.println("BLocknumber for block0: " + dummyBlock.getBlockNumber());// debug
      System.out.println("First name for block0: " + dummyBlock.getFirstName());// debug
      System.out.println("Last name for block0: " + dummyBlock.getLastName());// debug
      System.out.println("DOB for block0: " + dummyBlock.getDOB());// debug
      System.out.println("Rx for block0: " + dummyBlock.getRx());// debug
      System.out.println("SSN: " + dummyBlock.getSSNum());// debug
      System.out.println("Illness: " + dummyBlock.getIllness());// debug
      System.out.println("Treatment: " + dummyBlock.getTreatment());// debug
      System.out.println("Is block0 verified?: " + dummyBlock.verifyTheBlock() + "\n");// debug

      System.out.println("Hash of block 1: " + block1.getHashID());// debug
      System.out.println("PrevHash of block 1: " + block1.getPrevHashID());// debug
      System.out.println("BLocknumber for block1: " + block1.getBlockNumber());// debug
      System.out.println("First name for block1: " + block1.getFirstName());// debug
      System.out.println("Last name for block1: " + block1.getLastName());// debug
      System.out.println("DOB for block1: " + block1.getDOB());// debug
      System.out.println("Rx for block1: " + block1.getRx());// debug
      System.out.println("SSN: " + block1.getSSNum());// debug
      System.out.println("Illness: " + block1.getIllness());// debug
      System.out.println("Treatment: " + block1.getTreatment());// debug
      System.out.println("Is block1 verified?: " + block1.verifyTheBlock() + "\n");// debug

      System.out.println("Hash of block 2: " + block2.getHashID());// debug
      System.out.println("PrevHash of block 2: " + block2.getPrevHashID());// debug
      System.out.println("BLocknumber for block2: " + block2.getBlockNumber());// debug
      System.out.println("First name for block2: " + block2.getFirstName());// debug
      System.out.println("Is block2 verified?: " + block2.verifyTheBlock() + "\n");// debug

      System.out.println("Hash of block 3: " + block3.getHashID());// debug
      System.out.println("PrevHash of block 3: " + block3.getPrevHashID());// debug
      System.out.println("BLocknumber for block3: " + block3.getBlockNumber());// debug
      System.out.println("First name for block3: " + block3.getFirstName());// debug
      System.out.println("Is block3 verified?: " + block3.verifyTheBlock() + "\n");// debug
  }
}
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

	  public BlockMaker(String prevHashID, String[] transactions, int blockNumber, String timeStamp, int proof_of_work,
	          String firstName, String lastName, String dob, String illness, String treatment, String Rx, String ssNumber,
	          String BlockID, UUID uuid) {
	      this.prevHashID = prevHashID;
	      this.transactions = transactions;
	      this.blockNumber = blockNumber + 1;// to identify which block it is
	      this.timeStamp = timeStamp;
	      this.nonce_randomSeed = 0;
	      this.proof_of_work = proof_of_work;

	      this.firstName = firstName;
	      this.lastName = lastName;
	      this.dob = dob;
	      this.Rx = Rx;
	      this.illness = illness;
	      this.treatment = treatment;
	      this.ssNumber = ssNumber;

	      this.BlockID = BlockID;
	      this.uuid = uuid;

	      this.hashID = calcHashID();
	  }

	  // VERIFY------------------
	  public boolean verifyTheBlock() {// The umbrella method that returns the puzzle answer
	      return (this.hashID.equals(calcHashID()));
	  }

	  public boolean puzzleSolver() {// The mechanism to check the puzzle
	      this.hashID = calcHashID();
	      while (!puzzleSolved(this.hashID)) {// if puzzle conditions not satisfied, keep going
	          this.nonce_randomSeed++;
	          this.hashID = calcHashID();
	      }
	      return true;
	  }

	  public boolean puzzleSolved(String hash) {// The condition checker to see if puzzle meets condition
	      char[] checker = hash.toCharArray();
	      for (int i = 0; i < this.proof_of_work; i++) {
	          if (checker[i] != '0') {
	              return false;
	          }
	      }
	      System.out.println("verified block!");// debug
	      return true;

	  }
	  // VERIFY END------------------

	  public String getPrevHashID() {
	      return prevHashID;
	  }

	  public String[] getTransactions() {
	      return transactions;
	  }

	  public String getHashID() {
	      return hashID;
	  }

	  public String getTimeStamp() {
	      return timeStamp;
	  }

	  public int getNonce_RS() {
	      return nonce_randomSeed;
	  }

	  public void setNonce_RS(int nc) {
	      this.nonce_randomSeed = nc;
	  }

	  public String getFirstName() {
	      return this.firstName;
	  }

	  public String getLastName() {
	      return this.lastName;
	  }

	  public String getDOB() {
	      return this.dob;
	  }

	  public String getRx() {
	      return this.Rx;
	  }

	  public String getTreatment() {
	      return this.treatment;
	  }

	  public String getIllness() {
	      return this.illness;
	  }

	  public String getSSNum() {
	      return this.ssNumber;
	  }

	  public String getUUID() {
	      return this.uuid.toString();
	  }

	  // SHA-256 digest of the block to get hash--------
	  public String calcHashID() {
	      StringBuilder builder = new StringBuilder();
	      builder.append(this.prevHashID);
	      builder.append(this.transactions);
	      builder.append(this.blockNumber);
	      builder.append(this.timeStamp);
	      builder.append(this.nonce_randomSeed);

	      builder.append(this.firstName);
	      builder.append(this.lastName);
	      builder.append(this.dob);
	      builder.append(this.Rx);
	      builder.append(this.illness);
	      builder.append(this.treatment);
	      builder.append(this.ssNumber);

	      builder.append(this.BlockID);
	      builder.append(this.uuid);

	      byte[] byteData = mDigest_SHA256_to_toBytes(builder.toString());

	      StringBuffer sbuff = new StringBuffer();
	      for (int i = 0; i < byteData.length; i++) {
	          sbuff.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	      }

	      String SHA256String = sbuff.toString();
	      return SHA256String;
	  }

	  public static String mDigest_SHA256_to_toString(String str) {
	      return Base64.getEncoder().encodeToString(mDigest_SHA256_to_toBytes(str));
	  }

	  public static byte[] mDigest_SHA256_to_toBytes(String str) {
	      try {
	          MessageDigest md = MessageDigest.getInstance("SHA-256");
	          md.update(str.getBytes());
	          return md.digest();
	      } catch (java.security.NoSuchAlgorithmException e) {
	          throw new RuntimeException(e);
	      }
	  }
	  // SHA-256 digest of the block to get hash END--------

	  // block stuff
	  public String getBlockID() {
	      return this.BlockID;
	  }

	  public int getBlockNumber() {
	      return blockNumber;
	  }

	  public void setBlockNumber(int block_num) {
	      this.blockNumber = block_num;
	  }

	  // block stuff END
	  // JSON related stuff
	  public void WriteJSON(String JsonFileName) {
	      System.out.println("JSON stuff happening");

	      Gson gson = new GsonBuilder().setPrettyPrinting().create();

	      // java object gets converted to string
	      String json = gson.toJson(this);

	      System.out.println("\nJSON string of current class: " + json);
	      
	      // Write the json object to a file
	      try (FileWriter writer = new FileWriter(JsonFileName)) {
	          gson.toJson(this, writer);
	      } catch (IOException e) {
	          e.printStackTrace();
	      }
	      System.out.println("JSON stuff is done.");
	  }

	  // JSON related stuff END
}
