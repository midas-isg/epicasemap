package models.filters;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AccountFilter extends GenericFilter implements Filter {
	@JsonIgnore
	private String email;
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}