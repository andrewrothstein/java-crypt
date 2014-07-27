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
public class decrypt {

    public static void decryptStream(InputStream in, OutputStream out, String passKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidAlgorithmParameterException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        SecretKey sKey = new SecretKeySpec(md.digest(passKey.getBytes()), "AES");
        Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte [] iv = new byte[16];
        //we know the first 16 bytes are the intialization vector, so we read them
        in.read(iv);
        aes.init(Cipher.DECRYPT_MODE, sKey, new IvParameterSpec(iv));

        CipherInputStream cin = new CipherInputStream(in, aes);
        IOUtils.copy(cin, out);

        cin.close();
    }

    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {
        if(args.length < 2) {
            System.err.println("Usage : decrypt password file");
            System.err.println("Will generate file.dec");
            System.exit(1);
        }

        String password = args[0].trim();
        String inputFile = args[1].trim();

	if (inputFile.equals("-")) {
	    decryptStream(System.in, System.out, password);
	}
	else {
	    FileInputStream in = new FileInputStream(inputFile);
	    FileOutputStream out = new FileOutputStream(inputFile + ".dec");
	    decryptStream(in, out, password);
	    in.close();
	    out.close();
	}
    }
}
