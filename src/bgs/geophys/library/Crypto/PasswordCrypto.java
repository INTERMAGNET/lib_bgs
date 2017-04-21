/*
 * PasswordCrypto.java
 *
 * Created on 30 December 2005, 21:49
 */

package bgs.geophys.library.Crypto;

import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
import com.sun.crypto.provider.SunJCE;

import bgs.geophys.library.Misc.*;

/**
 * A class to encrypt and decrpyt plain text using password based encryption.
 *
 * @author  Administrator
 */
public class PasswordCrypto 
{

    // debugging code
    /*
    public static void main (String args[])
    {
        StoreCrypto tester;
        byte pass_phrase [];
        boolean test_passwd;
        String cleartext;
        byte encrypted_text [];
        byte decrypted_text [];
        byte cleartext_bytes [];
        
        try
        {
            tester = new StoreCrypto (bytes2Chars ("this-is-a-passwd".getBytes()));
            pass_phrase = tester.getEncyptedPassPhrase();
            test_passwd = tester.checkEncyptedPassPhrase(pass_phrase);
            cleartext = "This is some text for encyption";
            cleartext_bytes = cleartext.getBytes();
            encrypted_text = tester.encrypt(cleartext_bytes);
            decrypted_text = tester.decrypt(encrypted_text);
            
            System.out.println (test_passwd);
            System.out.println (cleartext_bytes.length);
            System.out.println (new String (cleartext_bytes));
            System.out.println (encrypted_text.length);
            System.out.println (new String (encrypted_text));
            System.out.println (decrypted_text.length);
            System.out.println (new String (decrypted_text));
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    */
    
    private Cipher encrypt_cipher;
    private Cipher decrypt_cipher;
    private boolean b64_flag;
    
    /** Creates a new instance of StoreCrypto 
     * @param passwd the password
     * @param b64_flag true if encrypted text needs to be Base64 encoded and decoded
     *        text needs to be base64 decoded
     * @throws NoSuchAlgorithmException if a secret-key factory for the specified 
     *         algorithm is not available in the default provider package or any of 
     *         the other provider packages that were searched, if no transformation 
     *         was specified, or if the specified transformation is not available 
     *         from the specified provider.
     * @throws InvalidKeySpecException if the given key specification is inappropriate 
     *         for this secret-key factory to produce a secret key.
     * @throws NoSuchPaddingException if transformation contains a padding scheme 
     *         that is not available. 
     * @throws InvalidKeyException if the given key is inappropriate for initializing 
     *         this cipher, or its keysize exceeds the maximum allowable keysize (as 
     *         determined from the configured jurisdiction policy files). 
     * @throws InvalidAlgorithmParameterException if the given algorithm parameters 
     *         are inappropriate for this cipher, or this cipher is being initialized 
     *         for decryption and requires algorithm parameters and params is null, 
     *         or the given algorithm parameters imply a cryptographic strength that 
     *         would exceed the legal limits (as determined from the configured 
     *         jurisdiction policy files). */
    public PasswordCrypto (char[] passwd, boolean b64_flag)
    throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
    {
        int length, count;
        boolean ciphers_created;
        PBEKeySpec pbe_key_spec;
        PBEParameterSpec pbe_param_spec;
        SecretKeyFactory key_fac;
        SecretKey pbe_key;
        char pp [];

        // Salt - use random variable name
        byte[] wefrlk = 
        { 
            (byte) 0x34, (byte) 0x8c, (byte) 0x29, (byte) 0xb5, 
            (byte) 0xd5, (byte) 0x01, (byte) 0xf8, (byte) 0x83 
        };

        // Iteration count - use random variable name
        int wemrtp = 17;

        // clear sensitive variables in a finally block
        this.b64_flag = b64_flag;
        pbe_key_spec = null;
        ciphers_created = false;
        pp = null;
        try
        {
            // pad password to 16 bytes
            length = passwd.length % 16;
            pp = new char [passwd.length + length];
            for (count=0; count<pp.length; count++)
            {
                if (count < passwd.length) pp [count] = passwd [count];
                else pp [count] = (char) ((count % 95) + 32);
            }
            
            // Create PBE parameter set
            pbe_param_spec = new PBEParameterSpec(wefrlk, wemrtp);

            // convert encryption password into a SecretKey object, using a PBE key factory
            pbe_key_spec = new PBEKeySpec(pp);
            key_fac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            pbe_key = key_fac.generateSecret(pbe_key_spec);

            // Create PBE Ciphers
            encrypt_cipher = Cipher.getInstance("PBEWithMD5AndDES");
            decrypt_cipher = Cipher.getInstance("PBEWithMD5AndDES");

            // Initialize PBE Ciphers with key and parameters
            encrypt_cipher.init(Cipher.ENCRYPT_MODE, pbe_key, pbe_param_spec);
            decrypt_cipher.init(Cipher.DECRYPT_MODE, pbe_key, pbe_param_spec);
            ciphers_created = true;
        }
        
        // pass on all exceptions
        catch (NoSuchAlgorithmException e) { throw e; }
        catch (InvalidKeySpecException e) { throw e; }
        catch (NoSuchPaddingException e) { throw e; }
        catch (InvalidKeyException e) { throw e; }
        catch (InvalidAlgorithmParameterException e) { throw e; }
        
        // clear unwanted encryption variables
        finally
        {
            clear (wefrlk);
            if (pp != null) clear (pp);
            wemrtp = 0;
            if (pbe_key_spec != null) pbe_key_spec.clearPassword ();
            if (! ciphers_created)
            {
                encrypt_cipher = null;
                decrypt_cipher = null;
            }
        }
    }
    
