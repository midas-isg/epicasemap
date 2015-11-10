package models.entities;

public class Mode {
	private Boolean use;
	private Boolean read;
	private Boolean change;
	private Boolean permit;
	
	public Boolean getUse() {
		return use;
	}

	public void setUse(Boolean use) {
		this.use = use;
	}

	public Boolean getRead_data() {
		return read;
	}

	public void setRead_data(Boolean read) {
		this.read = read;
	}

	public Boolean getChange() {
		return change;
	}

	public void setChange(Boolean change) {
		this.change = change;
	}

	public Boolean getPermit() {
		return permit;
	}

	public void setPermit(Boolean permit) {
		this.permit = permit;
	}
}