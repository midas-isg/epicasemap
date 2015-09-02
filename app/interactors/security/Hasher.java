package interactors.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Salted password hashing. http://crackstation.net/hashing-security.htm */
class Hasher {
	private HashSpec spec;
	
	Hasher(HashSpec hashSpec){
		spec = hashSpec;
	}
	
    String hash(String password, HashParameter hp, String appSalt) {
    	final char[] passwordChars = password.toCharArray();
    	final String comboSalt = hp.getSalt() + HashKit.toHex(appSalt.getBytes());
		final byte[] salt = HashKit.fromHex(comboSalt);
    	final int iterations = hp.getIterationCount();
		final int bits = hp.getHashByteSize() * 8;
		KeySpec spec = new PBEKeySpec(passwordChars, salt, iterations, bits);
        return HashKit.toHex(encode(spec, hp.getAlgorithm()));
    }

    byte[] encode(KeySpec spec, String algorithm){
		SecretKeyFactory factory = getSecretKeyFactory(algorithm);
		SecretKey secretKey = getSecretKey(factory, spec);
		return secretKey.getEncoded();
	}

	private SecretKeyFactory getSecretKeyFactory(String algorithm) {
		try {
			return SecretKeyFactory.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private SecretKey getSecretKey(SecretKeyFactory factory, KeySpec spec){
		try {
			return factory.generateSecret(spec);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	HashParameter makeHashParameter() {
        HashParameter hp = new HashParameter(spec);
        byte[] salt = generateRandomSalt(spec.getSaltByteSize());
        hp.setSalt(HashKit.toHex(salt));
		return hp;
	}

	private byte[] generateRandomSalt(int byteSize) {
		SecureRandom random = new SecureRandom();
        byte[] salt = new byte[byteSize];
        random.nextBytes(salt);
		return salt;
	}
}