    /** set internal variables to a state whereby the
     * cipher can no longer be used */
    public void clear ()
    {
        encrypt_cipher = null;
        decrypt_cipher = null;
    }
    
    /** set a byte array's elements to zero so they cannot be used
      * @param text the text to set to zero */
    public static void clear (byte text [])
    {
        int count;
        
        for (count=0; count<text.length; count++) text[count] = 0;
    }
    
    /** set a char array's elements to zero so they cannot be used
      * @param text the text to set to zero */
    public static void clear (char text [])
    {
        int count;
        
        for (count=0; count<text.length; count++) text[count] = 0;
    }
    
    /** convert a byte array to a char array
     * @param byte_array the byte array to convert
     * @return the new char array */
    public static char [] bytes2Chars (byte byte_array [])
    {
        int count;
        char char_array [];
        
        char_array = new char [byte_array.length];
        for (count=0; count<byte_array.length; count++)
            char_array [count] = (char) byte_array [count];
        
        return char_array;
    }
    
    /** get an encypted pass-phrase to be used to verify that a password is correct -
     * call clear() with the pass-phrase when it is no longer needed
     * @returns the pass-phrase
     * @throws IllegalStateException if this cipher is in a wrong state (e.g., has 
     *         not been initialized) 
     * @throws IllegalBlockSizeException if this cipher is a block cipher, no padding 
     *         has been requested (only in encryption mode), and the total input 
     *         length of the data processed by this cipher is not a multiple of block size 
     * @throws BadPaddingException if this cipher is in decryption mode, and (un)padding 
     *         has been requested, but the decrypted data is not bounded by the appropriate 
     *         padding bytes */
    public byte [] getEncryptedPassPhrase ()
    throws IllegalStateException, IllegalBlockSizeException, BadPaddingException
    {
        return encrypt (getPassPhrase());
    }
    
    /** check an encrypted pass-phrase against the internal pass-phrase
     * @param test_encrypted_phrase the pass-phrase to check
     * @returns true of the phrases match, false otherwise
     * @throws IllegalStateException if this cipher is in a wrong state (e.g., has 
     *         not been initialized) 
     * @throws IllegalBlockSizeException if this cipher is a block cipher, no padding 
     *         has been requested (only in encryption mode), and the total input 
     *         length of the data processed by this cipher is not a multiple of block size 
     * @throws BadPaddingException if this cipher is in decryption mode, and (un)padding 
     *         has been requested, but the decrypted data is not bounded by the appropriate 
     *         padding bytes */
    public boolean checkEncyptedPassPhrase (byte test_encrypted_phrase [])
    throws IllegalStateException, IllegalBlockSizeException, BadPaddingException
    {
        int count;
        boolean ret_val;
        byte wefrek [], trekle [];
        
        ret_val = true;
        wefrek = getPassPhrase();
        trekle = decrypt (test_encrypted_phrase);
        if (trekle.length < wefrek.length) ret_val = false;
        else
        {
            for (count=0; count<wefrek.length; count++)
            {
                if (wefrek[count] != trekle[count]) ret_val = false;
            }
        }
            
        clear (trekle);
        clear (wefrek);
        
        return ret_val;
    }
        
    /** encrypt some text
     * @param text the text to encrypt
     * @return the encrypted text
     * @throws IllegalStateException if this cipher is in a wrong state (e.g., has 
     *         not been initialized) 
     * @throws IllegalBlockSizeException if this cipher is a block cipher, no padding 
     *         has been requested (only in encryption mode), and the total input 
     *         length of the data processed by this cipher is not a multiple of block size 
     * @throws BadPaddingException if this cipher is in decryption mode, and (un)padding 
     *         has been requested, but the decrypted data is not bounded by the appropriate 
     *         padding bytes */
    public byte [] encrypt (byte cleartext [])
    throws IllegalStateException, IllegalBlockSizeException, BadPaddingException
    {
        int count;
        byte b64_encoded [], encrypted_text [], length_bytes [], length_and_encrypt [];
        
        if (encrypt_cipher == null) throw new IllegalStateException ("Cipher not available");
        
        // encrypt the clear text
        encrypted_text = encrypt_cipher.doFinal (cleartext);
        
        // add the length of the clear text to the front of the byte arry
        length_bytes = Utils.intToBytes (cleartext.length);
        length_and_encrypt = new byte [length_bytes.length + encrypted_text.length];
	for (count=0; count<length_bytes.length; count++)
            length_and_encrypt[count] = length_bytes[count];
	for (count=0; count<encrypted_text.length; count++)
            length_and_encrypt[length_bytes.length + count] = encrypted_text[count];
        clear (encrypted_text);
        
        // optionally encode with base64 (for systems that can't cope with binary data)
        b64_encoded = null;
        if (b64_flag) 
        {
            b64_encoded = Base64.encode(length_and_encrypt);
            clear (length_and_encrypt);
        }

        if (b64_flag) return b64_encoded;
        return length_and_encrypt;
    }
    
