package models.entities;

import interactors.security.password.HashedPassword;

import javax.persistence.Embeddable;

@Embeddable
public class Password extends HashedPassword {
	@Override
	public String getHash() {
		return super.getHash();
	}
	
	@Override
	public String getSalt() {
		return super.getSalt();
	}
	
	public Integer getIterations() {
		return super.getIterationCount();
	}
	
	public void setIterations(Integer n) {
		super.setIterationCount(n);
	}

	@Override
	public String getAlgorithm() {
		return super.getAlgorithm();
	}
}