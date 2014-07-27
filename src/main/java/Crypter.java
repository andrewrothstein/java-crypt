import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by schopra on 7/25/14.
 * Fiddled upon by arothste on 26-JUL-2014
 */
public class Crypter {
    public static boolean encryptStream(InputStream in, OutputStream out, String passKey) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        //we use the sha-256 sum of the passcode as passkey for encryption
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        System.out.println("Using pass key " + new String(Base64.encodeBase64(md.digest(passKey.getBytes()))));
        SecretKey sKey = new SecretKeySpec(md.digest(passKey.getBytes()), "AES");

        Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aes.init(Cipher.ENCRYPT_MODE, sKey);

        //extract the initialization vector to save with the encrypted data.
        //a new iv is randomly created everytime, so we get fair bit of randomization with everyfile
        //in the reference code from Brad, this value was also stored with the encrypted data.
        byte[] iv = aes.getIV();
        System.out.println("IV = " + new String(Base64.encodeBase64(iv)));
        out.write(iv);
        CipherOutputStream cout = new CipherOutputStream(out, aes);
        IOUtils.copy(in, cout);
        //we need to close the cipher stream to make sure all the blocks are flushed out properly
        cout.close();
        out.close();
        in.close();
        return true;
    }

    public static boolean decryptStream(InputStream in, OutputStream out, String passKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidAlgorithmParameterException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        System.out.println("Using pass key " + new String(Base64.encodeBase64(md.digest(passKey.getBytes()))));
        SecretKey sKey = new SecretKeySpec(md.digest(passKey.getBytes()), "AES");
        Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte [] iv = new byte[16];
        //we know the first 16 bytes are the intialization vector, so we read them
        in.read(iv);
        System.out.println("IV = " + new String(Base64.encodeBase64(iv)));
        aes.init(Cipher.DECRYPT_MODE, sKey, new IvParameterSpec(iv));

        CipherInputStream cin = new CipherInputStream(in, aes);
        IOUtils.copy(cin, out);

        out.close();
        cin.close();
        in.close();
        return true;
    }

    public static boolean encryptFile(String inFile, String outFile, String passKey) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        return encryptStream(new FileInputStream(inFile), new FileOutputStream(outFile), passKey);
    }

    public static boolean decryptFile(String inFile, String outFile, String passKey) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        return decryptStream(new FileInputStream(inFile), new FileOutputStream(outFile), passKey);
    }

    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {
        if(args.length < 2) {
            System.err.println("Usage : Encryption password InputFile");
            System.err.println("This will generate encrypted InputFile.enc file and InputFile.dec decrypted file.");
            System.exit(1);
        }

        String password = args[1].trim();
        String inputFile = args[0].trim();

	//        Encryption.encryptFile("target/scala-2.10/LoanVault-assembly-0.1.jar", "test.jar.enc", "testpassword");
	//        Encryption.decryptFile("test.jar.enc", "test.dec.jar", "testpassword");
        encryptFile(inputFile, inputFile + ".enc", password);
        decryptFile(inputFile + ".enc", inputFile + ".dec", password);
    }
}
