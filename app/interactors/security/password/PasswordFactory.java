package interactors.security.password;

import interactors.security.password.HashSpec;
import interactors.security.password.HashVerifier;
import interactors.security.password.Hasher;
import interactors.security.password.Authority;

public class PasswordFactory {
	private PasswordFactory(){}

	public static Authority makeAuthority(){
		HashSpec spec = makeHashSpec();
		return makeAuthority(spec);
	}

	static Authority makeAuthority(HashSpec spec) {
		return new Authority(new Hasher(spec), new HashVerifier());
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