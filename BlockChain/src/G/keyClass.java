package G;

//import java.security.*;
//import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.Cipher;
import java.security.spec.*;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
//using https://github.com/hhohho/Learning-Blockchain-in-Java/blob/master/Learning-Blockchain-in-Java-Chapter2-src.rar as reference
import java.io.Reader;

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
class RSAkeyClass{
	private KeyPair kpair;//helps make key pair
	private Cipher cipher;//object that makes a cipher
	
	RSAkeyClass(int keySize)//constructor to generate a key pair in a specified size
	{
		try {
			KeyPairGenerator kPairGen = KeyPairGenerator.getInstance("RSA");//create keypair generator using RSA algo
			kPairGen.initialize(keySize);//init with keySize
			kpair=kPairGen.genKeyPair();//where we generate the keyPair
		}
		catch(NoSuchAlgorithmException e){//check exceptions when generating keypair
			e.printStackTrace();
		}
		try {
			cipher = Cipher.getInstance("RSA");//create cipher object to encrypt or decrypt
			//this will be the object to cypher since we have a specific instance made of Cipher
		}
		catch(NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException on Cipher.getInstance(RSA)");
		} 
		catch (NoSuchPaddingException e) {
			System.out.println("NoSuchPaddingException on Cipher.getInstance(RSA)");
		}
	}
	
	public  PublicKey makePublicKey() {//make a public key
		return(kpair.getPublic());
		//uses getPublic() method on kpair
	}
	public PrivateKey makePrivateKey() {//make a private key
		return(kpair.getPrivate());
		//uses getPrivate() method on kpair
	}
	public byte[] encrypt(String inpt) {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, kpair.getPublic());//init a cypher using value of Cipher.Encrypt_MODE and encrypt with public key
			//note: ENCRYPT_MODE is a constant used to init a cipher for encryption mode
		}
		catch(InvalidKeyException e) {System.out.println("InvalidKeyException on cipher.init for encrypt");}//any exceptions caught when encrypting
		
		try {
			
			return(cipher.doFinal(inpt.getBytes()));//finishes the encryption adn turn the input into Bytes using getBytes()
		}
		catch(IllegalBlockSizeException e) {
			System.out.println("IllegalBlockSizeException on cipher.doFinal for encrypt");
		} 
		catch(BadPaddingException e) {
            System.out.println("BadPaddingException on cipher.doFinal for encrypt");
        }
		
		return("".getBytes());//default if block fails then we don't have anything
	}
	public String decrypt(byte[] inpt){//takes in an encrypted message. inpt will act as a hash of sorts
		try {
			cipher.init(Cipher.DECRYPT_MODE, kpair.getPrivate());//init a cypher using value of Cipher.Decrypt_MODE and decrypt with private key
			//Note: DECRYPT_MODE is a constant used to init a cipher for decryption mode
		}
		catch(InvalidKeyException e) {
			System.out.println("InvalidKeyException on cipher.init for decrypt");
		}
		
		try {
			return(new String(cipher.doFinal(inpt)));//finishes teh decryption and turns the input into String
		}
		catch(IllegalBlockSizeException e) {
			System.out.println("IllegalBlockSizeException on cipher.doFinal for decrypt");
		}
		catch(BadPaddingException e) {
            System.out.println("BadPaddingException on cipher.doFinal for decrypt");
        }
		return("");//default if block fails so we won't have anything
	}
	
	public byte[] EncryptWithPrivate(String inpt) {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, kpair.getPrivate());//init a cypher using value of Cipher.Encrypt_MODE and encrypt with private key
			//note: ENCRYPT_MODE is a constant used to init a cipher for encryption mode
		}
		catch(InvalidKeyException e) {System.out.println("InvalidKeyException on cipher.init for encrypt");}//any exceptions caught when encrypting
		
		try {
			return(cipher.doFinal(inpt.getBytes()));//finishes the encryption adn turn the input into Bytes using getBytes()
		}
		catch(IllegalBlockSizeException e) {
			System.out.println("IllegalBlockSizeException on cipher.doFinal for encrypt");
		} 
		catch(BadPaddingException e) {
            System.out.println("BadPaddingException on cipher.doFinal for encrypt");
        }
		
		return("".getBytes());//default if block fails then we don't have anything
	}
	public String DecryptWithPublic(byte[] inpt) {
		try {
			cipher.init(Cipher.DECRYPT_MODE, kpair.getPublic());//init a cypher using value of Cipher.Decrypt_MODE and decrypt with public key
			//Note: DECRYPT_MODE is a constant used to init a cipher for decryption mode
		}
		catch(InvalidKeyException e) {
			System.out.println("InvalidKeyException on cipher.init for decrypt");
		}
		
		try {
			return(new String(cipher.doFinal(inpt)));//finishes teh decryption and turns the input into String
		}
		catch(IllegalBlockSizeException e) {
			System.out.println("IllegalBlockSizeException on cipher.doFinal for decrypt");
		}
		catch(BadPaddingException e) {
            System.out.println("BadPaddingException on cipher.doFinal for decrypt");
        }
		
		return("");//default if block fails so we won't have anything
	}
	
}
/**
 * @author anh17
 *
 */
