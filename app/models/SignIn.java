package models;

import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;

public  class SignIn {
	@Required
	private String email;
	@Required
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "email=" + email; // SHOULD NOT include password!
	}
}