package interactors.security;

public class SecurityFactory {
	private SecurityFactory(){}

	public static PasswordAuthority makePasswordAuthority(){
		HashSpec spec = makeHashSpec();
		return makePasswordAuthority(spec);
	}

	static PasswordAuthority makePasswordAuthority(HashSpec spec) {
		return new PasswordAuthority(new Hasher(spec), new HashVerifier());
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