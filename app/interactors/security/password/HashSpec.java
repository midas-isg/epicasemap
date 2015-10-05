package interactors.security.password;

public class HashSpec {
	private int hashByteSize;
	private int saltByteSize;
	private Integer iterationCount;
	private String algorithm;

	public int getHashByteSize() {
		return hashByteSize;
	}
	public void setHashByteSize(int hashByteSize) {
		this.hashByteSize = hashByteSize;
	}
	public int getSaltByteSize() {
		return saltByteSize;
	}
	public void setSaltByteSize(int saltByteSize) {
		this.saltByteSize = saltByteSize;
	}
	public Integer getIterationCount() {
		return iterationCount;
	}
	public void setIterationCount(int iterationCount) {
		this.iterationCount = iterationCount;
	}
	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
}