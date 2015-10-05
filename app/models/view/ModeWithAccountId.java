package models.view;

import java.util.List;

import models.entities.Mode;

public class ModeWithAccountId extends Mode {
	private List<Long> accountIds;

	public List<Long> getAccountIds() {
		return accountIds;
	}

	public void setAccountIds(List<Long> accountIds) {
		this.accountIds = accountIds;
	}
}
