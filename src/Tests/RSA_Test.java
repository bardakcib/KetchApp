package Tests;

import java.util.Base64;
import Core.RSA_Encryption;

public class RSA_Test {

	public RSA_Test(String input) {
		try {
			RSA_Encryption rsa = new RSA_Encryption(true);

			String encryptedText = rsa.Encrypt(input, null);
			System.out.println(encryptedText);

			byte[] chipper = Base64.getDecoder().decode(encryptedText);
			String decryptedText = rsa.Decrypt(chipper, null);
			System.out.println(decryptedText);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
