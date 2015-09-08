package interactors.security;

import interactors.security.password.Authority;
import interactors.security.password.PasswordFactory;

public class SecurityFactory {
	private SecurityFactory(){}

	public static Authority makePasswordAuthority(){
		return PasswordFactory.makeAuthority();
	}
}