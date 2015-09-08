package interactors.security.password;

public class HashedPassword extends HashParameter{
	private String hash;

	public HashedPassword() {
	}

	HashedPassword(HashParameter hp) {
		setSalt(hp.getSalt());
		setIterationCount(hp.getIterationCount());
		setAlgorithm(hp.getAlgorithm());
	}

	public String getHash() {
		return hash;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
		setHashByteSize(hash.length() / 2);
	}
}

class HashParameter extends HashSpec{
	private String salt;
	
	HashParameter() {
	}

	HashParameter(HashSpec spec) {
        setAlgorithm(spec.getAlgorithm());
        setHashByteSize(spec.getHashByteSize());
        setIterationCount(spec.getIterationCount());
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
		setSaltByteSize(salt.length() / 2);
	}
}