
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.encrypt;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

// these are the DECRYPTION and ENCRYPTION routines
// when reading these classes replace the words
// "FileCheck" with "Encrypt"
//
// Usage:
// Checkfile(String fName, String mode)
//           fName = file to be encrypted/decrypted
//           mode = "check" - decrypt, anything else will encrypt
// returns the de-crypted file. It is up to the implementation to delete it
// as soon as it is finished with, otherwise the decrypted file will be left on disc!
//  
// getData will return a String array of decrypted contents - provided the file was originally 
// encrypted using checkFile.
/**
 *
 * @author jex
 */
public class FileCheckUtils {
    
private static boolean debug = false;

  public  static File checkFile(String datFile, String mode) throws FileCheckException{
        File outFile;
        FileInputStream fis;
        FileOutputStream fos;
        CipherInputStream cis;

        Cipher cipher;
        

        try{
        SecretKey desKey = generateBGGMKey();   
        // Create the cipher 
        cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

        if(mode.equalsIgnoreCase("check")){
//        outFile = decryptFileName(datFile);
        outFile = File.createTempFile("~34#", ".tmp");
        outFile.deleteOnExit();
        cipher.init(Cipher.DECRYPT_MODE, desKey);
        }
        else{
        outFile = new File(cryptFileName(datFile));  
        if(debug)System.out.println("output file is: "+outFile.getAbsolutePath());
        cipher.init(Cipher.ENCRYPT_MODE, desKey);
        }
       

    fis = new FileInputStream(datFile);
    cis = new CipherInputStream(fis, cipher);
    fos = new FileOutputStream(outFile);

    byte[] b = new byte[8];
    int i = cis.read(b);
    while (i != -1) {
        fos.write(b, 0, i);
        i = cis.read(b);
    }
    
    fis.close();
    cis.close();
    fos.close();
    
    return outFile;
                
        } catch (Exception e){
          throw new FileCheckException(e);
        }

    }
  
    //encrypts a string and returns as a byte array
    public static byte[] checkString(String strToEncrypt) throws FileCheckException {
        Cipher cipher;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            SecretKey desKey = generateBGGMKey();
            // Create the cipher 
            cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, desKey);

            //encrypt the string
            ByteArrayInputStream bais = new ByteArrayInputStream(strToEncrypt.getBytes());
            CipherInputStream cis = new CipherInputStream(bais, cipher);          

            //copy to a byte array output straem
            byte[] b = new byte[8];
            int i = cis.read(b);
            while (i != -1) {
                baos.write(b, 0, i);
                i = cis.read(b);
            }
            bais.close();
            cis.close();
        } 
        catch (Exception e) {            
            throw new FileCheckException(e);
        }
        
        //return a byte array of the encrypted string
        byte[] bytes = baos.toByteArray();
        return bytes;
    }  
 
  public static ArrayList <String> getData(String fname) throws FileCheckException{
 
      ArrayList <String> data = new ArrayList <String> ();
      
      try{
       File outFile = checkFile(fname,"check");
    
       BufferedReader br = new BufferedReader(new FileReader(outFile));
        
        String line;
        while((line = br.readLine())!=null){           
            data.add(line);
        }
        
        outFile.delete();

        return data;
                
        } catch (Exception e){
          throw new FileCheckException(e);
        }
    
  }  
  
  
    private static SecretKey generateBGGMKey() 
                throws FileCheckException {

     try{
       byte[] bkey =  CheckUtils.getBkey();
       DESKeySpec desKeySpec = new DESKeySpec(bkey);
       SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
       SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
       return secretKey;
    }catch(Exception e){
        throw new FileCheckException(e);}
    }
    
     private static String cryptFileName(String datFile)throws FileCheckException{

        try{
        String cptFile = new String();
        String suffix = new String(".crypt");
        int pos = datFile.lastIndexOf(".");
        if(pos != 0){
         cptFile = datFile.substring(0, pos)+ suffix;
        }else{
         cptFile = datFile.concat(suffix);
        }
        return cptFile;
        } catch(Exception e){
            throw new FileCheckException(e);
        }
}

    static void writeToScreen(byte[] text){
        
      String output = new String();
      Character c;
      for(int i=0;i<text.length;i++){
        c = (char) text[i];
        output = output.concat(c.toString());
       }
       System.out.println(output);

    }
}