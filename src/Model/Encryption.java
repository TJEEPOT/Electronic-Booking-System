/******************************************************************************
 
 Project     : CMP-5012B - Software Engineering Project:
                EventBookingSystem.
 
 File        : Encryption.java
 
 Date        : Thursday 04 June 2020
 
 Author      : Martin Siddons
 
 Description : This class defines static methods to do with encryption of data,
    initially generateSalt and generateHash.
 
 History     : 05/06/2020 - v1.0 - Initial setup, ported methods here.
 ******************************************************************************/
package Model;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class Encryption {
    /**
     * Generates a salt to be stored in the database for use in hashing passwords.
     *
     * @return a 16 byte salt, as byte array.
     */
    public static byte[] generateSalt(){
        SecureRandom rand = new SecureRandom();
        byte[] salt = new byte[16];
        rand.nextBytes(salt);
        return salt;
    }
    
    /**
     * Creates a password hash from a given salt and plaintext password using
     * PBKDF2 hashing algorithm.
     *
     * @param salt 16 byte array salt (retrieved from DB or generated).
     * @param pwd plaintext password String.
     * @return password hash as byte array or null if process failed.
     */
    public static byte[] generateHash(byte[] salt, String pwd){
        try {
            KeySpec spec = new PBEKeySpec(pwd.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return factory.generateSecret(spec).getEncoded();
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