    /** decrypt some text
     * @param coded_text the text to decrypt
     * @return the decrypted text
     * @throws IllegalStateException if this cipher is in a wrong state (e.g., has 
     *         not been initialized) 
     * @throws IllegalBlockSizeException if this cipher is a block cipher, no padding 
     *         has been requested (only in encryption mode), and the total input 
     *         length of the data processed by this cipher is not a multiple of block size 
     * @throws BadPaddingException if this cipher is in decryption mode, and (un)padding 
     *         has been requested, but the decrypted data is not bounded by the appropriate 
     *         padding bytes */
    public byte [] decrypt (byte coded_text [])
    throws IllegalStateException, IllegalBlockSizeException, BadPaddingException
    {
        int length, count;
        byte length_and_encrypt [], length_bytes [], encrypted_text [], cleartext [], text_no_pad [];

        if (decrypt_cipher == null) throw new IllegalStateException ("Cipher not available");
        
        // optionally decode from base 64
        if (b64_flag) length_and_encrypt = Base64.decode(coded_text);
        else length_and_encrypt = coded_text;
        
        // strip the length of the clear text, which is the first four bytes in
        // the encrypted text
        if (length_and_encrypt.length < 4) throw new BadPaddingException ("Entry too short");
        length_bytes = new byte [4];
        encrypted_text = new byte [length_and_encrypt.length -4];
        for (count=0; count<4; count++) 
            length_bytes [count] = length_and_encrypt [count];
        for (count=4; count<length_and_encrypt.length; count++) 
            encrypted_text[count -4] = length_and_encrypt[count];
        length = Utils.bytesToInt (length_bytes);
        if (b64_flag) clear (length_and_encrypt);

        // decrypt the text
        cleartext = decrypt_cipher.doFinal (encrypted_text);
        clear (encrypted_text);

        // remove any padding
        text_no_pad = null;
        if (cleartext.length < length) throw new BadPaddingException ("Entry too short");
        else if (cleartext.length > length)
        {
            text_no_pad = new byte [length];
            for (count=0; count<length; count++)
                text_no_pad [count] = cleartext [count];
            clear (cleartext);
        }
        
        if (text_no_pad == null) return cleartext;
        return text_no_pad;
    }
    
    /** check a plain text pass-phrase against the internal pass-phrase
     * @returns true of the phrases match, false otherwise */
    private boolean checkPassPhrase (byte test_phrase [])
    {
        int count;
        boolean ret_val;
        byte wefrek [];
        
        ret_val = true;
        wefrek = getPassPhrase();
        if (test_phrase.length != wefrek.length)
            ret_val = false;
        else
        {
            for (count=0; count<wefrek.length; count++)
            {
                if (test_phrase[count] != wefrek[count])
                    ret_val = false;
            }
        }
            
        clear (wefrek);
        
        return ret_val;
    }
        
    /** get a plain-text pass-phrase to be used to verify that a password is correct -
     * call clear() with the pass-phrase when it is no longer needed
     * @returns the pass-phrase */
    private byte [] getPassPhrase ()
    {
        byte[] wefrlk = 
        {
            (byte) 0x93, (byte) 0x3b, (byte) 0xd8, (byte) 0x12,
            (byte) 0xa8, (byte) 0xd3, (byte) 0x79, (byte) 0xe9,
            (byte) 0x18, (byte) 0xc4, (byte) 0x94, (byte) 0x38,
            (byte) 0x1c, (byte) 0xa8, (byte) 0x48, (byte) 0x65,
            
            (byte) 0xad, (byte) 0xc9, (byte) 0x47, (byte) 0x26,
            (byte) 0x2e, (byte) 0x29, (byte) 0x77, (byte) 0x3e,
            (byte) 0x46, (byte) 0x17, (byte) 0xa7, (byte) 0x95,
            (byte) 0xd8, (byte) 0x95, (byte) 0x73, (byte) 0xc6,
            
            (byte) 0x36, (byte) 0xc8, (byte) 0x25, (byte) 0xc8,
            (byte) 0xa7, (byte) 0x93, (byte) 0x53, (byte) 0x74,
            (byte) 0x14, (byte) 0x04, (byte) 0xa0, (byte) 0x75,
            (byte) 0x9a, (byte) 0xb6, (byte) 0x30, (byte) 0x32,
            
            (byte) 0xb0, (byte) 0x69, (byte) 0x03, (byte) 0x6e,
            (byte) 0x0e, (byte) 0x94, (byte) 0x26, (byte) 0x33,
            (byte) 0x64, (byte) 0x85, (byte) 0x49, (byte) 0xef,
            (byte) 0xbb, (byte) 0x9f, (byte) 0x22, (byte) 0x0d
        };
        
        return wefrlk;
    }

    
}
