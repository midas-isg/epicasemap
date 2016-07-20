package models;

import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;

public  class Registration extends SignIn {
    @Required
	private String name;
    @Required
	private String organization;

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}