public class keyClass {
	//fake readJSON results in for now
	public static void main(String[] args) {
		try {
			RSAkeyClass kPair = new RSAkeyClass(512);//using RSA algo to generate key pair 512 bytes long
			
			System.out.println("Enter something to encrypt: ");
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));//take in input (String)
			String input = r.readLine();//message
			
			byte[] cipheredInput = kPair.encrypt(input);
			
			//Symmetric encryption/decryption--------------------------
			
			//for Debugging
			System.out.println("Initial message: "+input+"\n");
			
			String kPairP = kPair.makePublicKey().toString();//make public key and turn to String
			System.out.println("Public Key: "+kPairP);//debug
			
			//json stuff
			WriteJSON(kPairP);
			ReadJSON("publicData.json", kPairP);
			
			System.out.println("key Data:");//debugging
			
			//other debug stuff
			System.out.println("encrypted with public key: "+cipheredInput.toString());
			System.out.println("decrypted with private key: "+kPair.decrypt(cipheredInput));
			cipheredInput=kPair.EncryptWithPrivate(input);
			System.out.println("encrypt with private key: "+cipheredInput.toString());
			System.out.println("decrypt with public key: "+kPair.DecryptWithPublic(cipheredInput));

		} catch (Exception e) {//throw exception if needed
			e.printStackTrace();
		} 

	}
	// JSON related stuff
	  public static void WriteJSON(String s) {
		  String JsonFileName = "publicData.json";
		  
	      System.out.println("WriteJSON stuff happening-----------------------------------");

	      Gson gson = new GsonBuilder().setPrettyPrinting().create();

	      // java object gets converted to string
	      String json = gson.toJson(s);

	      System.out.println("\nJSON string of current class: " + json);//what's inside json file
	      
	      // Write the json object to a file
	      try (FileWriter writer = new FileWriter(JsonFileName)) {
	          gson.toJson(json, writer);
	      } catch (IOException e) {
	          e.printStackTrace();
	      }
	      System.out.println("WriteJSON stuff is done.-----------------------------------\n");
	  }
	  public static void ReadJSON(String JsonFileName, String keyP) {//second paramter of keyP to remove another time
		  System.out.println("ReadJSON stuff happening-----------------------------------");
		  Gson gson = new Gson();
		  try (Reader reader = new FileReader(JsonFileName)){
			  
			  //Read and convert JSON file into Java object
			  //RSAkeyClass rsaKeyIn = gson.fromJson(reader, RSAkeyClass.class);
			  String rsaKeyIn = keyP;
			  
			  //print object
			  System.out.println("GSON block object: "+rsaKeyIn);
			  
		  }catch (IOException e) {
			  e.printStackTrace();
		  }
		  System.out.println("ReadJSON is done.-----------------------------------\n");
	  }

	  // JSON related stuff END

}
