package paukstadt.savechat.encryption;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class PasswordHelper
{
    public final static String SECRET = "secret";
    private final static char[] HEXARRAY = "0123456789ABCDEF".toCharArray();

    public static String getSha1HashAsHex(String toHash)
    {
        String hash = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = toHash.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            hash = bytesToHex( bytes );
        }
        catch( NoSuchAlgorithmException | UnsupportedEncodingException e )
        {
            Log.d("PasswordHelper", e.getMessage());
        }
        return hash;
    }

    private static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[ bytes.length * 2 ];
        for( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[ j ] & 0xFF;
            hexChars[ j * 2 ] = HEXARRAY[ v >>> 4 ];
            hexChars[ j * 2 + 1 ] = HEXARRAY[ v & 0x0F ];
        }
        return new String( hexChars );
    }

    public static String createSecret(String password, String phonenumber)
    {
        return getSha1HashAsHex(phonenumber + password);
    }

    public static byte[] encrypt(byte[] content, byte[] secret)
    {
        byte[] result = new byte[content.length];

        try
        {
            //Create key from secret
            SecretKeySpec key = new SecretKeySpec(secret, "AES");

            //Create cipher
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            int tmp = cipher.update(content, 0, content.length, result, 0);
            cipher.doFinal(result, tmp);
        }
        catch (Exception e)
        {
            //Ignore for the moment
        }

        return result;
    }

    public static byte[] decrypt(byte[] content, byte[] secret)
    {
        byte[] result = new byte[content.length];

        try
        {
            //Create key from secret
            SecretKeySpec key = new SecretKeySpec(secret, "AES");

            //Create cipher
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, key);
            int tmp = cipher.update(content, 0, content.length, result, 0);
            cipher.doFinal(result, tmp);
        }
        catch (Exception e)
        {
            //Ignore for the moment
        }

        return result;
    }
}
