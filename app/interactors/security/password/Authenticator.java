package interactors.security.password;

public class Authenticator {
	private Hasher hasher; 
	private HashVerifier verifier;
	
	public Authenticator(Hasher hasher, HashVerifier verifier){
		this.hasher = hasher;
		this.verifier = verifier;
	}

	public HashedPassword hash(String password){
        HashParameter hp = hasher.makeHashParameter();
        String hash = hasher.hash(password, hp);
        return newHashedPassword(hash, hp);
    }

	private HashedPassword newHashedPassword(String hash, HashParameter hp) {
		final HashedPassword hashedPassword = new HashedPassword();
		hashedPassword.copy(hp);
		hashedPassword.setHash(hash);
		return hashedPassword;
	}

    public boolean verify(String presentedPassword, HashedPassword hp){
		if (hp == null || presentedPassword == null)
			return false;
    	String correctHash = hp.getHash();
        String presentedHash = hasher.hash(presentedPassword, hp);
        return verifier.verify(correctHash, presentedHash);
    }
}