package interactors.security.password;

public class Authority {
	private Hasher hasher; 
	private HashVerifier verifier;
	
	public Authority(Hasher hasher, HashVerifier verifier){
		this.hasher = hasher;
		this.verifier = verifier;
	}

	public HashedPassword hash(String password, String appSalt){
        HashParameter hp = hasher.makeHashParameter();
        String hash = hasher.hash(password, hp, appSalt);
        return newHashedPassword(hash, hp);
    }

	private HashedPassword newHashedPassword(String hash, HashParameter hp) {
		final HashedPassword hashedPassword = new HashedPassword(hp);
		hashedPassword.setHash(hash);
		return hashedPassword;
	}

    public boolean verify(String presentedPassword, HashedPassword hp, String appSalt){
    	String correctHash = hp.getHash();
        String presentedHash = hasher.hash(presentedPassword, hp, appSalt);
        return verifier.verify(correctHash, presentedHash);
    }
}