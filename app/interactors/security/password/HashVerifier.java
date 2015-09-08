package interactors.security.password;

class HashVerifier{
	HashVerifier(){
	}
	
	public boolean verify(String a, String b){
		return compareInConstantTime(HashKit.fromHex(a), HashKit.fromHex(b));
	}
	
    private boolean compareInConstantTime(byte[] a, byte[] b){
        int diff = a.length ^ b.length;
        int length = Math.min(a.length, b.length);
        for(int i = 0; i < length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }
}