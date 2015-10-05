package models.filters;

import java.util.List;

import controllers.security.Restricted.Access;

public class Restriction {
	public Long accountId;
	public Long seriesId;
	public List<Access> accesses;
	
	public Restriction(Long accountId, List<Access> accesses) {
		this(accountId, accesses, null );
	}
	
	public Restriction(Long accountId, List<Access> accesses, Long seriesId) {
		this.accountId = accountId;
		this.accesses = accesses;
		this.seriesId = seriesId;
	}
}