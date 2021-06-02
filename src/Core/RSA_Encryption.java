package Core;

import java.util.Arrays;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.KeyPairGenerator;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.GeneralSecurityException;

public class RSA_Encryption {
	private final int size = 2048;
	private final String chipperInstance = "RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING";
//	private final String chipperInstance = "RSA";
	private final String keyFactoryInstance = "RSA";

	private PublicKey publicKey;
	private PrivateKey privateKey;
	private final String serverPublicKeyStr = new String(
			Files.readAllBytes(Paths.get("resources\\serverKeys\\publickey.txt")));
	private final String serverPrivateKeyStr = new String(
			Files.readAllBytes(Paths.get("resources\\serverKeys\\privatekey.txt")));

	public RSA_Encryption(boolean generateKey) throws Exception {
		if (generateKey) {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyFactoryInstance);

			// if chipperInstance = "RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING";
			// Then size should be min 2048
			// Increasing size will cost muuch when generating key pair,encrypt and decrypt
			// For less package size, use chipper size = RSA only and 512
			// this time length / 8 - 11 bytes to encrypt
			// https://stackoverflow.com/questions/10007147/getting-a-illegalblocksizeexception-data-must-not-be-longer-than-256-bytes-when
			keyPairGenerator.initialize(size);

			KeyPair keyPair = keyPairGenerator.generateKeyPair();

			publicKey = keyPair.getPublic();
			privateKey = keyPair.getPrivate();
		}
	}

	public String GetMyPublicKey() throws Exception {
		return savePublicKey(publicKey);
	}

	public String GetMyPrivateKey() throws Exception {
		return savePrivateKey(privateKey);
	}

	public String Encrypt(String plainText, String publicKey) throws Exception {
		Cipher cipher = Cipher.getInstance(chipperInstance);

		if (publicKey == null)
			publicKey = serverPublicKeyStr;

		cipher.init(Cipher.ENCRYPT_MODE, loadPublicKey(publicKey));
		byte[] cipherText = cipher.doFinal(plainText.getBytes());
		return Base64.getEncoder().encodeToString(cipherText);
	}

	public String Decrypt(byte[] cipherTextArray, String privateKey) throws Exception {
		Cipher cipher = Cipher.getInstance(chipperInstance);

		if (privateKey == null)
			privateKey = serverPrivateKeyStr;

		cipher.init(Cipher.DECRYPT_MODE, loadPrivateKey(privateKey));
		byte[] decryptedTextArray = cipher.doFinal(cipherTextArray);
		return new String(decryptedTextArray);
	}

	private PrivateKey loadPrivateKey(String key64) throws GeneralSecurityException {
		byte[] clear = Base64.getDecoder().decode(key64);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
		KeyFactory fact = KeyFactory.getInstance(keyFactoryInstance);
		PrivateKey priv = fact.generatePrivate(keySpec);
		Arrays.fill(clear, (byte) 0);
		return priv;
	}

	private PublicKey loadPublicKey(String stored) throws GeneralSecurityException {
		byte[] data = Base64.getDecoder().decode(stored);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
		KeyFactory fact = KeyFactory.getInstance(keyFactoryInstance);
		return fact.generatePublic(spec);
	}

	private String savePrivateKey(PrivateKey priv) throws GeneralSecurityException {
		KeyFactory fact = KeyFactory.getInstance(keyFactoryInstance);
		PKCS8EncodedKeySpec spec = fact.getKeySpec(priv, PKCS8EncodedKeySpec.class);
		byte[] packed = spec.getEncoded();
		String key64 = Base64.getEncoder().encodeToString(packed);

		Arrays.fill(packed, (byte) 0);
		return key64;
	}

	private String savePublicKey(PublicKey publ) throws GeneralSecurityException {
		KeyFactory fact = KeyFactory.getInstance(keyFactoryInstance);
		X509EncodedKeySpec spec = fact.getKeySpec(publ, X509EncodedKeySpec.class);
		return Base64.getEncoder().encodeToString(spec.getEncoded());
	}
}
