package org.secure.sms;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

public class StringCryptor 
{
	private static final String CIPHER_ALGORITHM = "AES";
	private static final String RANDOM_GENERATOR_ALGORITHM = "SHA1PRNG";
	private static final int RANDOM_KEY_SIZE = 128;
	
	// Encrypts string and encode in Base64
	public static String encrypt( String password, String data ) throws Exception 
	{
		byte[] secretKey = 
				generateKey( password.getBytes() )
				// password.getBytes()
				;
	    byte[] clear = data.getBytes();
		
	    SecretKeySpec secretKeySpec = new SecretKeySpec( secretKey, CIPHER_ALGORITHM );
		Cipher cipher = Cipher.getInstance( CIPHER_ALGORITHM );
	    cipher.init( Cipher.ENCRYPT_MODE, secretKeySpec );
	    
	    byte[] encrypted = cipher.doFinal( clear );
	    String encryptedString = Base64.encodeToString( encrypted, Base64.DEFAULT );
	    
		return encryptedString;
	}
	
	// Decrypts string encoded in Base64
	public static String decrypt( String password, String encryptedData ) throws Exception 
	{
		byte[] secretKey = 
				generateKey( password.getBytes() )
				 //password.getBytes()
				;
		
		SecretKeySpec secretKeySpec = new SecretKeySpec( secretKey, CIPHER_ALGORITHM );
		Cipher cipher = Cipher.getInstance( CIPHER_ALGORITHM );
	    cipher.init( Cipher.DECRYPT_MODE, secretKeySpec );
	    
	    byte[] encrypted = Base64.decode( encryptedData, Base64.DEFAULT );
	    byte[] decrypted = cipher.doFinal( encrypted );
	    
		return new String( decrypted );
	}
	
	/*
	 * Java + Android + Encryption + Exception means just one thing normally,
	 *  somebody is using the SecureRandom class again as a key derivation function. 
	 *  This fails when the SecureRandom implementation of "SHA1PRNG" does not behave 
	 *  as the one in Sun's implementation in Java SE. Especially if the seed is added 
	 *  to the state of the random number generator instead of the seed being used as a starting point of the PRNG.
	 *  
	 *  Basically, simply use SecretKey aesKey = new SecretKeySpec(byte[] keyData, "AES") instead, 
	 *  or - if you start off with a password - try and generate the key using PBKDF2.
	 */
	public static byte[] generateKey( byte[] seed ) throws Exception
	{
		KeyGenerator keyGenerator = KeyGenerator.getInstance( CIPHER_ALGORITHM );
		SecureRandom secureRandom = new SecureRandom();
				//SecureRandom.getInstance( RANDOM_GENERATOR_ALGORITHM );
		//secureRandom.setSeed( seed );
	    keyGenerator.init( RANDOM_KEY_SIZE, secureRandom );
	    SecretKey secretKey = keyGenerator.generateKey();
	    return secretKey.getEncoded();
	}
}
his to "UTF-16" if needed
			for(int ii=0; ii < 36000; ii++){
				md.update(md.digest());
			}
			digest = md.digest();
			//Log.v(TAG,"\tHASH36k:"+Functions.convertByteArrayToHexString(digest));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Log.e(TAG,"\tHASH FAILED");
		}
        String salt = AesCbcWithIntegrity.saltString(AesCbcWithIntegrity.generateSalt());	
        
        //Log.i("StringCryptor", "Salt: " + salt);        
        AesCbcWithIntegrity.SecretKeys key = AesCbcWithIntegrity.generateKeyFromPassword(password, salt);
        
        // The encryption / storage & display:        
        AesCbcWithIntegrity.CipherTextIvMac civ = AesCbcWithIntegrity.encrypt(data, key);
        //Log.i("StringCryptor", "Encrypted: " + civ.toString());
        
		return civ.toString();
	}*/
	
	public static String encrypt( String password, String salt, String data ) throws GeneralSecurityException, UnsupportedEncodingException{
		
        AesCbcWithIntegrity.SecretKeys key = AesCbcWithIntegrity.generateKeyFromPassword(password, salt);
                
        AesCbcWithIntegrity.CipherTextIvMac civ = AesCbcWithIntegrity.encrypt(data, key);
        
		return civ.toString(); 
	}
	
	// Decrypts string encoded in Base64
	public static String decrypt( String password, String salt, String encryptedData ) throws GeneralSecurityException, UnsupportedEncodingException  	{
		// alternately, regenerate the key from password/salt.
		AesCbcWithIntegrity.SecretKeys key = AesCbcWithIntegrity.generateKeyFromPassword(password, salt);
		AesCbcWithIntegrity.CipherTextIvMac civ = new AesCbcWithIntegrity.CipherTextIvMac(encryptedData);
		
		String decryptedText = AesCbcWithIntegrity.decryptString(civ, key);
		
		return decryptedText;
	}
}
