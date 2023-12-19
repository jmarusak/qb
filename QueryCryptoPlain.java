import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.util.*;

public class QueryCryptoPlain 
{    
    public byte[] hex2byte(String str)
    {
       str = str.toLowerCase();
       
       byte[] bytes = new byte[str.length() / 2];
       for (int i = 0; i < bytes.length; i++)
       {
          bytes[i] = (byte) Integer
                .parseInt(str.substring(2 * i, 2 * i + 2), 16);
       }
       return bytes;
    }

    public String byte2hex(byte[] b)
    {

     // String Buffer can be used instead

       String hs = "";
       String stmp = "";

       for (int n = 0; n < b.length; n++)
       {
          stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));

          if (stmp.length() == 1)
          {
             hs = hs + "0" + stmp;
          }
          else
          {
             hs = hs + stmp;
          }

          if (n < b.length - 1)
          {
             hs = hs + "";
          }
       }

       return hs.toUpperCase();
    }


    public QueryCryptoPlain() {

    }

    public String encrypt(String secretKey, String plainText) throws Exception {
        byte[] salt = {
            (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03
        };
        int iterationCount = 19;

        KeySpec keySpec = new PBEKeySpec(secretKey.toCharArray(), salt, iterationCount);
        SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
        AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

        Cipher cipher = Cipher.getInstance(key.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
        
        byte[] in = plainText.getBytes("UTF-8");
        byte[] out = cipher.doFinal(in);
        String encStr = byte2hex(out);
        
        return encStr;
    }
    
    public String decrypt(String secretKey, String encryptedText) throws Exception {
        byte[] salt = {
            (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03
        };
        int iterationCount = 19;
     
        KeySpec keySpec = new PBEKeySpec(secretKey.toCharArray(), salt, iterationCount);
        SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
        AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
        
        Cipher cipher = Cipher.getInstance(key.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
       
        byte[] enc = hex2byte(encryptedText);
        byte[] utf8 = cipher.doFinal(enc); 
        String plainStr = new String(utf8, "UTF-8");
        
        return plainStr;
    }    
    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            System.out.println();
            System.out.println("QueryCryptoPlain: Release 1.0");
            System.out.println();
            System.out.println("Usage: QueryCrypto <key> <password>");
            System.out.println();       
            return;
        }
        
        QueryCrypto QueryCrypto=new QueryCrypto();
        
        String key=args[0];   
        String enc=args[1];
        
        String plain=QueryCrypto.decrypt(key, enc);
        
        System.out.println(); 
        System.out.println("Key: "+key);
        System.out.println("Pwd: "+plain);
    }
}
