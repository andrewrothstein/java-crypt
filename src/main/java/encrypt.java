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
public class encrypt {

    public static void encryptStream(InputStream in, OutputStream out, String passKey) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        //we use the sha-256 sum of the passcode as passkey for encryption
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        SecretKey sKey = new SecretKeySpec(md.digest(passKey.getBytes()), "AES");

        Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aes.init(Cipher.ENCRYPT_MODE, sKey);

        //extract the initialization vector to save with the encrypted data.
        //a new iv is randomly created everytime, so we get fair bit of randomization with everyfile
        //in the reference code from Brad, this value was also stored with the encrypted data.
        byte[] iv = aes.getIV();
        out.write(iv);
        CipherOutputStream cout = new CipherOutputStream(out, aes);
        IOUtils.copy(in, cout);

        //we need to close the cipher stream to make sure all the blocks are flushed out properly
        cout.close();
    }

    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException {
        if(args.length < 2) {
            System.err.println("Usage : encrypt password file");
            System.err.println("Will generate fil1.enc");
            System.exit(1);
        }

        String password = args[0].trim();
        String inputFile = args[1].trim();

	if (inputFile.equals("-")) {
	    encryptStream(System.in, System.out, password);
	}
	else {
	    FileInputStream in = new FileInputStream(inputFile);
	    FileOutputStream out = new FileOutputStream(inputFile + ".enc");
	    encryptStream(in, out, password);
	    in.close();
	    out.close();
	}
    }
}
