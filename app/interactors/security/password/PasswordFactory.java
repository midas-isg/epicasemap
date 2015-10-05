package interactors.security.password;


public class PasswordFactory {
	private PasswordFactory(){}

	public static Authenticator makeAuthority(String appSalt){
		HashSpec spec = makeHashSpec();
		return makeAuthority(spec, appSalt);
	}

	static Authenticator makeAuthority(HashSpec spec, String appSalt) {
		return new Authenticator(makeHasher(spec, appSalt), new HashVerifier());
	}

	static Hasher makeHasher(HashSpec spec, String appSalt) {
		return new Hasher(spec, appSalt);
	}
	
	static HashSpec makeHashSpec() {
		HashSpec hashSpec = new HashSpec();
		hashSpec.setAlgorithm("PBKDF2WithHmacSHA1");
		hashSpec.setHashByteSize(24);
		hashSpec.setSaltByteSize(24);
		hashSpec.setIterationCount(1000);
		return hashSpec;
	}
}