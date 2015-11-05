package models.view;

import models.entities.MetaData;

abstract class Input extends MetaData {
	private Long id;
	private Long ownerId;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long accountId) {
		ownerId = accountId;
	}
}