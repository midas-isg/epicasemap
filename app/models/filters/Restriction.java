package models.filters;

import java.util.List;

import controllers.security.Restricted.Access;

public class Restriction {
	public Long accountId;
	public Long seriesId;
	public Long vizId;
	public List<Access> accesses;
	
	public Restriction(Long accountId, List<Access> accesses) {
		this(accountId, accesses, null, null);
	}
	
	public Restriction(Long accountId, List<Access> accesses, Long seriesId, Long vizId) {
		this.accountId = accountId;
		this.accesses = accesses;
		this.seriesId = seriesId;
		this.vizId = vizId;
	}
}