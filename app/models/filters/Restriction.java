package models.filters;

import java.util.List;

import controllers.security.Restricted.Access;

public class Restriction {
	public Long accountId;
	public List<Access> accesses;
	
	public Restriction(Long accountId, List<Access> accesses) {
		this.accountId = accountId;
		this.accesses = accesses;
	}
}